package com.motive.challenge.service;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.motive.challenge.model.City;
import com.motive.challenge.repository.CityRepository;

@Service
public class CityService {
    private final CityRepository repository;
    //private final RestTemplate http;
    private final RestTemplate http = new RestTemplate();


	/*public CityService(CityRepository repository, RestTemplate http) {
		this.repository = repository;
	    this.http = http;
	}*/
    public CityService(CityRepository repository) {
        this.repository = repository;
    }

    public List<City> list(String state) {
        return state == null || state.isBlank() ? repository.findAll()
                : repository.findByStateCodeIgnoreCaseOrderByCityAsc(state);
    }

	public Optional<City> find(long id) {
		return repository.findById(id);
	}
	
	public City saveCity(City city) {
	    return repository.save(city);
	}
	
    @Transactional
    public int importCsv(Reader reader) throws IOException {
        List<City> cities = new ArrayList<>();
        try (CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim().parse(reader)) {
            for (CSVRecord row : parser) {
                try {
                    String city = row.get("CITY");
                    String state = row.get("STATE_CODE");
                    double lat = Double.parseDouble(row.get("LATITUDE"));
                    double lon = Double.parseDouble(row.get("LONGITUDE"));
                    if (city.isBlank() || state.isBlank() || !Double.isFinite(lat) || !Double.isFinite(lon)
                            || lat < -90 || lat > 90 || lon < -180 || lon > 180) continue;
                    cities.add(new City(city, state, lat, lon));
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    // Bad individual row: skip without terminating the import.
                }
            }
        }
        repository.saveAll(cities);
        return cities.size();
    }

    public List<StateCount> topStates(int k) {
        if (k < 1) throw new IllegalArgumentException("k must be >= 1");
        Map<String, Long> counts = repository.findAll().stream()
                .collect(Collectors.groupingBy(City::getStateCode, Collectors.counting()));
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(k).map(e -> new StateCount(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public static class StateCount {
        private final String state;
        private final long count;
        public StateCount(String state, long count) { this.state = state; this.count = count; }
        public String getState() { return state; }
        public long getCount() { return count; }
    }
}
