package com.example.flightbooking.requests;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightSearchRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        String airline,
        @Min(1) int numberOfTickets,
        @NotNull @FutureOrPresent LocalDate departureDate

) {
}
