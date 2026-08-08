package com.borrowbox.api;

import com.borrowbox.api.dto.EventResponse;
import com.borrowbox.model.EventLog;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The activity feed.
 *
 * <p>This endpoint is the observer pattern showing through to the outside. The
 * services that raise events know nothing about it; EventLog subscribes once at
 * startup and this reads back what it heard.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

  private static final int DEFAULT_LIMIT = 20;
  private static final int MAX_LIMIT = 200;

  private final EventLog eventLog;

  public EventController(EventLog eventLog) {
    this.eventLog = eventLog;
  }

  /**
   * The most recent activity, newest first.
   */
  @GetMapping
  public List<EventResponse> recent(@RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {
    if (limit < 1 || limit > MAX_LIMIT) {
      throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT + ".");
    }

    return eventLog.getRecentEvents(limit).stream().map(EventResponse::from).toList();
  }
}
