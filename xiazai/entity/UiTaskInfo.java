package com.lixh.entity;

/**
 * 自定义的一个记载下载器详细信息的类
 * UI刷新 可继承此类
 */
public class UiTaskInfo {
    public int fileSize;//文件大小
    private int complete;//完成度
    private String url;//下载器标识

    public UiTaskInfo(int fileSize, int complete, String url) {
        this.fileSize = fileSize;
        this.complete = complete;
        this.url = url;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "LoadInfo [fileSize=" + fileSize + ", complete=" + complete
                + ", url=" + url + "]";
    }

    //下载完成调用此方式
    public void downLoadSuccess() {

    }
}