package com.borrowbox.model;

/**
 * Something that wants to hear about domain events.
 *
 * <p>Implementations decide what to do with them: keep a log, print them,
 * push them to a client. The part of the system raising the event neither
 * knows nor cares.
 */
@FunctionalInterface
public interface Observer {

  void onEvent(DomainEvent event);
}
