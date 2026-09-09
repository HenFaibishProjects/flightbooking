package com.example.flightbooking.domain;

import java.time.Instant;
import java.util.UUID;

public record FlightBooking(
        UUID id,
        String origin,
        String destination,
        String airline,
        int numberOfTickets,
        Instant flightDate
) {
    public FlightBooking {
        if (numberOfTickets < 1) {
            throw new IllegalArgumentException("Must book at least 1 ticket.");
        }
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and destination cannot be null.");
        }
    }
}
