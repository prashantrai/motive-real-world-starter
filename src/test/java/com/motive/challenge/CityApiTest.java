package com.motive.challenge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CityApiTest {
    @Autowired MockMvc mvc;

    @Test void listsSeededCities() throws Exception {
        mvc.perform(get("/api/cities")).andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3));
    }

    @Test void topStates() throws Exception {
        mvc.perform(get("/api/cities/top-states?k=1")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].state").value("CA"))
            .andExpect(jsonPath("$[0].count").value(2));
    }

    @Test void rejectsInvalidK() throws Exception {
        mvc.perform(get("/api/cities/top-states?k=0")).andExpect(status().isBadRequest());
    }
}
