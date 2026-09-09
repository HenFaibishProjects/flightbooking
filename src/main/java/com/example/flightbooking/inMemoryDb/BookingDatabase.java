package com.example.flightbooking.inMemoryDb;

import com.example.flightbooking.domain.FlightBooking;
import com.example.flightbooking.repository.FlightRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookingDatabase implements FlightRepository {

    // The actual in-memory storage
    private final Map<UUID, FlightBooking> store = new ConcurrentHashMap<>();

    // CREATE or UPDATE
    public FlightBooking save(FlightBooking booking) {
        // Generate an ID if it's a new record
        UUID id = (booking.id() == null) ? UUID.randomUUID() : booking.id();

        var recordToSave = new FlightBooking(
                id,
                booking.origin(),
                booking.destination(),
                booking.airline(),
                booking.numberOfTickets(),
                booking.availableSeats(),
                booking.flightDate()
        );

        store.put(id, recordToSave);
        return recordToSave;
    }


    public Optional<FlightBooking> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }


    public List<FlightBooking> findByRoute(String origin, String destination) {
        return store.values().stream()
                .filter(b -> b.origin().equalsIgnoreCase(origin) &&
                        b.destination().equalsIgnoreCase(destination))
                .toList();
    }


    public boolean delete(UUID id) {
        return store.remove(id) != null;
    }


    public void clear() {
        store.clear();
    }

    @Override
    public Optional<List<FlightBooking>> search(String origin, String destination, String airline, LocalDate departureDate) {
        List<FlightBooking> matchingFlights = store.values().stream()
                .filter(flight -> flight.origin().equalsIgnoreCase(origin))
                .filter(flight -> flight.destination().equalsIgnoreCase(destination))
                .filter(flight -> airline == null || airline.isBlank()
                        || flight.airline() != null && flight.airline().equalsIgnoreCase(airline))
                .filter(flight -> flight.flightDate().atZone(ZoneOffset.UTC).toLocalDate().equals(departureDate))
                .filter(flight -> flight.availableSeats() > 0)
                .toList();

        return matchingFlights.isEmpty() ? Optional.empty() : Optional.of(matchingFlights);
    }
}
