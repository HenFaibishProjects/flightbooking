package com.example.flightbooking.requests;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record FlightSearchRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        String airline,
        @NotNull @FutureOrPresent LocalDate departureDate

) {
}
