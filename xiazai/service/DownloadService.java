package com.lixh.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.lixh.BuildConfig;
import com.lixh.Dao.Dao;
import com.lixh.entity.DownloadInfo;
import com.lixh.entity.UiTaskInfo;
import com.lixh.utils.UFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/***
 * 2013/5/25
 *
 * @author wwj下载器服务
 */
public class DownloadService extends Service {
    private DownloadServiceBinder binder;
    private DownLoadCallbackResult callback;
    DownloadService instance;
    public static int STOPSERVICE = 0;
    // 存放各个下载器
    private Map<String, Downloader> downloaders = new HashMap<String, Downloader>();
    // 设置下载线程数为4，这里是我为了方便随便固定的
    String threadcount = "1";
    DownloadTask downloadTask;
    List<DownloadInfo> downloadInfos;
    /**
     * 利用消息处理机制适时更新进度条
     */
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                final String url = (String) msg.obj;
                int length = msg.arg1;
                int size = msg.arg2;
                if (BuildConfig.DEBUG) {
                    Log.e("是否运行了mHandler", url);
                }
                if (binder.uiTask.containsKey(url)) {
                    UiTaskInfo ui = binder.uiTask.get(url);

                    if (ui.getFileSize() != size) {
                        ui.setFileSize(size);
                        // 设置进度条按读取的length长度更新
                    }
                    ui.setComplete(length);
                    if (length == size) {
                        ui.downLoadSuccess();
                        binder.RemoveDownload(url);

                    }

                }

            }

        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        downloadInfos = Dao.getInstance(instance).getAllDownloadinfos();
        for (DownloadInfo infos : downloadInfos) {
            if (infos.getState().equals("2")) {
                Dao.getInstance(instance).updataInfosState("3", infos.getUrl());
            }

        }
        binder = new DownloadServiceBinder();
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("是否执行了 onBind");
        return binder;
    }

    public class DownloadServiceBinder extends Binder {
        public Map<String, UiTaskInfo> uiTask = new HashMap<String, UiTaskInfo>();

        /**
         * 开始下载
         *
         * @param url       下载地址
         * @param localfile 存放地址
         * @param ui        item的 View
         */
        public void startDownload(String url, String localfile,
                                  UiTaskInfo ui) {

            downloadTask = new DownloadTask(url, ui);
            downloadTask.execute(url, localfile, threadcount);
        }

        public void RemoveDownload(String url) {
            if (BuildConfig.DEBUG) {
                Log.e("当前的下载器", downloaders.size() + "");
            }
            if (downloaders.size() != 0 && downloaders.containsKey(url)) {
                downloaders.get(url).reset();
                downloaders.get(url).updataInfosState(url);
                downloaders.remove(url);
                uiTask.remove(url);
            }
        }

        /**
         * 删除
         */
        public Boolean deleteDownload(String url, String localfile) {
            // 初始化一个downloader下载器
            boolean isSuccessed = false;
            try {
                if (downloaders.size() != 0 && downloaders.containsKey(url)) {
                    downloaders.get(url).pause(url);
                    downloaders.get(url).reset();
                    downloaders.remove(url);

                }

                uiTask.remove(url);
                Dao.getInstance(instance).delete(url);
                isSuccessed = UFile.deleteFile(localfile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isSuccessed;
        }

        /**
         * 暂停
         */
        public void pauseDownload(String url) {
            try {
                downloaders.get(url).pause(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void addCallback(DownLoadCallbackResult callback) {
            DownloadService.this.callback = callback;
        }

    }

    class DownloadTask extends AsyncTask<String, Integer, UiTaskInfo> {
        String urlstr = null;
        Downloader downloader = null;
        UiTaskInfo ui;

        public DownloadTask(final String path, UiTaskInfo ui) {
            this.urlstr = path;
            this.ui = ui;
        }

        @Override
        protected void onPreExecute() {
            if (callback != null) {
                callback.onStaticResult(1, urlstr);
            }
        }

        @Override
        protected UiTaskInfo doInBackground(String... params) {
            urlstr = params[0];
            String localfile = params[1];
            int threadcount = Integer.parseInt(params[2]);
            // 初始化一个downloader下载器
            downloader = downloaders.get(urlstr);
            if (downloader == null) {
                downloader = new Downloader(urlstr, localfile, threadcount,
                        instance, mHandler);
                downloaders.put(urlstr, downloader);
            }
            if (downloader.isdownloading())
                return null;
            // 得到下载信息类的个数组成集合
            return downloader.getDownloaderInfors();
        }

        @Override
        protected void onPostExecute(UiTaskInfo loadInfo) {
            if (loadInfo != null) {
                showProgress(loadInfo, urlstr, ui);
                // 调用方法开始下载
                downloader.download();
            }
        }

    }

    ;

    /**
     * 显示进度条
     *
     * @param ui
     */
    private void showProgress(UiTaskInfo loadInfo, String url,
                              UiTaskInfo ui) {
        if (ui != null) {
            ui.setFileSize(loadInfo.getFileSize());
            ui.setComplete(loadInfo.getComplete());
            binder.uiTask.put(url, ui);
        }

    }

    @Override
    public void onDestroy() {
        System.out.println("服务关闭");
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
        for (String strurl : downloaders.keySet()) {
            if (downloaders.get(strurl).isdownloading()) {
                downloaders.get(strurl).pause(strurl);
            }

        }
        super.onDestroy();
    }
}
