package com.example.flightbooking.controllers;

import com.example.flightbooking.domain.FlightBooking;
import com.example.flightbooking.services.FlightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class AllFlightsController {

    private final FlightService flightService;

    public AllFlightsController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public List<FlightBooking> getAllFlights() {
        return flightService.getAllFlights();
    }
}