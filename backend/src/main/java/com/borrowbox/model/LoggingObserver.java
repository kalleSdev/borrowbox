package com.borrowbox.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An observer that writes events to the application log as they happen.
 *
 * <p>Paired with {@link EventLog}, which keeps them for the activity feed. Two
 * listeners on one event stream, and the code raising the event knows about
 * neither of them.
 */
public class LoggingObserver implements Observer {

  private static final Logger log = LoggerFactory.getLogger(LoggingObserver.class);

  @Override
  public void onEvent(DomainEvent event) {
    log.info("day {} | {} | {}", event.day(), event.type(), event.description());
  }
}
