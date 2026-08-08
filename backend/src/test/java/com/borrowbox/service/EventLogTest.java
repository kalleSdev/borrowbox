package com.borrowbox.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.borrowbox.model.DomainEvent;
import com.borrowbox.model.EventType;
import com.borrowbox.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The observer that remembers, so activity can be read back on demand.
 */
@IntegrationTest
class EventLogTest {

  @Autowired
  private EventLog log;

  @Autowired
  private EventRepository events;

  @BeforeEach
  void startFromEmpty() {
    events.deleteAll();
  }

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

    assertThat(log.getEvents()).extracting(DomainEvent::getDescription)
        .containsExactly("first", "second");
  }

  @Test
  @DisplayName("returns the most recent events newest first")
  void returnsRecentEventsNewestFirst() {
    record(1, "first");
    record(2, "second");
    record(3, "third");

    assertThat(log.getRecentEvents(2)).extracting(DomainEvent::getDescription)
        .containsExactly("third", "second");
  }

  @Test
  @DisplayName("asks for more recent events than exist without complaining")
  void handlesALimitLargerThanTheLog() {
    record(1, "only one");

    assertThat(log.getRecentEvents(50)).hasSize(1);
  }

  @Test
  @DisplayName("survives a restart, because it is on disk rather than in memory")
  void isStoredRatherThanHeldInMemory() {
    record(1, "written down");

    assertThat(events.findAllByOrderByIdAsc()).extracting(DomainEvent::getDescription)
        .containsExactly("written down");
  }
}
