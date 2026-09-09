package com.example.flightbooking.inMemoryDb;

import com.example.flightbooking.domain.FlightBooking;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightDataInitializerTest {

    private static final LocalDate STARTING_DATE = LocalDate.of(2026, 9, 9);
    private static final List<List<String>> ROUTES = List.of(
            List.of("London", "Paris"),
            List.of("Madrid", "Rome"),
            List.of("Berlin", "Amsterdam"),
            List.of("Dublin", "Barcelona"),
            List.of("Lisbon", "Madrid"),
            List.of("Paris", "Vienna"),
            List.of("Amsterdam", "Copenhagen"),
            List.of("Rome", "Athens")
    );

    @Test
    void seedFlightsGeneratesRealisticRepeatableData() {
        BookingDatabase firstDatabase = new BookingDatabase();
        BookingDatabase secondDatabase = new BookingDatabase();

        int inserted = new FlightDataInitializer(firstDatabase, true, 1_000).seedFlights(STARTING_DATE);
        new FlightDataInitializer(secondDatabase, true, 1_000).seedFlights(STARTING_DATE);

        List<FlightBooking> firstFlights = allFlights(firstDatabase);
        List<FlightBooking> secondFlights = allFlights(secondDatabase);
        assertEquals(1_000, inserted);
        assertEquals(1_000, firstDatabase.size());
        assertEquals(1_000, firstFlights.size());
        assertEquals(withoutIds(firstFlights), withoutIds(secondFlights));
        assertTrue(firstFlights.stream().allMatch(flight -> flight.id() != null));
        assertTrue(firstFlights.stream().allMatch(flight -> !flight.origin().equals(flight.destination())));
        assertTrue(firstFlights.stream().allMatch(flight -> flight.availableSeats() >= 0
                && flight.availableSeats() <= 180));
        assertTrue(firstFlights.stream().allMatch(flight -> {
            LocalDate departureDate = flight.flightDate().atZone(ZoneOffset.UTC).toLocalDate();
            return departureDate.isAfter(STARTING_DATE)
                    && !departureDate.isAfter(STARTING_DATE.plusDays(60));
        }));
        assertTrue(firstFlights.stream().anyMatch(flight -> flight.availableSeats() == 0));
        assertTrue(firstFlights.stream().anyMatch(flight -> flight.availableSeats() >= 1
                && flight.availableSeats() <= 3));
        assertTrue(firstFlights.stream().anyMatch(flight -> flight.availableSeats() >= 40));
    }

    @Test
    void seedFlightsCreatesMultipleAirlinesAndTimesForSameRouteAndDate() {
        BookingDatabase database = new BookingDatabase();
        new FlightDataInitializer(database, true, 1_000).seedFlights(STARTING_DATE);

        List<FlightBooking> flights = database.findByRoute("London", "Paris").stream()
                .filter(flight -> flight.flightDate().atZone(ZoneOffset.UTC).toLocalDate()
                        .equals(STARTING_DATE.plusDays(1)))
                .toList();

        assertTrue(flights.stream().map(FlightBooking::airline).distinct().count() >= 2);
        assertTrue(flights.stream().map(FlightBooking::flightDate).distinct().count() >= 2);
    }

    @Test
    void seedFlightsOnlyLoadsAnEmptyDatabase() {
        BookingDatabase database = new BookingDatabase();
        FlightDataInitializer initializer = new FlightDataInitializer(database, true, 25);

        assertEquals(25, initializer.seedFlights(STARTING_DATE));
        assertEquals(0, initializer.seedFlights(STARTING_DATE));
        assertEquals(25, database.size());
    }

    @Test
    void seedFlightsCanBeDisabled() {
        BookingDatabase database = new BookingDatabase();

        int inserted = new FlightDataInitializer(database, false, 1_000).seedFlights(STARTING_DATE);

        assertEquals(0, inserted);
        assertTrue(database.isEmpty());
    }

    @Test
    void searchesExcludeGeneratedFullFlights() {
        BookingDatabase database = new BookingDatabase();
        new FlightDataInitializer(database, true, 1_000).seedFlights(STARTING_DATE);

        var allAirlines = database.search("London", "Paris", null, STARTING_DATE.plusDays(1));
        var fullAirline = database.search("London", "Paris", "British Airways", STARTING_DATE.plusDays(1));

        assertTrue(allAirlines.isPresent());
        assertFalse(allAirlines.orElseThrow().isEmpty());
        assertTrue(allAirlines.orElseThrow().stream().allMatch(flight -> flight.availableSeats() > 0));
        assertTrue(fullAirline.isEmpty());
    }

    private List<FlightBooking> allFlights(BookingDatabase database) {
        return ROUTES.stream()
                .flatMap(route -> database.findByRoute(route.get(0), route.get(1)).stream())
                .toList();
    }

    private List<FlightData> withoutIds(List<FlightBooking> flights) {
        return flights.stream()
                .map(flight -> new FlightData(
                        flight.origin(),
                        flight.destination(),
                        flight.airline(),
                        flight.availableSeats(),
                        flight.flightDate()
                ))
                .sorted((left, right) -> left.toString().compareTo(right.toString()))
                .toList();
    }

    private record FlightData(
            String origin,
            String destination,
            String airline,
            int availableSeats,
            java.time.Instant flightDate
    ) {
        private FlightData {
            assertNotNull(flightDate);
        }
    }
}