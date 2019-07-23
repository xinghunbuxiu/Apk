package com.bt.sdk;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Torrent {

    private final static String TAG = "Torrent";

    private final Map<String, BtValue> mDecoded;
    private final Map<String, BtValue> mDecoded_info;
    private final HashSet<URI> mAllTrackers;
    private final ArrayList<List<URI>> mTrackers;

    private final Date mCreateDate;
    private final String mComment;
    private final String mCreatedBy;
    private final String mName;
    private final int mPieceLength;
    private final LinkedList<TorrentFile> mFiles;
    private final long mSize;

    // 对应bt文件中包含多个文件, 定义TorrentFile类来表示每个文件,方便管理
    public static class TorrentFile {

        public final File file;
        public final long size;

        public TorrentFile(File file, long size) {
            this.file = file;
            this.size = size;
        }
    }

    public Torrent(byte[] torrent) throws IOException {
        mDecoded = BtParser.btDecode(
                new ByteArrayInputStream(torrent)).getMap();

        mDecoded_info = mDecoded.get("info").getMap();
        try {
            mAllTrackers = new HashSet<>();
            mTrackers = new ArrayList<>();
            // 解析获得announce-list, 获取tracker地址
            if (mDecoded.containsKey("announce-list")) {
                List<BtValue> tiers = mDecoded.get("announce-list").getList();
                for (BtValue bv : tiers) {
                    List<BtValue> trackers = bv.getList();
                    if (trackers.isEmpty()) {
                        continue;
                    }

                    List<URI> tier = new ArrayList<>();
                    for (BtValue tracker : trackers) {
                        URI uri = new URI(tracker.getString());

                        if (!mAllTrackers.contains(uri)) {
                            tier.add(uri);
                            mAllTrackers.add(uri);
                        }
                    }

                    if (!tier.isEmpty()) {
                        mTrackers.add(tier);
                    }
                }
            } else if (mDecoded.containsKey("announce")) { // 对应单个tracker地址
                URI tracker = new URI(mDecoded.get("announce").getString());
                mAllTrackers.add(tracker);

                List<URI> tier = new ArrayList<>();
                tier.add(tracker);
                mTrackers.add(tier);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        // 获取文件创建日期
        mCreateDate = mDecoded.containsKey("creation date") ?
                new Date(mDecoded.get("creation date").getLong() * 1000)
                : null;
        // 获取文件的comment
        mComment = mDecoded.containsKey("comment")
                ? mDecoded.get("comment").getString()
                : null;
        // 获取谁创建的文件
        mCreatedBy = mDecoded.containsKey("created by")
                ? mDecoded.get("created by").getString()
                : null;
        // 获取文件名字
        mName = mDecoded_info.get("name").getString();
        mPieceLength = mDecoded_info.get("piece length").getInt();


        mFiles = new LinkedList<>();
        // 解析多文件的信息结构
        if (mDecoded_info.containsKey("files")) {
            for (BtValue file : mDecoded_info.get("files").getList()) {
                Map<String, BtValue> fileInfo = file.getMap();
                StringBuilder path = new StringBuilder();
                for (BtValue pathElement : fileInfo.get("path").getList()) {
                    path.append(File.separator)
                            .append(pathElement.getString());
                }
                mFiles.add(new TorrentFile(
                        new File(mName, path.toString()),
                        fileInfo.get("length").getLong()));
            }
        } else {
            // 对于单文件的bt种子, bt文件的名字就是单文件的名字
            mFiles.add(new TorrentFile(
                    new File(mName),
                    mDecoded_info.get("length").getLong()));
        }

        // 计算bt种子中所有文件的大小
        long size = 0;
        for (TorrentFile file : mFiles) {
            size += file.size;
        }
        mSize = size;
        // 下面就是单纯的将bt种子文件解析的内容打印出来
        String infoType = isMultiFile() ? "Multi" : "Single";
        Log.i(TAG, "Torrent: file information: " + infoType);
        Log.i(TAG, "Torrent: file name: " + mName);
        Log.i(TAG, "Torrent: Announced at: " + (mTrackers.size() == 0 ? " Seems to be trackerless" : ""));
        for (int i = 0; i < mTrackers.size(); ++i) {
            List<URI> tier = mTrackers.get(i);
            for (int j = 0; j < tier.size(); ++j) {
                Log.i(TAG, "Torrent: {} " + (j == 0 ? String.format("%2d. ", i + 1) : "    ")
                        + tier.get(j));
            }
        }

        if (mCreateDate != null) {
            Log.i(TAG, "Torrent: createDate: " + mCreateDate);
        }

        if (mComment != null) {
            Log.i(TAG, "Torrent: Comment: " + mComment);
        }

        if (mCreatedBy != null) {
            Log.i(TAG, "Torrent: created by: " + mCreatedBy);
        }

        if (isMultiFile()) {
            Log.i(TAG, "Found {} file(s) in multi-file torrent structure." + mFiles.size());
            int i = 0;
            for (TorrentFile file : mFiles) {
                Log.i(TAG, "Torrent: file is " +
                        (String.format("%2d. path: %s size: %s", ++i, file.file.getPath(), file.size)));
            }
        }

        long pieces = (mSize / mDecoded_info.get("piece length").getInt()) + 1;

        Log.i(TAG, "Torrent: Pieces....: (byte(s)/piece" +
                pieces + " " + mSize / mDecoded_info.get("piece length").getInt());

        Log.i(TAG, "Torrent: Total size...: " + mSize);
    }

    /**
     * 加载指定的种子文件, 将种子文件转化为Torrent对象
     */
    public static Torrent load(File torrent) throws IOException {
        byte[] data = readFileToByteArray(torrent);
        return new Torrent(data);
    }

    public boolean isMultiFile() {
        return mFiles.size() > 1;
    }

    /**
     * 由file对象获得byte[]对象
     */
    private static byte[] readFileToByteArray(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
