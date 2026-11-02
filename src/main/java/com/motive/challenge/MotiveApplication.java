package com.motive.challenge;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import com.motive.challenge.service.CityService;

@SpringBootApplication
public class MotiveApplication {
    public static void main(String[] args) { SpringApplication.run(MotiveApplication.class, args); }

    @Bean
    CommandLineRunner seedSampleData(CityService service) {
        return args -> {
            try (var in = new InputStreamReader(new ClassPathResource("sample-cities.csv").getInputStream(), StandardCharsets.UTF_8)) {
                service.importCsv(in);
            }
        };
    }
}
