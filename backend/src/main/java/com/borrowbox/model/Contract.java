package com.borrowbox.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An agreement to lend one item to one member for a fixed run of days, both
 * ends inclusive.
 *
 * <p>Contracts are only ever built through {@link #create}, which throws rather
 * than hand back a loan that breaks the rules. A Contract that exists is a loan
 * that was allowed to happen, so nothing downstream has to re-check it.
 */
public final class Contract {

  private final Item item;
  private final Member lender;
  private final Member borrower;
  private final int startDay;
  private final int endDay;
  private final int cost;
  private final List<Observer> observers = new ArrayList<>();

  private Contract(Item item, Member borrower, int startDay, int endDay) {
    this.item = item;
    this.lender = item.getOwner();
    this.borrower = borrower;
    this.startDay = startDay;
    this.endDay = endDay;
    this.cost = item.getCostDaily() * (endDay - startDay + 1);
  }

  /**
   * Draws up a loan of {@code item} to {@code borrower}, or explains why it
   * cannot happen. The lender is whoever owns the item.
   *
   * @throws LendingNotAllowedException if the period is nonsensical, already
   *     booked, or the borrower is trying to borrow from themselves
   */
  public static Contract create(Item item, Member borrower, int startDay, int endDay, Time time) {
    Objects.requireNonNull(item, "item");
    Objects.requireNonNull(borrower, "borrower");

    if (startDay < time.getCurrentDay()) {
      throw new LendingNotAllowedException("A loan cannot start in the past.");
    }
    if (endDay < startDay) {
      throw new LendingNotAllowedException("A loan cannot end before it starts.");
    }
    if (borrower == item.getOwner()) {
      throw new LendingNotAllowedException("A member cannot borrow their own item.");
    }
    if (!item.isAvailable(startDay, endDay)) {
      throw new LendingNotAllowedException("The item is already booked for part of that period.");
    }

    return new Contract(item, borrower, startDay, endDay);
  }

  public void attach(Observer observer) {
    observers.add(observer);
  }

  /**
   * Tells every attached observer that something happened to this loan.
   */
  public void notifyObservers(String message) {
    for (Observer observer : observers) {
      observer.update(message);
    }
  }

  /** How many days the item is on loan for, counting both ends. */
  public int getDurationInDays() {
    return endDay - startDay + 1;
  }

  /** Whether the loan is running on the given day. */
  public boolean isActiveOn(int day) {
    return day >= startDay && day <= endDay;
  }

  public Item getItem() {
    return item;
  }

  public Member getLender() {
    return lender;
  }

  public Member getBorrower() {
    return borrower;
  }

  public int getStartDay() {
    return startDay;
  }

  public int getEndDay() {
    return endDay;
  }

  /** Total credits the borrower pays the lender for the whole period. */
  public int getCost() {
    return cost;
  }
}
