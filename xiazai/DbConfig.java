package com.lixh;

/**
 * Created by LIXH on 2019/7/3.
 * email lixhVip9@163.com
 * des
 */
public class DbConfig {
    private static final class HolderClass {
        private static final DbConfig INSTANCE = new DbConfig();
    }

    private int dbVersion;
    private int threadCount = 1;

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }


    public static DbConfig getImpl() {
        return HolderClass.INSTANCE;
    }

    public int getThreadCount() {
        return this.threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * SDcardUtil.getDbDir()+"/download.db"
     */
    private String dbName;

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
