package com.example.flightbooking.exceptions;

public class SeatNotAvailableException extends FlightBookingException {

    public SeatNotAvailableException(String message) {
        super(message);
    }
}