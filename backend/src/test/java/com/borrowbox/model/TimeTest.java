package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The simulated clock that every other date in the system is measured against.
 */
class TimeTest {

  @Test
  @DisplayName("starts on day zero")
  void startsOnDayZero() {
    assertThat(new Time().getCurrentDay()).isZero();
  }

  @Test
  @DisplayName("advances one day at a time")
  void advancesOneDayAtATime() {
    Time time = new Time();

    time.advanceDay();
    time.advanceDay();
    time.advanceDay();

    assertThat(time.getCurrentDay()).isEqualTo(3);
  }
}
