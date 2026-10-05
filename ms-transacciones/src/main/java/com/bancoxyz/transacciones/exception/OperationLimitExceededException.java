package com.bancoxyz.transacciones.exception;

public class OperationLimitExceededException extends RuntimeException {

    public OperationLimitExceededException(String message) {
        super(message);
    }
}