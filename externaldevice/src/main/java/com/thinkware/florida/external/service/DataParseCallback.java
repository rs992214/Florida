package com.thinkware.florida.external.service;

/**
 * 데이터 및 오류를 주고 받는 역학을 한다.
 */

public abstract class DataParseCallback<T> {

    public void onParse(T t) {
    }

    public void onError(Exception e) {
    }
}
