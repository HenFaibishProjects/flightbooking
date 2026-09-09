package com.example.flightbooking.responds;

import java.time.Instant;

public record FlightResponse(
        String origin,
        String destination,
        String airline,
        int numberOfSeats,
        Instant departureTime

) {
}
