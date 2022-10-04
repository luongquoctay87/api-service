package com.example.sample.api.response;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.util.List;

public class FailureResponse extends ApiResponse {

    public FailureResponse() {
        super(HttpStatus.BAD_REQUEST.value(), "Error");
    }

    public FailureResponse(String message) {
        super(HttpStatus.BAD_REQUEST.value(), message);
    }

    public FailureResponse(String message, Object data) {
        super(HttpStatus.BAD_REQUEST.value(), message, data);
    }

    public FailureResponse(int code, String message) {
        super(code, message);
    }

    public FailureResponse(int value, String message, String error) {
        super(value, message, error);
    }

    @Value
    @AllArgsConstructor
    public static class ApiError {
        List<Error> errors;
    }

    @Value
    @AllArgsConstructor
    public static class Error {
        private String field;
        private Object value;
        private String message;
    }
}
