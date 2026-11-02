package com.motive.challenge.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class GeoService {
    private final RestTemplate http;
//    private final RestTemplate http = new RestTemplate();
    
    
    //public GeoService(RestTemplate http) { this.http = http; }
    /*public GeoService() {
        http.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set(
                "User-Agent",
                "MotivePractice/1.0 (contact: mymail@gmail.com)"
            );
            return execution.execute(request, body);
        });
    }*/
    public GeoService(RestTemplateBuilder builder) {
        /*this.http = builder
                .defaultHeader("User-Agent",
                        "MotivePractice/1.0 (mymail@gmail.com)")
                .build(); */
    	
    	this.http = builder
                .defaultHeader("User-Agent", "mymail.rai@gmail.com")
                .build();
                
    }

    public String reverse(double lat, double lon) {
        validate(lat, lon);

        // https://nominatim.openstreetmap.org/reverse?format=json&lat=37.7749&lon=-122.4194
//        String url = "https://nominatim.openstreetmap.org/reverse"
//                + "?format=json&lat=" + lat + "&lon=" + lon;
        
        String url = "https://nominatim.openstreetmap.org/reverse?"
        		+ "format=json&addressdetails=1&lat=" + lat + "&lon=" + lon;

        JsonNode json = http.getForObject(url, JsonNode.class);

        if (json == null || !json.hasNonNull("display_name")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "Address unavailable");
        }

        
        JsonNode address = json.path("address");
	    String road = address.path("road").asText("");
	    String neighbourhood = address.path("neighbourhood").asText("");
	    
	    System.out.println(">>> reverse json :: " + json);
	    System.out.println(">>> reverse road :: " + road);
	    System.out.println(">>> reverse neighbourhood :: " + neighbourhood);
	    
        
        return json.path("display_name").asText();
    }
    
    // calculate the route between two cities
    // http://project-osrm.org/docs/v5.23.0/api/#route-service
	public RouteSummary route(double fromLat, double fromLon, double toLat, double toLon) {

		validate(fromLat, fromLon);
		validate(toLat, toLon);

		// OSRM expects longitude,latitude
		// OSRM expects longitude,latitude
	    String url = "https://router.project-osrm.org/route/v1/driving/"
	            + fromLon + "," + fromLat + ";"
	            + toLon + "," + toLat + "?overview=false";

		JsonNode json = http.getForObject(url, JsonNode.class);	

		if (json == null || !json.path("code").asText().equals("Ok")
	            || json.path("routes").isEmpty()) {
	        throw new ResponseStatusException(
	                HttpStatus.NOT_FOUND, "No route found");
	    }

		JsonNode route = json.path("routes").get(0);
		
		// {"legs":[{"steps":[],"weight":3619.1,"summary":"","duration":3619.1,"distance":77460.3}],"weight_name":"routability","weight":3619.1,"duration":3619.1,"distance":77460.3}

		System.out.println(">>> route:: " + route);

		return new RouteSummary(
	            route.path("distance").asDouble(),
	            route.path("duration").asDouble());
	}
	
	// Address Lookup
	public JsonNode lookup(String osmId) {

	    String url = "https://nominatim.openstreetmap.org/lookup"
	            + "?osm_ids=" + osmId
	            + "&format=json&addressdetails=1";

	    JsonNode json = http.getForObject(url, JsonNode.class);

	    if (json == null || !json.isArray() || json.isEmpty()) {
	        throw new ResponseStatusException(
	                HttpStatus.NOT_FOUND, "OSM object not found");
	    }

	    // if you only want address [working]
	    JsonNode address = json.get(0).path("address");
	    String building = address.path("building").asText("");
	    String road = address.path("road").asText("");
	    
	    return json.get(0);
	}
	
	/* Search in csv (using the endpoint)
	 * 
	 * http://localhost:8080/api/cities/search-by-coordinates?lat=37.775&lon=-122.4183
	 * 
	 * */
	public Map<String, String> findCityByCoordinates(double lat, double lon)
	        throws IOException {

	    String url = "https://raw.githubusercontent.com/kelvins/"
	            + "US-Cities-Database/main/csv/us_cities.csv";

	    // 1. Fetch CSV as a String
	    String csv = http.getForObject(url, String.class);

	    if (csv == null) {
	        throw new IOException("Empty CSV response");
	    }

	    // 2. Parse CSV
	    try (CSVParser parser = CSVFormat.DEFAULT
	            .withFirstRecordAsHeader()
	            .parse(new StringReader(csv))) {

	        // 3. Find matching coordinates
	        for (CSVRecord row : parser) {
	            try {
	                double rowLat = Double.parseDouble(row.get("LATITUDE"));
	                double rowLon = Double.parseDouble(row.get("LONGITUDE"));

	                if (Double.compare(rowLat, lat) == 0
	                        && Double.compare(rowLon, lon) == 0) {
	                    return row.toMap();
	                }
	            } catch (NumberFormatException ex) {
	                // Skip invalid rows
	            }
	        }
	    }

	    return Collections.emptyMap();
	}
	

    /**
     * Validates that latitude and longitude coordinates are finite numbers 
     * and fall within standard geographic boundaries (-90 to 90 for latitude, 
     * and -180 to 180 for longitude).
     *
     * @param lat The latitude to validate
     * @param lon The longitude to validate
     * @throws ResponseStatusException 400 Bad Request if coordinates are invalid
     */
    private void validate(double lat, double lon) {
        // Check for NaN (Not a Number) or Infinity values
        if (!Double.isFinite(lat) || !Double.isFinite(lon) || 
            // Ensure latitude is between the South Pole (-90) and North Pole (90)
            lat < -90 || lat > 90 || 
            // Ensure longitude is between West Hemisphere (-180) and East Hemisphere (180)
            lon < -180 || lon > 180) {
            
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coordinates");
        }
    }
    

    public static class RouteSummary {
        private final double distanceMeters;
        private final double durationSeconds;
        public RouteSummary(double distanceMeters, double durationSeconds) {
            this.distanceMeters = distanceMeters; this.durationSeconds = durationSeconds;
        }
        public double getDistanceMeters() { return distanceMeters; }
        public double getDurationSeconds() { return durationSeconds; }
    }
}
