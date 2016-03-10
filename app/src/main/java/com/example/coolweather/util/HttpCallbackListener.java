package com.example.coolweather.util;

/**
 * Created by Administrator on 2016/2/4 0004.

public interface HttpCallbackListener {
    public void onFinish(String responseContent);
    public void onError(Exception e);
}*/

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
