package com.ojasva.manik.relayq.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RelayqException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
