package com.lixh.service;

public interface DownLoadCallbackResult {
    void OnBackResult(String url, int result, int size);

    void onStaticResult(int result, String url);
}