package com.stockviewer.stockapi.exception;

public class TimeFrameNotSupported extends RuntimeException {
    public TimeFrameNotSupported(String message) {
        super(message);
    }
}
