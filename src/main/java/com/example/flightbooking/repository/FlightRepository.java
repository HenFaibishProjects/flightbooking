package com.example.flightbooking.repository;

import com.example.flightbooking.domain.FlightBooking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlightRepository {

    /**
     * Retrieves flights matching the given route and departure date only.
     * Airline and seat-availability filtering are business concerns applied by the caller.
     */
    Optional<List<FlightBooking>> search(String origin, String destination, LocalDate departureDate);

    List<FlightBooking> findAll();
}
