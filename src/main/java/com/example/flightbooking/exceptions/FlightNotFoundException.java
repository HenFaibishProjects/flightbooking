package com.example.flightbooking.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

public class FlightNotFoundException extends RuntimeException {

    public FlightNotFoundException(String message) {
        super(message);
    }

    @RestControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler(FlightNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleFlightNotFoundException(
                FlightNotFoundException exception,
                HttpServletRequest request
        ) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(BookingNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleBookingNotFoundException(
                BookingNotFoundException exception,
                HttpServletRequest request
        ) {
            return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler({SeatNotAvailableException.class, InvalidBookingException.class})
        public ResponseEntity<ErrorResponse> handleBadRequestException(
                FlightBookingException exception,
                HttpServletRequest request
        ) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                MethodArgumentNotValidException exception,
                HttpServletRequest request
        ) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI());
        }

        @ExceptionHandler(FlightBookingException.class)
        public ResponseEntity<ErrorResponse> handleFlightBookingException(
                FlightBookingException exception,
                HttpServletRequest request
        ) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
        }

        private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
            ErrorResponse response = new ErrorResponse(
                    LocalDateTime.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    path
            );

            return ResponseEntity.status(status).body(response);
        }
    }
}