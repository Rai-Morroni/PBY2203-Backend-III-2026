package com.bancoxyz.mobile.exception;

public class OperationLimitExceededException extends RuntimeException {

    public OperationLimitExceededException(String message) {
        super(message);
    }
}