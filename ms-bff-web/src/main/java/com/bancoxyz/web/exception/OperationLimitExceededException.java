package com.bancoxyz.web.exception;

public class OperationLimitExceededException extends RuntimeException {

    public OperationLimitExceededException(String message) {
        super(message);
    }
}