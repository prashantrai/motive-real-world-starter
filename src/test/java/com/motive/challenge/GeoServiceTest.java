package com.motive.challenge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.motive.challenge.service.GeoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;

class GeoServiceTest {
    private final RestTemplate http = mock(RestTemplate.class);
    private final RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
//    private final GeoService geo = new GeoService(http);
//    private final GeoService geo = new GeoService();
    private final GeoService geo = new GeoService(builder);
    private final ObjectMapper json = new ObjectMapper();

//    @Test 
    void parsesReverseGeocodeWithoutNetwork() throws Exception {
        JsonNode body = json.readTree("{\"display_name\":\"San Francisco, California\"}");
        when(http.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(body));
        assertEquals("San Francisco, California", geo.reverse(37.7749, -122.4194));
    }

//    @Test 
    void parsesRouteWithoutNetwork() throws Exception {
        when(http.getForObject(anyString(), eq(JsonNode.class)))
                .thenReturn(json.readTree("{\"code\":\"Ok\",\"routes\":[{\"distance\":1200,\"duration\":180}]}"));
        GeoService.RouteSummary r = geo.route(37, -122, 38, -121);
        assertEquals(1200, r.getDistanceMeters());
        assertEquals(180, r.getDurationSeconds());
    }

//    @Test 
    void invalidCoordinatesRejected() {
        assertThrows(ResponseStatusException.class, () -> geo.reverse(100, 0));
        verifyNoInteractions(http);
    }
}
