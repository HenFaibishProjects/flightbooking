package com.example.flightbooking.controllers;

import com.example.flightbooking.requests.FlightSearchRequest;
import com.example.flightbooking.responds.FlightResponse;
import com.example.flightbooking.services.FlightService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/flights")
public class SearchController {

    private final FlightService flightService;

    public SearchController(FlightService flightService) {
        this.flightService = flightService;
    }


    @GetMapping("/search")
    public List<FlightResponse> searchFlights(@Valid @ModelAttribute FlightSearchRequest request) {
        return flightService.searchFlights(request);
    }
}
