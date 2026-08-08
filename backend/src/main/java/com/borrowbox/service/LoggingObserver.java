package com.borrowbox.service;

import com.borrowbox.model.DomainEvent;
import com.borrowbox.model.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * An observer that writes events to the application log as they happen.
 *
 * <p>Paired with {@link EventLog}, which stores them. Two listeners on one
 * event stream, and the code raising the event knows about neither.
 */
@Component
public class LoggingObserver implements Observer {

  private static final Logger log = LoggerFactory.getLogger(LoggingObserver.class);

  @Override
  public void onEvent(DomainEvent event) {
    log.info("day {} | {} | {}", event.getDay(), event.getType(), event.getDescription());
  }
}
