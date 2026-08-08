package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The rules that decide whether a loan may be drawn up at all.
 */
class ContractTest {

  private Time time;
  private Member lender;
  private Member borrower;
  private Item item;

  @BeforeEach
  void setUp() {
    time = new Time();
    lender = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time.getCurrentDay());
    borrower = new Member("Linus", "linus@example.com", "0700000002", "bbbbbb", time.getCurrentDay());
    item = lender.createItem("Cordless Drill", "18V", "Tools", 10, time.getCurrentDay());
  }

  private Contract contractFor(int startDay, int endDay) {
    return Contract.create(item, borrower, startDay, endDay, time.getCurrentDay());
  }

  private void assertRejected(int startDay, int endDay, String reason) {
    assertThatExceptionOfType(LendingNotAllowedException.class)
        .isThrownBy(() -> contractFor(startDay, endDay))
        .withMessageContaining(reason);
  }

  @Test
  @DisplayName("holds the period, the parties and the price")
  void holdsThePeriodThePartiesAndThePrice() {
    Contract contract = contractFor(2, 4);

    assertThat(contract.getStartDay()).isEqualTo(2);
    assertThat(contract.getEndDay()).isEqualTo(4);
    assertThat(contract.getItem()).isSameAs(item);
    assertThat(contract.getLender()).isSameAs(lender);
    assertThat(contract.getBorrower()).isSameAs(borrower);
  }

  @Test
  @DisplayName("takes the lender from whoever owns the item")
  void takesTheLenderFromTheItemOwner() {
    assertThat(contractFor(2, 4).getLender()).isSameAs(item.getOwner());
  }

  @Test
  @DisplayName("charges the daily rate for every day of the loan, both ends included")
  void chargesTheDailyRateForEveryDay() {
    Contract contract = contractFor(2, 4);

    assertThat(contract.getDurationInDays()).isEqualTo(3);
    assertThat(contract.getCost()).isEqualTo(30);
  }

  @Test
  @DisplayName("charges a single day for a same-day loan")
  void chargesASingleDayForASameDayLoan() {
    assertThat(contractFor(3, 3).getCost()).isEqualTo(10);
  }

  @Test
  @DisplayName("knows which days it is running on")
  void knowsWhichDaysItIsRunningOn() {
    Contract contract = contractFor(2, 4);

    assertThat(contract.isActiveOn(1)).isFalse();
    assertThat(contract.isActiveOn(2)).isTrue();
    assertThat(contract.isActiveOn(4)).isTrue();
    assertThat(contract.isActiveOn(5)).isFalse();
  }

  @Test
  @DisplayName("can start on the current day")
  void canStartToday() {
    assertThat(contractFor(0, 3).getStartDay()).isZero();
  }

  @Test
  @DisplayName("is refused when it starts in the past")
  void isRefusedWhenItStartsInThePast() {
    time.advanceDay();
    time.advanceDay();

    assertRejected(1, 3, "cannot start in the past");
  }

  @Test
  @DisplayName("is refused when it ends before it starts")
  void isRefusedWhenItEndsBeforeItStarts() {
    assertRejected(6, 2, "cannot end before it starts");
  }

  @Test
  @DisplayName("is refused when a member tries to borrow their own item")
  void isRefusedWhenBorrowingFromYourself() {
    assertThatExceptionOfType(LendingNotAllowedException.class)
        .isThrownBy(() -> Contract.create(item, lender, 2, 4, time.getCurrentDay()))
        .withMessageContaining("cannot borrow their own item");
  }

  @Test
  @DisplayName("is refused when the item is already booked for that period")
  void isRefusedWhenTheItemIsAlreadyBooked() {
    item.addContract(contractFor(2, 6));

    assertRejected(4, 8, "already booked");
  }

  @Test
  @DisplayName("is refused when it would start on the last day of another loan")
  void isRefusedWhenItStartsOnAnotherLoansLastDay() {
    item.addContract(contractFor(2, 6));

    assertRejected(6, 8, "already booked");
  }

  @Test
  @DisplayName("is allowed to start the day after another loan ends")
  void isAllowedToStartTheDayAfterAnotherLoanEnds() {
    item.addContract(contractFor(2, 6));

    assertThat(contractFor(7, 9).getStartDay()).isEqualTo(7);
  }
}
