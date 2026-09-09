package com.example.flightbooking.services;

import com.example.flightbooking.domain.FlightBooking;
import com.example.flightbooking.exceptions.FlightNotFoundException;
import com.example.flightbooking.repository.FlightRepository;
import com.example.flightbooking.requests.FlightSearchRequest;
import com.example.flightbooking.responds.FlightResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FlightService.class);

     private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public List<FlightBooking> getAllFlights() {
        List<FlightBooking> flights = flightRepository.findAll();
        LOGGER.debug("Retrieved {} flight(s) from the database", flights.size());
        return flights;
    }

    public List<FlightResponse> searchFlights(@Valid FlightSearchRequest request) {
        LOGGER.debug(
                "Searching flights: origin={}, destination={}, airline={}, departureDate={}",
                request.origin(), request.destination(), request.airline(), request.departureDate()
        );

        List<FlightResponse> results = flightRepository.search(
                        request.origin(),
                        request.destination(),
                        request.departureDate()
                )
                .orElseGet(List::of)
                .stream()
                .filter(flight -> matchesAirline(flight, request.airline()))
                .filter(flight -> flight.availableSeats() >= request.numberOfTickets())
                .map(flight -> new FlightResponse(
                        flight.origin(),
                        flight.destination(),
                        flight.airline(),
                        flight.availableSeats(),
                        flight.flightDate()
                ))
                .toList();

        if (results.isEmpty()) {
            LOGGER.debug("No flights found for request: {}", request);
            throw new FlightNotFoundException("Flight not found: " + request);
        }

        LOGGER.debug("Search returned {} flight(s) for request: {}", results.size(), request);
        return results;
    }

    private boolean matchesAirline(FlightBooking flight, String requestedAirline) {
        return requestedAirline == null || requestedAirline.isBlank()
                || flight.airline() != null && flight.airline().equalsIgnoreCase(requestedAirline);
    }
}
