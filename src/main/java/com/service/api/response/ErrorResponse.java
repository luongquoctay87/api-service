package com.service.api.response;

public class ErrorResponse extends ApiResponse {
    /**
     * Error response for api
     * @param status
     * @param message
     */
    public ErrorResponse(int status, String message) {
        super(status, message, null);
    }
}
