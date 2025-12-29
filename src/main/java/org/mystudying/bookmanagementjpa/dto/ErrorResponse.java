package org.mystudying.bookmanagementjpa.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}

