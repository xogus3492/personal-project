package com.example.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // enum 클래스 생성자 필수
public enum ErrorCode {

    ACCESS_DENIED(403, "NO_ATHORIZED", "권한이 없습니다."),
    UNAUTHORIZED(401, "UNAUTHORIZED", "unauthorized"),
    ;

    private final int status;
    private final String errorCode;
    private final String message;

}
