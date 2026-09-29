package com.bancoxyz.batch_legacy.exception;

public class OperationLimitExceededException extends RuntimeException {

    public OperationLimitExceededException(String message) {
        super(message);
    }
}