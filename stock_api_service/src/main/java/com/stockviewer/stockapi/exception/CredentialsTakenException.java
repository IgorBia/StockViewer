package com.stockviewer.stockapi.exception;

public class CredentialsTakenException extends RuntimeException {
    public CredentialsTakenException(String message) {
        super(message);
    }
}
