package com.example.flightbooking.inMemoryDb;

import com.example.flightbooking.domain.FlightBooking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

@Component
public class FlightDataInitializer implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightDataInitializer.class);
    private static final long RANDOM_SEED = 8_675_309L;
    private static final int DAYS_TO_SEED = 60;
    private static final List<Route> ROUTES = List.of(
            new Route("London", "Paris", "British Airways", "Air France"),
            new Route("Madrid", "Rome", "Iberia", "ITA Airways"),
            new Route("Berlin", "Amsterdam", "Lufthansa", "KLM"),
            new Route("Dublin", "Barcelona", "Aer Lingus", "Vueling"),
            new Route("Lisbon", "Madrid", "TAP Air Portugal", "Iberia"),
            new Route("Paris", "Vienna", "Air France", "Austrian Airlines"),
            new Route("Amsterdam", "Copenhagen", "KLM", "SAS"),
            new Route("Rome", "Athens", "ITA Airways", "Aegean Airlines")
    );

    private final BookingDatabase bookingDatabase;
    private final boolean enabled;
    private final int count;

    public FlightDataInitializer(
            BookingDatabase bookingDatabase,
            @Value("${app.seed.enabled:true}") boolean enabled,
            @Value("${app.seed.count:1000}") int count
    ) {
        this.bookingDatabase = bookingDatabase;
        this.enabled = enabled;
        this.count = count;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedFlights();
    }

    public int seedFlights() {
        return seedFlights(LocalDate.now(ZoneOffset.UTC));
    }

    int seedFlights(LocalDate startingDate) {
        if (!enabled || !bookingDatabase.isEmpty()) {
            LOGGER.info("Inserted 0 sample flights");
            return 0;
        }

        Random random = new Random(RANDOM_SEED);
        int inserted = 0;
        for (int index = 0; index < count; index++) {
            bookingDatabase.save(createFlight(index, startingDate, random));
            inserted++;
        }

        LOGGER.info("Inserted {} sample flights", inserted);
        return inserted;
    }

    private FlightBooking createFlight(int index, LocalDate startingDate, Random random) {
        int variant = index % 2;
        int routeAndDateIndex = index / 2;
        Route route = ROUTES.get(routeAndDateIndex % ROUTES.size());
        int dayOffset = routeAndDateIndex / ROUTES.size() % DAYS_TO_SEED + 1;
        LocalTime departureTime = variant == 0
                ? LocalTime.of(8, random.nextInt(60))
                : LocalTime.of(17, random.nextInt(60));

        return new FlightBooking(
                null,
                route.origin(),
                route.destination(),
                variant == 0 ? route.firstAirline() : route.secondAirline(),
                availableSeats(index, random),
                startingDate.plusDays(dayOffset).atTime(departureTime).toInstant(ZoneOffset.UTC)
        );
    }

    private int availableSeats(int index, Random random) {
        return switch (index % 5) {
            case 0 -> 0;
            case 1 -> random.nextInt(1, 4);
            default -> random.nextInt(40, 181);
        };
    }

    private record Route(
            String origin,
            String destination,
            String firstAirline,
            String secondAirline
    ) {
    }
}