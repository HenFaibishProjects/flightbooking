package com.example.flightbooking.domain;

import java.time.Instant;
import java.util.UUID;

public record FlightBooking(
        UUID id,
        String origin,
        String destination,
        String airline,
        int availableSeats,
        Instant flightDate
) {
    public FlightBooking {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and destination cannot be null.");
        }
    }
}
