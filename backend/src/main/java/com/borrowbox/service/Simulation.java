package com.borrowbox.service;

import com.borrowbox.model.Contract;
import com.borrowbox.model.DomainEvent;
import com.borrowbox.model.EventPublisher;
import com.borrowbox.model.EventType;
import com.borrowbox.model.Time;
import com.borrowbox.repository.ClockRepository;
import com.borrowbox.repository.ContractRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Drives the simulated calendar forward and announces what the new day means
 * for the loans already on the books.
 *
 * <p>Working out what starts and ends today used to sit inline in the console
 * menu loop, which put domain logic in the one place that should only have been
 * reading input.
 */
@Service
public class Simulation {

  private final ClockRepository clocks;
  private final ClockService clock;
  private final ContractRepository contracts;
  private final EventPublisher events;

  /**
   * Creates the simulation over the clock, the loans and the event stream.
   */
  public Simulation(ClockRepository clocks, ClockService clock, ContractRepository contracts,
      EventPublisher events) {
    this.clocks = clocks;
    this.clock = clock;
    this.contracts = contracts;
    this.events = events;
  }

  /**
   * What moving the calendar forward turned out to mean.
   *
   * @param day the day the system is now on
   * @param events everything that day raised, in the order it happened
   */
  public record DayAdvanced(int day, List<DomainEvent> events) {
  }

  /**
   * Moves the calendar on by one day and publishes an event for the new day
   * plus one for every loan starting or ending on it.
   *
   * <p>The raised events are returned as well as published, so the caller can
   * answer with them without going back to the log to work out which ones were
   * new.
   */
  @Transactional
  public DayAdvanced advanceDay() {
    Time time = clock.clock();
    time.advanceDay();
    clocks.save(time);
    int today = time.getCurrentDay();

    List<DomainEvent> raised = new ArrayList<>();
    raised.add(new DomainEvent(today, EventType.DAY_ADVANCED, "Day " + today + " begins."));

    for (Contract contract : contracts.findAll()) {
      if (contract.getStartDay() == today) {
        raised.add(new DomainEvent(today, EventType.LOAN_STARTED,
            contract.getBorrower().getName() + " picks up " + contract.getItem().getItemName()
                + " from " + contract.getLender().getName() + "."));
      }
      if (contract.getEndDay() == today) {
        raised.add(new DomainEvent(today, EventType.LOAN_ENDED,
            contract.getBorrower().getName() + " returns " + contract.getItem().getItemName()
                + " to " + contract.getLender().getName() + "."));
      }
    }

    raised.forEach(events::publish);
    return new DayAdvanced(today, raised);
  }
}
