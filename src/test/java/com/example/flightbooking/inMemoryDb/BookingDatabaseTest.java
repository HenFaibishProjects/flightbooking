package com.example.flightbooking.inMemoryDb;

import com.example.flightbooking.domain.FlightBooking;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingDatabaseTest {

    private static final LocalDate DEPARTURE_DATE = LocalDate.of(2026, 10, 15);
    private static final Instant DEPARTURE_TIME = Instant.parse("2026-10-15T10:00:00Z");

    @Test
    void searchReturnsOnlyMatchingFlightsWithAvailableSeats() {
        BookingDatabase database = new BookingDatabase();
        FlightBooking availableFlight = save(database, "London", "Paris", "SkyJet", 2, DEPARTURE_TIME);
        save(database, "London", "Paris", "SkyJet", 0, DEPARTURE_TIME);
        save(database, "London", "Paris", "SkyJet", -1, DEPARTURE_TIME);
        save(database, "Berlin", "Paris", "SkyJet", 2, DEPARTURE_TIME);
        save(database, "London", "Rome", "SkyJet", 2, DEPARTURE_TIME);
        save(database, "London", "Paris", "OtherAirline", 2, DEPARTURE_TIME);
        save(database, "London", "Paris", "SkyJet", 2, Instant.parse("2026-10-16T10:00:00Z"));

        Optional<List<FlightBooking>> result = database.search("london", "PARIS", "SkyJet", DEPARTURE_DATE);

        assertEquals(Optional.of(List.of(availableFlight)), result);
    }

    @Test
    void searchReturnsEmptyWhenAllMatchingFlightsAreFull() {
        BookingDatabase database = new BookingDatabase();
        save(database, "London", "Paris", "SkyJet", 0, DEPARTURE_TIME);
        save(database, "London", "Paris", "SkyJet", -2, DEPARTURE_TIME);

        Optional<List<FlightBooking>> result = database.search("London", "Paris", "SkyJet", DEPARTURE_DATE);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchDoesNotModifyStoredFlights() {
        BookingDatabase database = new BookingDatabase();
        save(database, "London", "Paris", "SkyJet", 4, DEPARTURE_TIME);
        save(database, "London", "Paris", "SkyJet", 0, DEPARTURE_TIME);
        List<FlightBooking> flightsBeforeSearch = database.findByRoute("London", "Paris");

        database.search("London", "Paris", "SkyJet", DEPARTURE_DATE);

        assertEquals(flightsBeforeSearch, database.findByRoute("London", "Paris"));
    }

    private FlightBooking save(
            BookingDatabase database,
            String origin,
            String destination,
            String airline,
            int availableSeats,
            Instant flightDate
    ) {
        return database.save(new FlightBooking(
                UUID.randomUUID(),
                origin,
                destination,
                airline,
                availableSeats,
                flightDate
        ));
    }
}