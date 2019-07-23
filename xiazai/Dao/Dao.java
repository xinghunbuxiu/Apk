package com.lixh.Dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lixh.DBHelper.DBHelper;
import com.lixh.DbConfig;
import com.lixh.entity.DownloadInfo;
import com.lixh.entity.UiTaskInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个业务类
 */
public class Dao {
    private static Dao dao = null;
    private Context context;
    DbConfig dbConfig;

    private Dao(Context context) {
        this.context = context;
        dbConfig = DbConfig.getImpl();
    }

    public static Dao getInstance(Context context) {
        if (dao == null) {
            dao = new Dao(context);
        }
        return dao;
    }


    public SQLiteDatabase getConnection() {
        SQLiteDatabase sqliteDatabase = null;
        try {
            sqliteDatabase = new DBHelper(context).getReadableDatabase();
        } catch (Exception e) {
        }
        return sqliteDatabase;
    }

    /**
     * 查看数据库中是否有数据
     */
    public synchronized boolean isHasInfors(String urlstr) {
        SQLiteDatabase database = getConnection();
        int count = 0;
        Cursor cursor = null;
        try {
            String sql = "select count(*) from download_info where url=?";
            cursor = database.rawQuery(sql, new String[]{urlstr});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return count == 0;
    }

    public synchronized boolean getHasInfors(String urlstr) {
        SQLiteDatabase database = getConnection();
        int count = 0;
        Cursor cursor = null;
        try {
            String sql = "select count(*) from download_info where url=?";
            cursor = database.rawQuery(sql, new String[]{urlstr});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return count == 0;
    }

    /**
     * 查看数据库中是否有数据
     */
    public synchronized String getDownloadState(String urlstr) {
        SQLiteDatabase database = getConnection();
        String state = null;
        Cursor cursor = null;
        try {
            String sql = "select state from download_info where url=?";
            cursor = database.rawQuery(sql, new String[]{urlstr});
            if (cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                if (count != 0) {
                    state = cursor.getString(cursor.getColumnIndex("state"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return state;
    }

    /**
     * 查看数据库中的所有有数据
     */
    public synchronized List<DownloadInfo> getAllDownloadinfos() {
        SQLiteDatabase database = getConnection();
        List<DownloadInfo> list = new ArrayList<DownloadInfo>();

        Cursor cursor = null;
        try {
            String sql = "select thread_id,start_pos, end_pos,compelete_size,url,state from download_info ";
            cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                DownloadInfo info = new DownloadInfo(cursor.getInt(0),
                        cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
                        cursor.getString(4), cursor.getString(5));
                list.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;

    }

    /**
     * 保存 下载的具体信息
     */
    public synchronized void saveInfos(List<DownloadInfo> infos) {
        SQLiteDatabase database = getConnection();
        try {
            for (DownloadInfo info : infos) {
                String sql = "insert into download_info(thread_id,start_pos, end_pos,compelete_size,url,state) values (?,?,?,?,?,?)";
                Object[] bindArgs = {info.getThreadId(), info.getStartPos(),
                        info.getEndPos(), info.getCompeleteSize(),
                        info.getUrl(), info.getState()};
                database.execSQL(sql, bindArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

    /**
     * 得到下载具体信息
     */
    public synchronized List<DownloadInfo> getInfos(String urlstr) {
        List<DownloadInfo> list = new ArrayList<DownloadInfo>();
        SQLiteDatabase database = getConnection();
        Cursor cursor = null;
        try {
            String sql = "select thread_id, start_pos, end_pos,compelete_size,url,state from download_info where url=?";
            cursor = database.rawQuery(sql, new String[]{urlstr});
            while (cursor.moveToNext()) {
                DownloadInfo info = new DownloadInfo(cursor.getInt(0),
                        cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
                        cursor.getString(4), cursor.getString(5));
                list.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;
    }

    public UiTaskInfo getDownloadInfoSize(List<DownloadInfo> infos) {
        Log.v("TAG", "not isFirst size=" + infos.size());
        int size = 0;
        int compeleteSize = 0;
        for (DownloadInfo info : infos) {
            compeleteSize += info.getCompeleteSize();
            size += info.getEndPos() - info.getStartPos() + 1;
        }
        return new UiTaskInfo(size, compeleteSize, infos.get(0).getUrl());
    }

    /**
     * 更新数据库中的下载信息
     */
    public synchronized void updataInfos(int threadId, int compeleteSize,
                                         String urlstr) {
        SQLiteDatabase database = getConnection();
        try {
            String sql = "update download_info set compelete_size=? where thread_id=? and url=?";
            Object[] bindArgs = {compeleteSize, threadId, urlstr};
            database.execSQL(sql, bindArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

    /**
     * 更新数据库中的下载信息
     */
    public synchronized void updataInfosState(String state, String urlstr) {
        SQLiteDatabase database = getConnection();
        try {
            String sql = "update download_info set state=? where url=?";
            Object[] bindArgs = {state, urlstr};
            database.execSQL(sql, bindArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }

    /**
     * 下载完成后删除数据库中的数据
     */
    public synchronized void delete(String url) {
        SQLiteDatabase database = getConnection();
        try {
            database.delete("download_info", "url=?", new String[]{url});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != database) {
                database.close();
            }
        }
    }
}