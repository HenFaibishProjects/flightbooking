package com.example.flightbooking.services;

import com.example.flightbooking.requests.FlightSearchRequest;
import com.example.flightbooking.responds.FlightResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightService {
     private

    public List<FlightResponse> searchFlights(@Valid FlightSearchRequest request) {
    }
}
