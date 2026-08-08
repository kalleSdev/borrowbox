package com.borrowbox.model;

/**
 * An observer that echoes events to the console as they happen.
 *
 * <p>Paired with {@link EventLog}, which keeps them. Two listeners, one event
 * stream, neither of which the code raising the event has to know about.
 */
public class ConsoleEventPrinter implements Observer {

  @Override
  public void onEvent(DomainEvent event) {
    System.out.println("[day " + event.day() + "] " + event.description());
  }
}
