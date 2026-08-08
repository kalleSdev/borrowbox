package com.borrowbox.api;

import com.borrowbox.api.dto.ClockResponse;
import com.borrowbox.api.dto.EventResponse;
import com.borrowbox.service.ClockService;
import com.borrowbox.service.Simulation;
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

  private final ClockService clock;
  private final Simulation simulation;

  public ClockController(ClockService clock, Simulation simulation) {
    this.clock = clock;
    this.simulation = simulation;
  }

  @GetMapping
  public ClockResponse current() {
    return ClockResponse.at(clock.today());
  }

  /**
   * Moves the calendar on by one day and reports what that day brought with it:
   * the loans that started, the ones that ended.
   */
  @PostMapping("/advance")
  public ClockResponse advance() {
    Simulation.DayAdvanced result = simulation.advanceDay();
    List<EventResponse> raised = result.events().stream().map(EventResponse::from).toList();
    return new ClockResponse(result.day(), raised);
  }
}
