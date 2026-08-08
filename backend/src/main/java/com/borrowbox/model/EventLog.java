package com.borrowbox.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An observer that remembers what it heard, so the activity of the whole
 * system can be read back later rather than only appearing once on a console.
 */
public class EventLog implements Observer {

  private final List<DomainEvent> events = new ArrayList<>();

  @Override
  public void onEvent(DomainEvent event) {
    events.add(event);
  }

  /** Everything that has happened, oldest first. */
  public List<DomainEvent> getEvents() {
    return new ArrayList<>(events);
  }

  /** The most recent events, newest first, capped at {@code limit}. */
  public List<DomainEvent> getRecentEvents(int limit) {
    List<DomainEvent> newestFirst = new ArrayList<>(events);
    Collections.reverse(newestFirst);
    return newestFirst.subList(0, Math.min(limit, newestFirst.size()));
  }

  /**
   * Everything recorded after the first {@code index} events, oldest first.
   * Lets a caller snapshot the size, do something, and collect exactly what
   * that something raised.
   */
  public List<DomainEvent> getEventsAfter(int index) {
    if (index >= events.size()) {
      return List.of();
    }
    return new ArrayList<>(events.subList(Math.max(index, 0), events.size()));
  }

  public int size() {
    return events.size();
  }
}
