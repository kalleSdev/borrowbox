package com.borrowbox.model;

/**
 * Drives the simulated calendar forward and announces what the new day means
 * for the loans already on the books.
 *
 * <p>Walking every member's items and their contracts to work out what starts
 * and ends today used to sit inline in the menu loop, which put domain logic in
 * the one place that should only be reading input.
 */
public class Simulation {

  private final Time time;
  private final MemberList members;
  private final EventPublisher events;

  /**
   * Creates a simulation over the given registry and clock.
   */
  public Simulation(Time time, MemberList members, EventPublisher events) {
    this.time = time;
    this.members = members;
    this.events = events;
  }

  /**
   * Moves the calendar on by one day and publishes an event for the new day
   * plus one for every loan starting or ending on it.
   *
   * @return the day the system is now on
   */
  public int advanceDay() {
    time.advanceDay();
    int today = time.getCurrentDay();

    events.publish(new DomainEvent(today, EventType.DAY_ADVANCED, "Day " + today + " begins."));

    for (Member member : members.getAllMembers()) {
      for (Item item : member.getOwnedItems()) {
        for (Contract contract : item.getContracts()) {
          if (contract.getStartDay() == today) {
            events.publish(new DomainEvent(today, EventType.LOAN_STARTED,
                contract.getBorrower().getName() + " picks up " + item.getItemName()
                    + " from " + contract.getLender().getName() + "."));
          }
          if (contract.getEndDay() == today) {
            events.publish(new DomainEvent(today, EventType.LOAN_ENDED,
                contract.getBorrower().getName() + " returns " + item.getItemName()
                    + " to " + contract.getLender().getName() + "."));
          }
        }
      }
    }

    return today;
  }
}
