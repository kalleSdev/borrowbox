package com.borrowbox.model;

/**
 * Carries out a loan from end to end: check the borrower can pay, draw up the
 * contract, move the credits, and file it against the item and both members.
 *
 * <p>This exists so the money moves in exactly one place. Before, credits were
 * being touched by the Contract constructor and again by every member the
 * contract was filed against, which charged the same loan three times over.
 */
public class LendingService {

  private final Time time;
  private final EventPublisher events;

  public LendingService(Time time, EventPublisher events) {
    this.time = time;
    this.events = events;
  }

  /**
   * Lends an item to a borrower for the given period.
   *
   * @throws LendingNotAllowedException if the loan breaks a lending rule or the
   *     borrower cannot cover the cost
   */
  public Contract lend(Item item, Member borrower, int startDay, int endDay) {
    Contract contract = Contract.create(item, borrower, startDay, endDay, time);

    if (borrower.getCredits() < contract.getCost()) {
      throw new LendingNotAllowedException(
          "The borrower has " + borrower.getCredits()
              + " credits but this loan costs " + contract.getCost() + ".");
    }

    Member lender = contract.getLender();
    borrower.deductCredits(contract.getCost());
    lender.addCredits(contract.getCost());

    item.addContract(contract);
    lender.addContract(contract);
    borrower.addContract(contract);

    events.publish(new DomainEvent(time.getCurrentDay(), EventType.LOAN_AGREED,
        borrower.getName() + " books " + item.getItemName() + " from " + lender.getName()
            + " for days " + contract.getStartDay() + " to " + contract.getEndDay()
            + ", costing " + contract.getCost() + " credits."));

    return contract;
  }
}
