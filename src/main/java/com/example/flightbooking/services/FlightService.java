package com.example.flightbooking.services;

import com.example.flightbooking.domain.FlightBooking;
import com.example.flightbooking.exceptions.FlightNotFoundException;
import com.example.flightbooking.inMemoryDb.BookingDatabase;
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

     private final BookingDatabase bookingDatabase;

    public FlightService(BookingDatabase bookingDatabase) {
        this.bookingDatabase = bookingDatabase;
    }

    public List<FlightBooking> getAllFlights() {
        List<FlightBooking> flights = bookingDatabase.findAll();
        LOGGER.debug("Retrieved {} flight(s) from the database", flights.size());
        return flights;
    }

    public List<FlightResponse> searchFlights(@Valid FlightSearchRequest request) {
        LOGGER.debug(
                "Searching flights: origin={}, destination={}, airline={}, departureDate={}",
                request.origin(), request.destination(), request.airline(), request.departureDate()
        );

        List<FlightResponse> results = bookingDatabase.search(
                        request.origin(),
                        request.destination(),
                        request.airline(),
                        request.departureDate()
                )
                .orElseThrow(() -> {
                    LOGGER.debug("No flights found for request: {}", request);
                    return new FlightNotFoundException(
                            "Flight not found: " + request
                    );
                })
                .stream()
                .map(flight -> new FlightResponse(
                        flight.origin(),
                        flight.destination(),
                        flight.airline(),
                        flight.availableSeats(),
                        flight.flightDate()
                ))
                .toList();

        LOGGER.debug("Search returned {} flight(s) for request: {}", results.size(), request);
        return results;
    }
}
