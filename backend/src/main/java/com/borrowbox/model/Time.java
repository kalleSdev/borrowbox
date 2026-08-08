package com.borrowbox.model;

/**
 * Time class.
 */
public class Time {
  // attribute(s)
  private int currentDay;

  public Time() {
    currentDay = 0;
  }

  // Getter for currentDay
  public int getCurrentDay() {
    return currentDay;
  }

  // Method to advance the day
  public void advanceDay() {
    currentDay++;
  }

  // tostring
  public String toString() {
    return "" + currentDay;
  }
}
