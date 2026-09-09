//package com.example.flightbooking.controllers;
//
//import com.example.flightbooking.domain.FlightBooking;
//import com.example.flightbooking.inMemoryDb.BookingDatabase;
//import com.example.flightbooking.services.FlightService;
//import org.junit.jupiter.api.Test;
//
//import java.time.Instant;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//class AllFlightsControllerTest {
//
//    @Test
//    void getAllFlightsReturnsEveryStoredFlightWithAllData() {
//        BookingDatabase database = new BookingDatabase();
//        FlightBooking availableFlight = database.save(flight("London", "Paris", 120));
//        FlightBooking fullFlight = database.save(flight("Madrid", "Rome", 0));
//        AllFlightsController controller = new AllFlightsController(new FlightService(database));
//
//        List<FlightBooking> result = controller.getAllFlights();
//
//        assertEquals(2, result.size());
//        assertEquals(availableFlight, result.stream()
//                .filter(flight -> flight.id().equals(availableFlight.id()))
//                .findFirst()
//                .orElseThrow());
//        assertEquals(fullFlight, result.stream()
//                .filter(flight -> flight.id().equals(fullFlight.id()))
//                .findFirst()
//                .orElseThrow());
//        result.forEach(flight -> assertNotNull(flight.id()));
//    }
//
//    private FlightBooking flight(String origin, String destination, int availableSeats) {
//        return new FlightBooking(
//                null,
//                origin,
//                destination,
//                "Example Air",
//                availableSeats,
//                Instant.parse("2026-09-10T08:00:00Z")
//        );
//    }
//}