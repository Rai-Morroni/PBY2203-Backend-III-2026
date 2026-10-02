package com.bancoxyz.core.exception;

public class OperationLimitExceededException extends RuntimeException {

    public OperationLimitExceededException(String message) {
        super(message);
    }
}