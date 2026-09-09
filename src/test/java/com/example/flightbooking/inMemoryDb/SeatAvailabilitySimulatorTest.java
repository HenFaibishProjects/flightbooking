//package com.example.flightbooking.inMemoryDb;
//
//import com.example.flightbooking.domain.FlightBooking;
//import org.junit.jupiter.api.Test;
//
//import java.time.Instant;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Random;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class SeatAvailabilitySimulatorTest {
//
//    private static final LocalDate DEPARTURE_DATE = LocalDate.of(2026, 9, 10);
//    private static final Instant DEPARTURE_TIME = Instant.parse("2026-09-10T08:00:00Z");
//
//    @Test
//    void updateAvailableSeatsPreservesOtherFieldsAndChangesSearchResults() {
//        BookingDatabase database = new BookingDatabase();
//        FlightBooking original = database.save(flight(null, 30));
//
//        var changeToFull = database.updateAvailableSeats(original.id(), current -> 0).orElseThrow();
//        FlightBooking fullFlight = database.findById(original.id()).orElseThrow();
//
//        assertEquals(30, changeToFull.oldAvailableSeats());
//        assertEquals(0, changeToFull.newAvailableSeats());
//        assertEquals(original.id(), fullFlight.id());
//        assertEquals(original.origin(), fullFlight.origin());
//        assertEquals(original.destination(), fullFlight.destination());
//        assertEquals(original.airline(), fullFlight.airline());
//        assertEquals(original.flightDate(), fullFlight.flightDate());
//        assertTrue(database.search("London", "Paris", "Air France", DEPARTURE_DATE).isEmpty());
//
//        database.updateAvailableSeats(original.id(), current -> 12);
//
//        assertEquals(12, database.search("London", "Paris", "Air France", DEPARTURE_DATE)
//                .orElseThrow().getFirst().availableSeats());
//    }
//
//    @Test
//    void simulatorSelectsAtMostTwentyDifferentExistingFlights() {
//        BookingDatabase database = new BookingDatabase();
//        for (int index = 0; index < 25; index++) {
//            database.save(flight(null, 20 + index));
//        }
//        List<FlightBooking> before = database.flightIds().stream()
//                .map(id -> database.findById(id).orElseThrow())
//                .toList();
//        SeatAvailabilitySimulator simulator = new SeatAvailabilitySimulator(database, new Random(42));
//
//        int changed = simulator.simulateSeatChanges();
//
//        long changedRecords = before.stream()
//                .filter(flight -> database.findById(flight.id()).orElseThrow().availableSeats()
//                        != flight.availableSeats())
//                .count();
//        assertEquals(20, changed);
//        assertEquals(20, changedRecords);
//        assertEquals(25, database.size());
//    }
//
//    @Test
//    void simulatorHandlesEmptyAndDeletedFlightsGracefully() {
//        BookingDatabase database = new BookingDatabase();
//        SeatAvailabilitySimulator simulator = new SeatAvailabilitySimulator(database, new Random(42));
//
//        assertEquals(0, simulator.simulateSeatChanges());
//
//        FlightBooking deleted = database.save(flight(UUID.randomUUID(), 0));
//        assertTrue(database.delete(deleted.id()));
//        assertTrue(database.updateAvailableSeats(deleted.id(), current -> 50).isEmpty());
//        assertEquals(0, simulator.simulateSeatChanges());
//    }
//
//    @Test
//    void scheduledUpdatesWaitForReadinessAndRepeatedSearchesReflectChanges() {
//        BookingDatabase database = new BookingDatabase();
//        FlightBooking fullFlight = database.save(flight(null, 0));
//        SeatAvailabilitySimulator simulator = new SeatAvailabilitySimulator(
//                database,
//                new SequenceRandom(1, 49, 0, 1, 59)
//        );
//
//        simulator.simulateSeatAvailability();
//
//        assertEquals(0, database.findById(fullFlight.id()).orElseThrow().availableSeats());
//        assertTrue(database.search("London", "Paris", "Air France", DEPARTURE_DATE).isEmpty());
//
//        simulator.onApplicationReady();
//        simulator.simulateSeatAvailability();
//
//        assertEquals(50, database.findById(fullFlight.id()).orElseThrow().availableSeats());
//        assertTrue(database.search("London", "Paris", "Air France", DEPARTURE_DATE).isPresent());
//
//        simulator.simulateSeatAvailability();
//
//        assertEquals(0, database.findById(fullFlight.id()).orElseThrow().availableSeats());
//        assertTrue(database.search("London", "Paris", "Air France", DEPARTURE_DATE).isEmpty());
//
//        simulator.simulateSeatAvailability();
//
//        assertEquals(60, database.findById(fullFlight.id()).orElseThrow().availableSeats());
//        assertTrue(database.search("London", "Paris", "Air France", DEPARTURE_DATE).isPresent());
//    }
//
//    private FlightBooking flight(UUID id, int availableSeats) {
//        return new FlightBooking(
//                id,
//                "London",
//                "Paris",
//                "Air France",
//                availableSeats,
//                DEPARTURE_TIME
//        );
//    }
//
//    private static class SequenceRandom extends Random {
//
//        private final int[] values;
//        private int index;
//
//        private SequenceRandom(int... values) {
//            this.values = values;
//        }
//
//        @Override
//        public int nextInt(int bound) {
//            return values[index++] % bound;
//        }
//
//        @Override
//        public int nextInt(int origin, int bound) {
//            return origin + nextInt(bound - origin);
//        }
//    }
//}