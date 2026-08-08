package com.borrowbox.config;

import com.borrowbox.model.EventPublisher;
import com.borrowbox.model.Observer;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the parts of the domain that are not themselves Spring beans.
 *
 * <p>EventPublisher is plain Java with no annotations on it, so that the
 * observer pattern can be exercised in a unit test without a container. This
 * is where it meets Spring: every Observer bean in the application is handed
 * to it at startup, which is how EventLog and LoggingObserver end up
 * subscribed without either of them knowing about the other.
 */
@Configuration
public class DomainConfig {

  @Bean
  public EventPublisher eventPublisher(List<Observer> observers) {
    EventPublisher publisher = new EventPublisher();
    observers.forEach(publisher::subscribe);
    return publisher;
  }
}
