package com.borrowbox.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Lets the frontend call the API from its own origin.
 *
 * <p>The allowed origins come from configuration rather than being hard-coded,
 * so a deployed build can point at wherever the frontend actually lives without
 * a code change.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final List<String> allowedOrigins;

  public WebConfig(@Value("${borrowbox.cors.allowed-origins}") List<String> allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public void addCorsMappings(@NonNull CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOrigins(allowedOrigins.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }
}
