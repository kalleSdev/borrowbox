package com.borrowbox.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The generated documentation and the cross-origin rules the frontend needs.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiDocsTest {

  @Autowired
  private MockMvc mvc;

  @Test
  @DisplayName("publishes an OpenAPI description covering every resource")
  void publishesAnOpenApiDescription() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("BorrowBox API"))
        .andExpect(jsonPath("$.paths['/api/members']").exists())
        .andExpect(jsonPath("$.paths['/api/items']").exists())
        .andExpect(jsonPath("$.paths['/api/contracts']").exists())
        .andExpect(jsonPath("$.paths['/api/clock']").exists())
        .andExpect(jsonPath("$.paths['/api/clock/advance']").exists())
        .andExpect(jsonPath("$.paths['/api/events']").exists());
  }

  @Test
  @DisplayName("lets the frontend dev server call the API")
  void allowsTheFrontendOrigin() throws Exception {
    mvc.perform(options("/api/members")
            .header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
  }

  @Test
  @DisplayName("does not let just anyone call the API from a browser")
  void rejectsAnUnknownOrigin() throws Exception {
    mvc.perform(options("/api/members")
            .header("Origin", "http://evil.example.com")
            .header("Access-Control-Request-Method", "POST"))
        .andExpect(status().isForbidden());
  }
}
