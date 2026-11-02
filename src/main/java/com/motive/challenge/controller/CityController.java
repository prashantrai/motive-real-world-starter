package com.motive.challenge.controller;

import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.motive.challenge.dto.CityRequest;
import com.motive.challenge.model.City;
import com.motive.challenge.service.CityService;

@RestController
@RequestMapping("/api/cities")
public class CityController {
    private final CityService service;
    public CityController(CityService service) { this.service = service; }

    @GetMapping
    public List<City> list(@RequestParam(required = false) String state) { return service.list(state); }

    @GetMapping("/{id}")
    public City byId(@PathVariable long id) {
        return service.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found"));
    }
    
    /*@PostMapping // without request dto
    public ResponseEntity<City> createCity(@RequestBody City city) {
        City savedCity = service.saveCity(city);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCity);
    }*/
    
    // with request dto
    @PostMapping
    public ResponseEntity<City> createCity(@RequestBody CityRequest request) {

        if (request.city == null || request.city.isBlank()
                || request.stateCode == null || request.stateCode.isBlank()
                || !Double.isFinite(request.latitude)
                || !Double.isFinite(request.longitude)
                || request.latitude < -90 || request.latitude > 90
                || request.longitude < -180 || request.longitude > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid city data");
        }

        City city = new City(
                request.city,
                request.stateCode,
                request.latitude,
                request.longitude
        );

        City saved = service.saveCity(city);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    

    @GetMapping("/top-states")
    public List<CityService.StateCount> topStates(@RequestParam(defaultValue = "3") int k) {
        if (k < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "k must be >= 1");
        return service.topStates(k);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Integer>> importFile(@RequestParam("file") MultipartFile file) throws IOException {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return ResponseEntity.ok(Map.of("imported", service.importCsv(reader)));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV file", ex);
        }
    }
}
