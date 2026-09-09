package com.example.flightbooking.inMemoryDb;

import com.example.flightbooking.domain.FlightBooking;
import com.example.flightbooking.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntUnaryOperator;

@Repository
public class BookingDatabase implements FlightRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingDatabase.class);

    // The actual in-memory storage
    private final ConcurrentHashMap<UUID, FlightBooking> store = new ConcurrentHashMap<>();

    // CREATE or UPDATE
    public FlightBooking save(FlightBooking booking) {
        // Generate an ID if it's a new record
        UUID id = (booking.id() == null) ? UUID.randomUUID() : booking.id();

        var recordToSave = new FlightBooking(
                id,
                booking.origin(),
                booking.destination(),
                booking.airline(),
                booking.availableSeats(),
                booking.flightDate()
        );

        store.put(id, recordToSave);
        LOGGER.debug(
                "Saved flight {} ({} -> {}, airline={}, availableSeats={})",
                id, recordToSave.origin(), recordToSave.destination(),
                recordToSave.airline(), recordToSave.availableSeats()
        );
        return recordToSave;
    }


    public Optional<FlightBooking> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }


    public boolean isEmpty() {
        return store.isEmpty();
    }


    public int size() {
        return store.size();
    }


    public List<UUID> flightIds() {
        return List.copyOf(store.keySet());
    }


    @Override
    public List<FlightBooking> findAll() {
        return store.values().stream().toList();
    }


    public Optional<SeatAvailabilityChange> updateAvailableSeats(UUID id, IntUnaryOperator seatGenerator) {
        AtomicReference<SeatAvailabilityChange> change = new AtomicReference<>();
        store.computeIfPresent(id, (flightId, current) -> {
            int newAvailableSeats = seatGenerator.applyAsInt(current.availableSeats());
            if (newAvailableSeats < 0 || newAvailableSeats > 180) {
                throw new IllegalArgumentException("Available seats must be between 0 and 180.");
            }
            change.set(new SeatAvailabilityChange(flightId, current.availableSeats(), newAvailableSeats));
            return new FlightBooking(
                    current.id(),
                    current.origin(),
                    current.destination(),
                    current.airline(),
                    newAvailableSeats,
                    current.flightDate()
            );
        });
        return Optional.ofNullable(change.get());
    }


    public List<FlightBooking> findByRoute(String origin, String destination) {
        return store.values().stream()
                .filter(b -> b.origin().equalsIgnoreCase(origin) &&
                        b.destination().equalsIgnoreCase(destination))
                .toList();
    }


    public boolean delete(UUID id) {
        boolean deleted = store.remove(id) != null;
        if (deleted) {
            LOGGER.debug("Deleted flight {}", id);
        } else {
            LOGGER.debug("Delete requested for flight {} but it was not found", id);
        }
        return deleted;
    }


    public void clear() {
        store.clear();
    }

    @Override
    public Optional<List<FlightBooking>> search(String origin, String destination, LocalDate departureDate) {
        LOGGER.debug(
                "Searching flights: origin={}, destination={}, departureDate={}",
                origin, destination, departureDate
        );

        List<FlightBooking> matchingFlights = store.values().stream()
                .filter(flight -> flight.origin().equalsIgnoreCase(origin))
                .filter(flight -> flight.destination().equalsIgnoreCase(destination))
                .filter(flight -> flight.flightDate().atZone(ZoneOffset.UTC).toLocalDate().equals(departureDate))
                .toList();

        LOGGER.debug(
                "Search for origin={}, destination={}, departureDate={} returned {} matching flight(s)",
                origin, destination, departureDate, matchingFlights.size()
        );

        return matchingFlights.isEmpty() ? Optional.empty() : Optional.of(matchingFlights);
    }

    public record SeatAvailabilityChange(UUID flightId, int oldAvailableSeats, int newAvailableSeats) {
    }
}
