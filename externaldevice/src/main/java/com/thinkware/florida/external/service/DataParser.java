package com.thinkware.florida.external.service;

/**
 * 데이터 파싱을 위한 상위 클래스
 */
public abstract class DataParser<T> {

    protected DataParseCallback callback;

    public abstract void parse(byte[] buffer, int size);

    public void setListener(DataParseCallback callback) {
        this.callback = callback;
    }

    public void setError(Exception e) {
        if (callback != null) {
            callback.onError(e);
        }
    }

    protected byte[] getInitCommandData() {
        return null;
    }

}
