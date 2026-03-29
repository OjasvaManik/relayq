package com.ojasva.manik.relayq.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends RelayqException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
