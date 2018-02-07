package com.ybyc.gateway.nettyplus.core.exception;

public class BytesCheckException extends RuntimeException {

    public BytesCheckException() {
    }

    public BytesCheckException(String message) {
        super(message);
    }

    public BytesCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public BytesCheckException(Throwable cause) {
        super(cause);
    }

    public BytesCheckException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
