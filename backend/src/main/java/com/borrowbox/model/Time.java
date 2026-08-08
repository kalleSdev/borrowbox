package com.borrowbox.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The simulated calendar, stored as a single row so the day survives a
 * restart along with everything booked against it.
 *
 * <p>Every date in BorrowBox is a day number measured against this. There are
 * no real dates anywhere in the system.
 */
@Entity
@Table(name = "simulation_clock")
public class Time {

  /** There is only ever one clock, so its id is fixed. */
  @Id
  private Long id = 1L;

  private int currentDay;

  public Time() {
    this.currentDay = 0;
  }

  public int getCurrentDay() {
    return currentDay;
  }

  public void advanceDay() {
    currentDay++;
  }

  @Override
  public String toString() {
    return String.valueOf(currentDay);
  }
}
