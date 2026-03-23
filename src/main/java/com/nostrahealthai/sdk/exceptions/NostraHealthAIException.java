package com.nostrahealthai.sdk.exceptions;

import java.util.Map;

/**
 * Exception thrown when the NostraHealthAI API returns an error.
 */
public class NostraHealthAIException extends Exception {
    private final Integer statusCode;
    private final Map<String, Object> response;

    public NostraHealthAIException(String message) {
        this(message, null, null);
    }

    public NostraHealthAIException(String message, Integer statusCode) {
        this(message, statusCode, null);
    }

    public NostraHealthAIException(String message, Integer statusCode, Map<String, Object> response) {
        super(message);
        this.statusCode = statusCode;
        this.response = response;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public Map<String, Object> getResponse() {
        return response;
    }
}
