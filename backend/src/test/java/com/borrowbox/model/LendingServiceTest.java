package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The loan transaction: who pays whom, how much, and exactly once.
 */
class LendingServiceTest {

  private Time time;
  private LendingService lending;
  private EventLog log;
  private Member lender;
  private Member borrower;
  private Item item;

  @BeforeEach
  void setUp() {
    time = new Time();
    EventPublisher events = new EventPublisher();
    log = new EventLog();
    events.subscribe(log);
    lending = new LendingService(time, events);
    lender = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time);
    borrower = new Member("Linus", "linus@example.com", "0700000002", "bbbbbb", time);
    item = lender.createItem("Cordless Drill", "18V", "Tools", 10);
    borrower.addCredits(500);
  }

  @Test
  @DisplayName("moves the cost from the borrower to the lender")
  void movesTheCostFromBorrowerToLender() {
    int lenderBefore = lender.getCredits();
    int borrowerBefore = borrower.getCredits();

    Contract contract = lending.lend(item, borrower, 2, 4);

    assertThat(contract.getCost()).isEqualTo(30);
    assertThat(borrower.getCredits()).isEqualTo(borrowerBefore - 30);
    assertThat(lender.getCredits()).isEqualTo(lenderBefore + 30);
  }

  @Test
  @DisplayName("charges the loan exactly once, not once per party it is filed against")
  void chargesTheLoanExactlyOnce() {
    lending.lend(item, borrower, 2, 4);

    assertThat(borrower.getCredits()).isEqualTo(470);
  }

  @Test
  @DisplayName("leaves the credit total in the system unchanged")
  void leavesTheCreditTotalUnchanged() {
    int totalBefore = lender.getCredits() + borrower.getCredits();

    lending.lend(item, borrower, 2, 4);

    assertThat(lender.getCredits() + borrower.getCredits()).isEqualTo(totalBefore);
  }

  @Test
  @DisplayName("files the contract against the item and both members")
  void filesTheContractAgainstTheItemAndBothMembers() {
    Contract contract = lending.lend(item, borrower, 2, 4);

    assertThat(item.getContracts()).containsExactly(contract);
    assertThat(lender.getContracts()).containsExactly(contract);
    assertThat(borrower.getContracts()).containsExactly(contract);
  }

  @Test
  @DisplayName("blocks the period against further loans")
  void blocksThePeriodAgainstFurtherLoans() {
    lending.lend(item, borrower, 2, 4);

    assertThat(item.isAvailable(3, 5)).isFalse();
  }

  @Test
  @DisplayName("announces the agreed loan to anyone listening")
  void announcesTheAgreedLoan() {
    lending.lend(item, borrower, 2, 4);

    assertThat(log.getEvents()).singleElement()
        .satisfies(event -> {
          assertThat(event.type()).isEqualTo(EventType.LOAN_AGREED);
          assertThat(event.description()).contains("Linus", "Cordless Drill", "Ada", "30 credits");
        });
  }

  @Test
  @DisplayName("says nothing when the loan is refused")
  void saysNothingWhenRefused() {
    Member broke = new Member("Ken", "ken@example.com", "0700000003", "cccccc", time);

    assertThatExceptionOfType(LendingNotAllowedException.class)
        .isThrownBy(() -> lending.lend(item, broke, 2, 4));

    assertThat(log.getEvents()).isEmpty();
  }

  @Test
  @DisplayName("refuses a borrower who cannot cover the cost")
  void refusesABorrowerWhoCannotPay() {
    Member broke = new Member("Ken", "ken@example.com", "0700000003", "cccccc", time);
    broke.addCredits(5);

    assertThatExceptionOfType(LendingNotAllowedException.class)
        .isThrownBy(() -> lending.lend(item, broke, 2, 4))
        .withMessageContaining("5 credits but this loan costs 30");
  }

  @Test
  @DisplayName("leaves everything untouched when a loan is refused")
  void leavesEverythingUntouchedWhenRefused() {
    Member broke = new Member("Ken", "ken@example.com", "0700000003", "cccccc", time);
    int lenderBefore = lender.getCredits();

    assertThatExceptionOfType(LendingNotAllowedException.class)
        .isThrownBy(() -> lending.lend(item, broke, 2, 4));

    assertThat(broke.getCredits()).isZero();
    assertThat(lender.getCredits()).isEqualTo(lenderBefore);
    assertThat(item.getContracts()).isEmpty();
    assertThat(item.isAvailable(2, 4)).isTrue();
  }
}
