package com.borrowbox.api;

import com.borrowbox.api.dto.ClockResponse;
import com.borrowbox.api.dto.EventResponse;
import com.borrowbox.model.EventLog;
import com.borrowbox.model.Simulation;
import com.borrowbox.model.Time;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The simulated calendar. Nothing in BorrowBox happens on a real date; loans
 * are booked against day numbers and the whole system only moves when somebody
 * pushes it forward.
 */
@RestController
@RequestMapping("/api/clock")
public class ClockController {

  private final Time time;
  private final Simulation simulation;
  private final EventLog eventLog;

  /**
   * Creates the controller over the shared clock, simulation and event log.
   */
  public ClockController(Time time, Simulation simulation, EventLog eventLog) {
    this.time = time;
    this.simulation = simulation;
    this.eventLog = eventLog;
  }

  @GetMapping
  public ClockResponse current() {
    return ClockResponse.at(time.getCurrentDay());
  }

  /**
   * Moves the calendar on by one day and reports what that day brought with it:
   * the loans that started, the ones that ended.
   */
  @PostMapping("/advance")
  public ClockResponse advance() {
    int eventsBefore = eventLog.size();
    int today = simulation.advanceDay();

    List<EventResponse> raised = eventLog.getEventsAfter(eventsBefore).stream()
        .map(EventResponse::from)
        .toList();

    return new ClockResponse(today, raised);
  }
}
