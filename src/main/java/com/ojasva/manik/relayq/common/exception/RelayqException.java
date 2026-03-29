package com.ojasva.manik.relayq.common.exception;

import org.springframework.http.HttpStatus;

public class RelayqException extends RuntimeException {
    private final HttpStatus status;

    public RelayqException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
