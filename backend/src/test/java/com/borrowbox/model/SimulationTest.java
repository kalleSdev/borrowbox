package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Advancing the calendar, and what the new day is announced to mean.
 */
class SimulationTest {

  private Time time;
  private MemberList registry;
  private EventLog log;
  private Simulation simulation;
  private LendingService lending;
  private Member lender;
  private Member borrower;
  private Item item;

  @BeforeEach
  void setUp() {
    time = new Time();
    registry = new MemberList(time);
    EventPublisher events = new EventPublisher();
    log = new EventLog();
    events.subscribe(log);
    simulation = new Simulation(time, registry, events);
    lending = new LendingService(time, events);

    lender = registry.register("Ada", "ada@example.com", "0700000001");
    borrower = registry.register("Linus", "linus@example.com", "0700000002");
    borrower.addCredits(500);
    item = lender.createItem("Cordless Drill", "18V", "Tools", 10);
  }

  @Test
  @DisplayName("moves the clock on and reports the new day")
  void movesTheClockOn() {
    assertThat(simulation.advanceDay()).isEqualTo(1);
    assertThat(time.getCurrentDay()).isEqualTo(1);
  }

  @Test
  @DisplayName("announces every day, even a quiet one")
  void announcesEveryDay() {
    simulation.advanceDay();

    assertThat(log.getEvents()).extracting(DomainEvent::type).containsExactly(EventType.DAY_ADVANCED);
  }

  @Test
  @DisplayName("announces a loan on the day it starts")
  void announcesALoanStarting() {
    lending.lend(item, borrower, 2, 4);

    simulation.advanceDay();
    simulation.advanceDay();

    assertThat(log.getEvents()).extracting(DomainEvent::type)
        .containsExactly(EventType.LOAN_AGREED, EventType.DAY_ADVANCED,
            EventType.DAY_ADVANCED, EventType.LOAN_STARTED);
  }

  @Test
  @DisplayName("announces a loan on the day it ends")
  void announcesALoanEnding() {
    lending.lend(item, borrower, 1, 2);
    simulation.advanceDay();
    simulation.advanceDay();

    assertThat(log.getEvents()).extracting(DomainEvent::type).contains(EventType.LOAN_ENDED);
  }

  @Test
  @DisplayName("announces both when a loan starts and ends on the same day")
  void announcesASingleDayLoanTwice() {
    lending.lend(item, borrower, 1, 1);

    simulation.advanceDay();

    assertThat(log.getEvents()).extracting(DomainEvent::type)
        .containsExactly(EventType.LOAN_AGREED, EventType.DAY_ADVANCED,
            EventType.LOAN_STARTED, EventType.LOAN_ENDED);
  }

  @Test
  @DisplayName("says nothing about loans that are merely in progress")
  void saysNothingAboutLoansInProgress() {
    lending.lend(item, borrower, 1, 5);
    simulation.advanceDay();
    int afterStart = log.size();

    simulation.advanceDay();

    assertThat(log.getEvents().subList(afterStart, log.size()))
        .extracting(DomainEvent::type)
        .containsExactly(EventType.DAY_ADVANCED);
  }
}
