package com.borrowbox.api.dto;

import java.util.List;

/**
 * Where the simulated calendar is, and anything that happened getting there.
 *
 * <p>{@code events} is empty when the clock is merely being read, and holds
 * whatever the new day raised when it has just been advanced.
 */
public record ClockResponse(int currentDay, List<EventResponse> events) {

  public static ClockResponse at(int currentDay) {
    return new ClockResponse(currentDay, List.of());
  }
}
