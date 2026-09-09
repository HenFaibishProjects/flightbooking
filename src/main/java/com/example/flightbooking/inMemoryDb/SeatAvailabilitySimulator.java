package com.example.flightbooking.inMemoryDb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@ConditionalOnProperty(
        name = "app.seat-simulator.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SeatAvailabilitySimulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeatAvailabilitySimulator.class);
    private static final int MAX_FLIGHTS_PER_RUN = 20;

    private final BookingDatabase bookingDatabase;
    private final Random random;
    private volatile boolean applicationReady;

    @Autowired
    public SeatAvailabilitySimulator(BookingDatabase bookingDatabase) {
        this(bookingDatabase, new Random());
    }

    SeatAvailabilitySimulator(BookingDatabase bookingDatabase, Random random) {
        this.bookingDatabase = bookingDatabase;
        this.random = random;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady = true;
    }

    @Scheduled(
            fixedDelayString = "${app.seat-simulator.interval-ms:10000}",
            initialDelayString = "${app.seat-simulator.interval-ms:10000}"
    )
    public void simulateSeatAvailability() {
        if (applicationReady) {
            simulateSeatChanges();
        }
    }

    int simulateSeatChanges() {
        List<UUID> flightIds = new ArrayList<>(bookingDatabase.flightIds());
        if (flightIds.isEmpty()) {
            return 0;
        }

        Collections.shuffle(flightIds, random);
        int selectionSize = Math.min(MAX_FLIGHTS_PER_RUN, flightIds.size());
        int changedFlights = 0;
        for (UUID flightId : flightIds.subList(0, selectionSize)) {
            var change = bookingDatabase.updateAvailableSeats(flightId, this::randomAvailableSeats);
            if (change.isPresent()) {
                var updated = change.orElseThrow();
                LOGGER.info(
                        "Changed flight {} available seats from {} to {}",
                        updated.flightId(),
                        updated.oldAvailableSeats(),
                        updated.newAvailableSeats()
                );
                changedFlights++;
            }
        }
        return changedFlights;
    }

    private int randomAvailableSeats(int currentAvailableSeats) {
        int availableSeats;
        do {
            availableSeats = random.nextInt(4) == 0 ? 0 : random.nextInt(1, 181);
        } while (availableSeats == currentAvailableSeats);
        return availableSeats;
    }
}