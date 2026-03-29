package com.ojasva.manik.relayq.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RelayqException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}