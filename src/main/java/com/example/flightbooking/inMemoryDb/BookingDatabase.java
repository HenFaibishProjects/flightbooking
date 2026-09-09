package com.example.flightbooking.inMemoryDb;

import com.example.flightbooking.domain.FlightBooking;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BookingDatabase {

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
                booking.flightDate()
        );

        store.put(id, recordToSave);
        return recordToSave;
    }


    public Optional<FlightBooking> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }


    public List<FlightBooking> findAll() {
        return store.values().stream().toList(); // .toList() is a modern Java feature
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
}
