package com.hipsterbait.android.models;

public interface DataCallback {
    public void onSuccess(byte[] data);
    public void onFail(String error);
}
