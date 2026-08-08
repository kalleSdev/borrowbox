package com.borrowbox.service;

import com.borrowbox.model.DomainEvent;
import com.borrowbox.model.Observer;
import com.borrowbox.repository.EventRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * An observer that writes down what it heard, so activity can be read back on
 * demand rather than only appearing once as it happens.
 */
@Component
public class EventLog implements Observer {

  private final EventRepository events;

  public EventLog(EventRepository events) {
    this.events = events;
  }

  @Override
  @Transactional
  public void onEvent(DomainEvent event) {
    events.save(event);
  }

  /** Everything that has happened, oldest first. */
  @Transactional(readOnly = true)
  public List<DomainEvent> getEvents() {
    return events.findAllByOrderByIdAsc();
  }

  /** The most recent events, newest first, capped at {@code limit}. */
  @Transactional(readOnly = true)
  public List<DomainEvent> getRecentEvents(int limit) {
    return events.findAllByOrderByIdDesc(PageRequest.of(0, limit));
  }

  @Transactional(readOnly = true)
  public long size() {
    return events.count();
  }
}
