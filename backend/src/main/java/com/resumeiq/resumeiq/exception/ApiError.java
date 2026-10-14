package com.resumeiq.resumeiq.exception;

import java.time.Instant;
import java.util.Map;

public class ApiError {
    private final Instant timestamp;
    private final String code;
    private final String message;
    private final Map<String, String> details;

    public ApiError(String code, String message, Map<String, String> details) {
        this.timestamp = Instant.now();
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
