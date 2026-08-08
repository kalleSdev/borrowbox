package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The subject side of the observer pattern.
 */
class EventPublisherTest {

  private final EventPublisher publisher = new EventPublisher();

  private static DomainEvent event(String description) {
    return new DomainEvent(1, EventType.DAY_ADVANCED, description);
  }

  @Test
  @DisplayName("delivers an event to every subscriber")
  void deliversToEverySubscriber() {
    List<String> first = new ArrayList<>();
    List<String> second = new ArrayList<>();
    publisher.subscribe(e -> first.add(e.getDescription()));
    publisher.subscribe(e -> second.add(e.getDescription()));

    publisher.publish(event("something happened"));

    assertThat(first).containsExactly("something happened");
    assertThat(second).containsExactly("something happened");
  }

  @Test
  @DisplayName("does nothing at all when nobody is listening")
  void doesNothingWhenNobodyIsListening() {
    publisher.publish(event("into the void"));
  }

  @Test
  @DisplayName("does not deliver events raised before a subscriber joined")
  void doesNotReplayHistoryToLateSubscribers() {
    publisher.publish(event("before"));

    List<String> heard = new ArrayList<>();
    publisher.subscribe(e -> heard.add(e.getDescription()));
    publisher.publish(event("after"));

    assertThat(heard).containsExactly("after");
  }
}
