package com.ybyc.gateway.nettyplus.core.exception;

public class TaskExecutingException extends RuntimeException {

    public TaskExecutingException() {
    }

    public TaskExecutingException(String message) {
        super(message);
    }

    public TaskExecutingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskExecutingException(Throwable cause) {
        super(cause);
    }

    public TaskExecutingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
