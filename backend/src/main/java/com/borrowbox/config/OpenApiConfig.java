package com.borrowbox.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Describes the API for the generated documentation at /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

  /**
   * The title and blurb shown at the top of the docs page.
   */
  @Bean
  public OpenAPI borrowBoxOpenApi() {
    return new OpenAPI().info(new Info()
        .title("BorrowBox API")
        .version("v1")
        .description("""
            A peer-to-peer lending system. Members list items they own, borrow \
            each other's for a run of days, and pay for them in credits.

            Time is simulated rather than real: everything is booked against day \
            numbers, and the calendar only moves when POST /api/clock/advance is \
            called. Advancing the day is what starts and ends loans, and each one \
            shows up in the activity feed at GET /api/events.""")
        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")));
  }
}
