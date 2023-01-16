package com.example.board.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse {

    private final String message;
    private final int status;
    private final String errorCode;
    private final List<FieldError> errors;

    private ErrorResponse(ErrorCode errorCode, List<FieldError> fieldErrors) {
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus();
        this.errorCode = errorCode.getErrorCode();
        this.errors = fieldErrors;
    }

    public static ErrorResponse of(final ErrorCode code) {
        return new ErrorResponse(code, null);
    }

    @Getter
    public static class FieldError {

    }

}
