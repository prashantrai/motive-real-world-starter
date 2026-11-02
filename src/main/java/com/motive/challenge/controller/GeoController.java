package com.motive.challenge.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.motive.challenge.service.GeoService;

@RestController
@RequestMapping("/api/geo")
public class GeoController {
    private final GeoService geo;
    public GeoController(GeoService geo) { this.geo = geo; }

    @GetMapping("/reverse")
    public Map<String, String> reverse(@RequestParam double lat, @RequestParam double lon) {
        return Map.of("address", geo.reverse(lat, lon));
    }

    // calculate the route between two cities
    @GetMapping("/route")
    public GeoService.RouteSummary route(@RequestParam double fromLat, @RequestParam double fromLon,
            @RequestParam double toLat, @RequestParam double toLon) {
        return geo.route(fromLat, fromLon, toLat, toLon);
    }
    
    // Address Lookup
    @GetMapping("/lookup")
    public JsonNode lookup(@RequestParam String osmId) {
        return geo.lookup(osmId);
    }
    
    
    /* Search in csv (using the endpoint)
	 * 
	 * http://localhost:8080/api/cities/search-by-coordinates?lat=37.775&lon=-122.4183
	 * 
	 * */
    @GetMapping("/search-by-coordinates")
    public Map<String, String> searchByCoordinates(
            @RequestParam double lat,
            @RequestParam double lon) throws IOException {

        Map<String, String> city =
        		geo.findCityByCoordinates(lat, lon);

        if (city.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "City not found");
        }

        return city;
    }
}
