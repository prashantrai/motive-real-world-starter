
package com.motive.challenge;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StandaloneApiClient {

//    private static final String USER_AGENT = "MotivePractice/1.0 (contact: your-actual-email)";
    private static final String USER_AGENT = "mymail@gmail.com";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Reusable HTTP GET for all three requirements
    public String get(String url) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response = http.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error: " + response.statusCode());
        }

        return response.body();
    }

    // 1. Nominatim: coordinates -> address
    public String reverse(double lat, double lon) throws Exception {

        String url = "https://nominatim.openstreetmap.org/reverse"
                + "?format=json&addressdetails=1"
                + "&lat=" + lat + "&lon=" + lon;

        JsonNode json = mapper.readTree(get(url));

        return json.path("display_name").asText("");
    }

    // 2. GitHub CSV: find city by coordinates
    public String findCity(double lat, double lon) throws Exception {

        String url = "https://raw.githubusercontent.com/kelvins/"
                + "US-Cities-Database/main/csv/us_cities.csv";

        String csv = get(url);

        try (CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .parse(new StringReader(csv))) {

            for (CSVRecord row : parser) {
                try {
                    double rowLat = Double.parseDouble(row.get("LATITUDE"));
                    double rowLon = Double.parseDouble(row.get("LONGITUDE"));

                    if (Double.compare(lat, rowLat) == 0
                            && Double.compare(lon, rowLon) == 0) {
                        return row.toMap().toString();
                    }
                } catch (NumberFormatException ex) {
                    // Skip malformed coordinates
                }
            }
        }

        return "City not found";
    }

    // 3. OSRM: route between two locations
    public String route(double fromLat, double fromLon,
                        double toLat, double toLon) throws Exception {

        // OSRM requires longitude,latitude order
        String url = "https://router.project-osrm.org/route/v1/driving/"
                + fromLon + "," + fromLat + ";"
                + toLon + "," + toLat + "?overview=false";

        JsonNode json = mapper.readTree(get(url));

        if (!"Ok".equals(json.path("code").asText())
                || json.path("routes").isEmpty()) {
            return "No route found";
        }

        JsonNode first = json.path("routes").get(0);

        return "Distance: " + first.path("distance").asDouble()
                + " meters, Duration: "
                + first.path("duration").asDouble() + " seconds";
    }

    public static void main(String[] args) throws Exception {

        StandaloneApiClient client = new StandaloneApiClient();

        // Requirement 1
        System.out.println("Address:");
        System.out.println(client.reverse(37.775, -122.4183));

        // Requirement 2
        System.out.println("\nCity:");
        System.out.println(client.findCity(37.775, -122.4183));

        // Requirement 3
        System.out.println("\nRoute:");
        System.out.println(client.route(
                37.775, -122.4183,
                37.3382, -121.8863));
    }
}
