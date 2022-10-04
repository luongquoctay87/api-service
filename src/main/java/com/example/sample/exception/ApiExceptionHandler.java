package com.example.sample.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final String TIMESTAMP = "timestamp";
    private final String STATUS = "status";
    private final String MESSAGE = "message";
    private final String PATH = "path";
    private final String ERRORS = "errors";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP, new Date());
        body.put(STATUS, status.value());
        body.put(MESSAGE, "Validation error");
        body.put(PATH, request.getDescription(false).replace("uri=", ""));

        // Get all errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList());

        body.put(ERRORS, errors);

        return new ResponseEntity<>(body, headers, status);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ResourceNotFoundException.class)
    public Map<String, Object> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put(TIMESTAMP, new Date());
        errors.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        errors.put(MESSAGE, "Invalid parameter");
        errors.put(PATH, request.getDescription(false).replace("uri=", ""));
        errors.put(ERRORS, e.getMessage());
        return errors;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public void constraintViolationException(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

}
