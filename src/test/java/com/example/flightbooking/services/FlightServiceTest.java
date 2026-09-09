package com.example.flightbooking.services;

import com.example.flightbooking.exceptions.FlightNotFoundException;
import com.example.flightbooking.inMemoryDb.BookingDatabase;
import com.example.flightbooking.domain.FlightBooking;
import com.example.flightbooking.requests.FlightSearchRequest;
import com.example.flightbooking.responds.FlightResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightServiceTest {

    private static final LocalDate DEPARTURE_DATE = LocalDate.of(2026, 10, 15);
    private static final Instant DEPARTURE_TIME = Instant.parse("2026-10-15T10:00:00Z");

    @Test
    void searchFlightsMatchesRouteAndDateCaseInsensitively() {
        BookingDatabase database = new BookingDatabase();
        FlightBooking matching = flight("London", "Paris", "SkyJet", 5, DEPARTURE_TIME);
        database.save(matching);
        database.save(flight("Berlin", "Paris", "SkyJet", 5, DEPARTURE_TIME));
        database.save(flight("London", "Rome", "SkyJet", 5, DEPARTURE_TIME));
        database.save(flight("London", "Paris", "SkyJet", 5, Instant.parse("2026-10-16T10:00:00Z")));
        FlightService service = new FlightService(database);

        List<FlightResponse> results = service.searchFlights(
                new FlightSearchRequest("london", "PARIS", null, 1, DEPARTURE_DATE)
        );

        assertEquals(1, results.size());
        assertEquals("London", results.get(0).origin());
        assertEquals("Paris", results.get(0).destination());
    }

    @Test
    void searchFlightsAppliesOptionalAirlineFilterCaseInsensitively() {
        BookingDatabase database = new BookingDatabase();
        database.save(flight("London", "Paris", "SkyJet", 5, DEPARTURE_TIME));
        database.save(flight("London", "Paris", "AirTwo", 5, DEPARTURE_TIME));
        FlightService service = new FlightService(database);

        List<FlightResponse> filtered = service.searchFlights(
                new FlightSearchRequest("London", "Paris", "skyjet", 1, DEPARTURE_DATE)
        );
        List<FlightResponse> unfiltered = service.searchFlights(
                new FlightSearchRequest("London", "Paris", null, 1, DEPARTURE_DATE)
        );
        List<FlightResponse> blankAirline = service.searchFlights(
                new FlightSearchRequest("London", "Paris", "  ", 1, DEPARTURE_DATE)
        );

        assertEquals(1, filtered.size());
        assertEquals("SkyJet", filtered.get(0).airline());
        assertEquals(2, unfiltered.size());
        assertEquals(2, blankAirline.size());
    }

    @Test
    void searchFlightsExcludesFullFlights() {
        BookingDatabase database = new BookingDatabase();
        database.save(flight("London", "Paris", "SkyJet", 0, DEPARTURE_TIME));
        FlightBooking available = flight("London", "Paris", "AirTwo", 3, DEPARTURE_TIME);
        database.save(available);
        FlightService service = new FlightService(database);

        List<FlightResponse> results = service.searchFlights(
                new FlightSearchRequest("London", "Paris", null, 1, DEPARTURE_DATE)
        );

        assertEquals(1, results.size());
        assertEquals("AirTwo", results.get(0).airline());
    }

    @Test
    void searchFlightsThrowsWhenNoRouteMatchesAtAll() {
        BookingDatabase database = new BookingDatabase();
        FlightService service = new FlightService(database);

        assertThrows(FlightNotFoundException.class, () -> service.searchFlights(
                new FlightSearchRequest("London", "Paris", null, 1, DEPARTURE_DATE)
        ));
    }

    @Test
    void searchFlightsThrowsWhenServiceFilteringRemovesEveryResult() {
        BookingDatabase database = new BookingDatabase();
        // Route/date matches at the repository level, but every flight is full.
        database.save(flight("London", "Paris", "SkyJet", 0, DEPARTURE_TIME));
        database.save(flight("London", "Paris", "AirTwo", -3, DEPARTURE_TIME));
        FlightService service = new FlightService(database);

        FlightNotFoundException exception = assertThrows(FlightNotFoundException.class, () -> service.searchFlights(
                new FlightSearchRequest("London", "Paris", null, 1, DEPARTURE_DATE)
        ));
        assertTrue(exception.getMessage().contains("Flight not found"));
    }

    @Test
    void searchFlightsThrowsWhenAirlineFilterRemovesEveryResult() {
        BookingDatabase database = new BookingDatabase();
        // Route/date and seats match, but the requested airline does not exist on this route.
        database.save(flight("London", "Paris", "SkyJet", 5, DEPARTURE_TIME));

        FlightService service = new FlightService(database);

        assertThrows(FlightNotFoundException.class, () -> service.searchFlights(
                new FlightSearchRequest("London", "Paris", "NoSuchAirline", 1, DEPARTURE_DATE)
        ));
    }

    private FlightBooking flight(String origin, String destination, String airline, int availableSeats, Instant flightDate) {
        return new FlightBooking(UUID.randomUUID(), origin, destination, airline, availableSeats, flightDate);
    }
}
