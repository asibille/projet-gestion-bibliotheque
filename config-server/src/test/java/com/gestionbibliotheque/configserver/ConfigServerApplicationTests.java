package com.gestionbibliotheque.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigServerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldServeConfigForSampleService() throws Exception {
        // Convention Spring Cloud Config : /{application}/{profile}
        mockMvc.perform(get("/sample-service/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertySources[0].source.foo").value("bar"));
    }
}
