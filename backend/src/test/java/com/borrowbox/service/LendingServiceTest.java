package com.borrowbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.borrowbox.model.Contract;
import com.borrowbox.model.EventType;
import com.borrowbox.model.Item;
import com.borrowbox.model.LendingNotAllowedException;
import com.borrowbox.model.Member;
import com.borrowbox.repository.ContractRepository;
import com.borrowbox.repository.EventRepository;
import com.borrowbox.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The loan transaction: who pays whom, how much, and exactly once.
 */
@IntegrationTest
class LendingServiceTest {

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

  private Member lender;
  private Member borrower;
  private Item item;

  @BeforeEach
  void setUp() {
    contracts.deleteAll();
    members.deleteAll();
    events.deleteAll();

    lender = registry.register("Ada", "ada@example.com", "0700000001");
    borrower = registry.register("Linus", "linus@example.com", "0700000002");
    item = registry.listItem(lender.getMemberId(), "Cordless Drill", "18V", "Tools", 10);
    borrower.addCredits(500);
    members.save(borrower);
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
  @DisplayName("leaves the credit total in the system unchanged")
  void leavesTheCreditTotalUnchanged() {
    int totalBefore = lender.getCredits() + borrower.getCredits();

    lending.lend(item, borrower, 2, 4);

    assertThat(lender.getCredits() + borrower.getCredits()).isEqualTo(totalBefore);
  }

  @Test
  @DisplayName("files the loan against the item and both members")
  void filesTheLoanAgainstTheItemAndBothMembers() {
    Contract contract = lending.lend(item, borrower, 2, 4);

    assertThat(item.getContracts()).extracting(Contract::getId).containsExactly(contract.getId());
    assertThat(lending.findFor(lender.getMemberId())).hasSize(1);
    assertThat(lending.findFor(borrower.getMemberId())).hasSize(1);
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

    assertThat(events.findAllByOrderByIdAsc()).singleElement().satisfies(event -> {
      assertThat(event.getType()).isEqualTo(EventType.LOAN_AGREED);
      assertThat(event.getDescription()).contains("Linus", "Cordless Drill", "Ada", "30 credits");
    });
  }

  @Test
  @DisplayName("refuses a borrower who cannot cover the cost, and says nothing")
  void refusesABorrowerWhoCannotPay() {
    Member broke = registry.register("Ken", "ken@example.com", "0700000003");

    assertThatExceptionOfType(LendingNotAllowedException.class)
        .isThrownBy(() -> lending.lend(item, broke, 2, 4))
        .withMessageContaining("0 credits but this loan costs 30");
    assertThat(events.count()).isZero();
  }

  @Test
  @DisplayName("leaves everything untouched when a loan is refused")
  void leavesEverythingUntouchedWhenRefused() {
    Member broke = registry.register("Ken", "ken@example.com", "0700000003");
    int lenderBefore = lender.getCredits();

    assertThatExceptionOfType(LendingNotAllowedException.class)
        .isThrownBy(() -> lending.lend(item, broke, 2, 4));

    assertThat(broke.getCredits()).isZero();
    assertThat(lender.getCredits()).isEqualTo(lenderBefore);
    assertThat(item.getContracts()).isEmpty();
    assertThat(item.isAvailable(2, 4)).isTrue();
  }
}
