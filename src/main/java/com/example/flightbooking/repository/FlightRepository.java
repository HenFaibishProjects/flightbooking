package com.example.flightbooking.repository;

import com.example.flightbooking.domain.FlightBooking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlightRepository {

    Optional<List<FlightBooking>> search(String origin, String destination, LocalDate departureDate);
}
