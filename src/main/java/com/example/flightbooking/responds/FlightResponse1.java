package com.example.flightbooking.responds;

import java.time.LocalDateTime;

public record FlightResponse(
        String origin,
        String destination,
        String airline,
        String numberOfSeats,
        LocalDateTime departureTime

) {
}
