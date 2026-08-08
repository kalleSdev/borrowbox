package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The observer that remembers, so activity can be read back on demand.
 */
class EventLogTest {

  private final EventLog log = new EventLog();

  private void record(int day, String description) {
    log.onEvent(new DomainEvent(day, EventType.DAY_ADVANCED, description));
  }

  @Test
  @DisplayName("starts empty")
  void startsEmpty() {
    assertThat(log.getEvents()).isEmpty();
    assertThat(log.size()).isZero();
  }

  @Test
  @DisplayName("keeps events in the order they arrived")
  void keepsEventsInOrder() {
    record(1, "first");
    record(2, "second");

    assertThat(log.getEvents()).extracting(DomainEvent::description).containsExactly("first", "second");
  }

  @Test
  @DisplayName("returns the most recent events newest first")
  void returnsRecentEventsNewestFirst() {
    record(1, "first");
    record(2, "second");
    record(3, "third");

    assertThat(log.getRecentEvents(2))
        .extracting(DomainEvent::description)
        .containsExactly("third", "second");
  }

  @Test
  @DisplayName("asks for more recent events than exist without complaining")
  void handlesALimitLargerThanTheLog() {
    record(1, "only one");

    assertThat(log.getRecentEvents(50)).hasSize(1);
  }

  @Test
  @DisplayName("hands out a copy of its events, not the list itself")
  void handsOutACopy() {
    record(1, "first");

    log.getEvents().clear();

    assertThat(log.getEvents()).hasSize(1);
  }
}
