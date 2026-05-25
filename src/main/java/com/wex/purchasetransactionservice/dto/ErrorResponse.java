package com.wex.purchasetransactionservice.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        HttpStatus status,
        List<String> messages
) {
    public static ErrorResponse of(HttpStatus status, List<String> message) {
        return new ErrorResponse(Instant.now(), status, message);
    }

}