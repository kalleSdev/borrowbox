package com.borrowbox.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The subject half of the observer pattern: collects observers and fans events
 * out to all of them.
 *
 * <p>Contracts used to carry their own observer list, which meant an observer
 * had to be attached to each contract individually and always after the
 * contract had already been created and its events already raised. Publishing
 * centrally means a listener registered once hears everything.
 */
public class EventPublisher {

  private final List<Observer> observers = new ArrayList<>();

  public void subscribe(Observer observer) {
    observers.add(observer);
  }

  /**
   * Hands the event to every subscriber, in the order they subscribed.
   */
  public void publish(DomainEvent event) {
    for (Observer observer : observers) {
      observer.onEvent(event);
    }
  }
}
