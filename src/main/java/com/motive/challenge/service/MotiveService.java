package com.motive.challenge.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MotiveService {

	private static String filePath = "/Users/prashantrai/Documents/motive-real-world-starter/src/main/resources/dataset_TSMC2014_NYC.csv";
	
	private static final String dateFormatStr = "EEE MMM dd HH:mm:ss Z yyyy"; // "EEE MMM dd HH:mm:ss Z yyyy"
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	
	static Map<String, String[]> longLatMap = new HashMap<>(); // {venueId, [lat, long]}
	
    public MotiveService(String filePath) {
    	//this.filePath = filePath;
    }

    public String hello() { 
    	return "Hello World";
    }
    
    public static String getMostCheckedInVenue(String filePath, String start, String end) throws IOException {
    
    	Instant startTime = OffsetDateTime.parse(start, FORMAT).toInstant();
    	Instant endTime = OffsetDateTime.parse(end, FORMAT).toInstant();
    	
//    	Instant startTime = Instant.parse(start);
//    	Instant endTime = Instant.parse(end);
    	
    	if(startTime.isAfter(endTime)) throw new IllegalArgumentException();
    	
    	Map<String, Integer> counts = new HashMap<>();
    	
    	try(BufferedReader reader = Files.newBufferedReader(Path.of(filePath))) {
    		String line;
    		
    		while((line = reader.readLine()) != null) {
    			
    			if (line.startsWith("userId,")) continue;
    			
    			String[] columns = line.split(",");
    			
    			try {
    				String venueId = columns[1];
    				String latitude = columns[4];;
    				String longitude = columns[5];
    				
    				Instant timestamp = OffsetDateTime.parse(columns[7].trim(), FORMAT).toInstant();
    				
    				if(!timestamp.isBefore(startTime) && !timestamp.isAfter(endTime)) {
    					counts.put(venueId, counts.getOrDefault(venueId, 0)+1);
    					longLatMap.put(venueId, new String[] {latitude, longitude});
    				}
    			} catch (DateTimeException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	String bestVenue = null;
    	int maxCount = 0;
    	
    	for(Map.Entry<String, Integer> entry : counts.entrySet()) {
    		if(entry.getValue() > maxCount) {
    			maxCount = entry.getValue();
    			bestVenue = entry.getKey();
    		}
    	}
    	
    	return bestVenue;
    	
    }

    public static String getMostCheckedInVenue_withCol_timezoneOffset_considered(
            String filePath, String start, String end)
            throws IOException {

        // Parse start and end timestamps in UTC
//        Instant startTime =
//            OffsetDateTime.parse(start, FORMAT).toInstant();
//
//        Instant endTime =
//            OffsetDateTime.parse(end, FORMAT).toInstant();
        
    	// When considering timezoneOffset column
    	// Input represents local date/time, without UTC offset.
        DateTimeFormatter localFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(start, localFormat);
        LocalDateTime endTime = LocalDateTime.parse(end, localFormat);
        

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException(
                "Start time must be before end time");
        }

        Map<String, Integer> counts = new HashMap<>();

        try (BufferedReader reader =
                Files.newBufferedReader(Path.of(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                // Skip CSV header
                if (line.startsWith("userId,")) {
                    continue;
                }

                String[] columns = line.split(",", -1);

                // Skip invalid rows
                if (columns.length < 8) {
                    continue;
                }

                try {
                    String venueId = columns[1].trim();

                    // Dataset timestamp is already in UTC
                    Instant timestamp =
                        OffsetDateTime.parse(
                            columns[7].trim(), FORMAT
                        ).toInstant();

                    // timezoneOffset:
                    // Column 6 contains timezone offset in minutes.
                    // Example: -240 means UTC-4 hours.
                    //
                    // Since the requirement uses UTC timestamps,
                    // we DO NOT apply timezoneOffset here.
                    //
                    // If local-time filtering is required,
                    // uncomment the following lines:
                    //
                     int timezoneOffset =
                         Integer.parseInt(columns[6].trim());
                    
                     LocalDateTime localTimestamp =
                         LocalDateTime.ofInstant(
                             timestamp,
                             ZoneOffset.ofTotalSeconds(
                                 timezoneOffset * 60)
                         );
                    //
                    // IMPORTANT:
                    // For local-time filtering, compare
                    // localTimestamp with local start/end
                    // instead of comparing UTC Instants.
                     
                     LocalTime localTime = localTimestamp.toLocalTime();

                     // for 9-5 while considering offset
//                     if (!localTime.isBefore(LocalTime.of(9, 0)) &&
//                         localTime.isBefore(LocalTime.of(17, 0))) {

                    // timezoneOffset: Compare with input local range.
                    if (!localTimestamp.isBefore(startTime) &&
                             !localTimestamp.isAfter(endTime)) {

                         counts.put(venueId,
                             counts.getOrDefault(venueId, 0) + 1);
                     }
                     

                    // Filter records by UTC date/time
//                    if (!timestamp.isBefore(startTime) &&
//                        !timestamp.isAfter(endTime)) {
//
//                        counts.put(
//                            venueId,
//                            counts.getOrDefault(venueId, 0) + 1
//                        );
//                    }

                } catch (DateTimeException |
                         NumberFormatException e) {

                    // Skip malformed records
                }
            }
        }

        String bestVenue = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {

            if (entry.getValue() > maxCount ||
                (entry.getValue() == maxCount &&
                 (bestVenue == null ||
                  entry.getKey().compareTo(bestVenue) < 0))) {

                maxCount = entry.getValue();
                bestVenue = entry.getKey();
            }
        }

        return bestVenue;
    }
    
    
 // Task 2: Get address from venue ID
    public static String getVenueAddress(String venueId)
            throws IOException, InterruptedException {
    	
    	ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();
    	

        
        String lat = longLatMap.get(venueId)[0];
        String lon = longLatMap.get(venueId)[1];
        
        System.out.println("lat: " + lat);
        System.out.println("lon: " + lon);        

        if (lat == null || lon == null) {
            return "Venue not found";
        }

        String url =
            "https://nominatim.openstreetmap.org/reverse" +
            "?format=json&lat=" + lat + "&lon=" + lon;
        // import java.net.URI;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(java.net.URI.create(url))
            .header("User-Agent", "FoursquareInterviewDemo/1.0")
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response =
            client.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException(
                "Nominatim API error: " + response.statusCode());
        }

        JsonNode json = mapper.readTree(response.body());

        return json.path("display_name")
                   .asText("Address not found");
    }
    
    
    public static void main2(String[] args) throws Exception {
    	String res = getMostCheckedInVenue(filePath, "Tue Apr 03 18:00:09 +0000 2012", "Tue Apr 03 18:27:43 +0000 2012");
    	String res2 = getMostCheckedInVenue_withCol_timezoneOffset_considered(filePath, "Tue Apr 03 18:00:09 +0000 2012", "Tue Apr 03 18:27:43 +0000 2012");
    	System.out.println(res);
    	System.out.println(res2);
    }
    
    public static void main(String[] args) throws Exception {

        String venueId = 
        		getMostCheckedInVenue(filePath, 
        				"Tue Apr 03 18:00:09 +0000 2012", 
        				"Tue Apr 03 18:27:43 +0000 2012");

        System.out.println("Most checked-in venue: " + venueId);

        if (venueId != null) {
            System.out.println("Address: " +
                getVenueAddress(venueId));
        }
    }
    
    
}
