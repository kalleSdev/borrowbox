package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Lending contracts: the rules that decide whether a loan may go ahead.
 */
class ContractTest {

  private Time time;
  private Member lender;
  private Member borrower;
  private Item item;

  @BeforeEach
  void setUp() {
    time = new Time();
    lender = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time);
    borrower = new Member("Linus", "linus@example.com", "0700000002", "bbbbbb", time);
    item = new Item("Cordless Drill", "18V", "Tools", 10, time);
    lender.addCredits(500);
    borrower.addCredits(500);
  }

  private Contract contractFor(int startDate, int endDate) {
    return new Contract(item, lender, borrower, startDate, endDate, time);
  }

  @Test
  @DisplayName("holds the period it was booked for")
  void holdsTheBookedPeriod() {
    Contract contract = contractFor(2, 4);

    assertThat(contract.isValid()).isTrue();
    assertThat(contract.getStartDate()).isEqualTo(2);
    assertThat(contract.getEndDate()).isEqualTo(4);
    assertThat(contract.getItem()).isSameAs(item);
    assertThat(contract.getOwner()).isSameAs(lender);
    assertThat(contract.getborrower()).isSameAs(borrower);
  }

  @Test
  @DisplayName("can start on the current day")
  void canStartToday() {
    assertThat(contractFor(0, 3).isValid()).isTrue();
  }

  @Test
  @DisplayName("is rejected when it starts in the past")
  void isRejectedWhenItStartsInThePast() {
    time.advanceDay();
    time.advanceDay();

    assertThat(contractFor(1, 3).isValid()).isFalse();
  }

  @Test
  @DisplayName("is rejected when it ends before it starts")
  void isRejectedWhenItEndsBeforeItStarts() {
    assertThat(contractFor(6, 2).isValid()).isFalse();
  }

  @Test
  @DisplayName("is rejected when the item is already booked for that period")
  void isRejectedWhenTheItemIsAlreadyBooked() {
    item.addContract(contractFor(2, 6));

    assertThat(contractFor(4, 8).isValid()).isFalse();
  }

  @Test
  @DisplayName("notifies attached observers of domain events")
  void notifiesAttachedObservers() {
    StringBuilder heard = new StringBuilder();
    Contract contract = contractFor(2, 4);

    contract.attach(heard::append);
    contract.notifyObservers("Contract started.");

    assertThat(heard.toString()).isEqualTo("Contract started.");
  }
}
