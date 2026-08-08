package com.borrowbox.api.dto;

import com.borrowbox.model.DomainEvent;

/**
 * One entry in the activity feed.
 */
public record EventResponse(int day, String type, String description) {

  /**
   * Describes a domain event for the API.
   */
  public static EventResponse from(DomainEvent event) {
    return new EventResponse(event.day(), event.type().name(), event.description());
  }
}
