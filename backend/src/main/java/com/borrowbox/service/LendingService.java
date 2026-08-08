package com.borrowbox.service;

import com.borrowbox.model.Contract;
import com.borrowbox.model.DomainEvent;
import com.borrowbox.model.EventPublisher;
import com.borrowbox.model.EventType;
import com.borrowbox.model.Item;
import com.borrowbox.model.LendingNotAllowedException;
import com.borrowbox.model.Member;
import com.borrowbox.repository.ContractRepository;
import com.borrowbox.repository.MemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carries out a loan from end to end: check the borrower can pay, draw up the
 * contract, move the credits, and file it against the item and both members.
 *
 * <p>This exists so the money moves in exactly one place. Before, credits were
 * being touched by the Contract constructor and again by every member the
 * contract was filed against, which charged the same loan three times over.
 */
@Service
public class LendingService {

  private final ContractRepository contracts;
  private final MemberRepository members;
  private final ClockService clock;
  private final EventPublisher events;

  /**
   * Creates the service over its repositories, the clock and the event stream.
   */
  public LendingService(ContractRepository contracts, MemberRepository members, ClockService clock,
      EventPublisher events) {
    this.contracts = contracts;
    this.members = members;
    this.clock = clock;
    this.events = events;
  }

  /**
   * Lends an item to a borrower for the given period.
   *
   * <p>Runs in one transaction, so a loan that fails halfway leaves no trace:
   * either both balances move and the contract is filed, or neither happens.
   *
   * @throws LendingNotAllowedException if the loan breaks a lending rule or the
   *     borrower cannot cover the cost
   */
  @Transactional
  public Contract lend(Item item, Member borrower, int startDay, int endDay) {
    Contract contract = Contract.create(item, borrower, startDay, endDay, clock.today());

    if (borrower.getCredits() < contract.getCost()) {
      throw new LendingNotAllowedException(
          "The borrower has " + borrower.getCredits()
              + " credits but this loan costs " + contract.getCost() + ".");
    }

    Member lender = contract.getLender();
    borrower.deductCredits(contract.getCost());
    lender.addCredits(contract.getCost());

    // Nothing is attached to the item until the loan is certain to go ahead,
    // so a refusal cannot leave a phantom booking on its calendar.
    item.addContract(contract);
    Contract saved = contracts.save(contract);
    members.save(borrower);
    members.save(lender);

    events.publish(new DomainEvent(clock.today(), EventType.LOAN_AGREED,
        borrower.getName() + " books " + item.getItemName() + " from " + lender.getName()
            + " for days " + saved.getStartDay() + " to " + saved.getEndDay()
            + ", costing " + saved.getCost() + " credits."));

    return saved;
  }

  @Transactional(readOnly = true)
  public List<Contract> findAll() {
    return contracts.findAll();
  }

  @Transactional(readOnly = true)
  public List<Contract> findFor(String memberId) {
    return contracts.findByLenderMemberIdOrBorrowerMemberId(memberId, memberId);
  }
}
