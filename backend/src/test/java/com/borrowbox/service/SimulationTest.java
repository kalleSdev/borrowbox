package com.borrowbox.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.borrowbox.model.DomainEvent;
import com.borrowbox.model.EventType;
import com.borrowbox.model.Item;
import com.borrowbox.model.Member;
import com.borrowbox.repository.ContractRepository;
import com.borrowbox.repository.EventRepository;
import com.borrowbox.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Advancing the calendar, and what the new day is announced to mean.
 */
@IntegrationTest
class SimulationTest {

  @Autowired
  private Simulation simulation;

  @Autowired
  private ClockService clock;

  @Autowired
  private LendingService lending;

  @Autowired
  private MemberService registry;

  @Autowired
  private MemberRepository members;

  @Autowired
  private ContractRepository contracts;

  @Autowired
  private EventRepository events;

  private Member borrower;
  private Item item;

  @BeforeEach
  void setUp() {
    contracts.deleteAll();
    members.deleteAll();
    events.deleteAll();

    Member lender = registry.register("Ada", "ada@example.com", "0700000001");
    borrower = registry.register("Linus", "linus@example.com", "0700000002");
    borrower.addCredits(500);
    members.save(borrower);
    item = registry.listItem(lender.getMemberId(), "Cordless Drill", "18V", "Tools", 10);
  }

  private int today() {
    return clock.today();
  }

  @Test
  @DisplayName("moves the clock on and reports the new day")
  void movesTheClockOn() {
    int before = today();

    assertThat(simulation.advanceDay().day()).isEqualTo(before + 1);
    assertThat(today()).isEqualTo(before + 1);
  }

  @Test
  @DisplayName("announces every day, even a quiet one")
  void announcesEveryDay() {
    Simulation.DayAdvanced result = simulation.advanceDay();

    assertThat(result.events()).extracting(DomainEvent::getType)
        .containsExactly(EventType.DAY_ADVANCED);
  }

  @Test
  @DisplayName("announces a loan on the day it starts")
  void announcesALoanStarting() {
    lending.lend(item, borrower, today() + 1, today() + 3);

    Simulation.DayAdvanced result = simulation.advanceDay();

    assertThat(result.events()).extracting(DomainEvent::getType)
        .containsExactly(EventType.DAY_ADVANCED, EventType.LOAN_STARTED);
    assertThat(result.events().get(1).getDescription())
        .isEqualTo("Linus picks up Cordless Drill from Ada.");
  }

  @Test
  @DisplayName("announces both when a loan starts and ends on the same day")
  void announcesASingleDayLoanTwice() {
    int tomorrow = today() + 1;
    lending.lend(item, borrower, tomorrow, tomorrow);

    Simulation.DayAdvanced result = simulation.advanceDay();

    assertThat(result.events()).extracting(DomainEvent::getType)
        .containsExactly(EventType.DAY_ADVANCED, EventType.LOAN_STARTED, EventType.LOAN_ENDED);
  }

  @Test
  @DisplayName("announces a loan on the day it ends")
  void announcesALoanEnding() {
    lending.lend(item, borrower, today() + 1, today() + 2);
    simulation.advanceDay();

    Simulation.DayAdvanced result = simulation.advanceDay();

    assertThat(result.events()).extracting(DomainEvent::getType)
        .containsExactly(EventType.DAY_ADVANCED, EventType.LOAN_ENDED);
  }

  @Test
  @DisplayName("says nothing about loans that are merely in progress")
  void saysNothingAboutLoansInProgress() {
    lending.lend(item, borrower, today() + 1, today() + 5);
    simulation.advanceDay();

    Simulation.DayAdvanced result = simulation.advanceDay();

    assertThat(result.events()).extracting(DomainEvent::getType)
        .containsExactly(EventType.DAY_ADVANCED);
  }

  @Test
  @DisplayName("writes everything it announces into the activity log")
  void writesWhatItAnnouncesIntoTheLog() {
    long before = events.count();

    simulation.advanceDay();

    assertThat(events.count()).isEqualTo(before + 1);
  }
}
