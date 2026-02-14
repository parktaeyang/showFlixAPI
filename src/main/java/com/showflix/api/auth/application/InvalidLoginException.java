package com.showflix.api.auth.application;

/**
 * 로그인 실패 시 사용하는 도메인 예외
 */
public class InvalidLoginException extends RuntimeException {

    public InvalidLoginException(String message) {
        super(message);
    }
}

