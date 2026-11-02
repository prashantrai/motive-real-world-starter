package com.motive.challenge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

//@Configuration
public class HttpConfig {
	
	/*
	 * We're using Spring's RestTemplate to make a GET request to Nominatim. Spring
	 * automatically parses the JSON response into a JsonNode. I check whether the
	 * address exists and return its display name. The HTTP client configuration
	 * handles timeouts and the required User-Agent.
	 */
	
    /*@Bean
    RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }*/
    
    
    /*@Bean
    RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set(
                    "User-Agent", "MotivePractice/1.0 (mymail.rai@gmail.com)");
            return execution.execute(request, body);
        });

        return restTemplate;
    }*/
}
