package com.ojasva.manik.relayq.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends RelayqException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
