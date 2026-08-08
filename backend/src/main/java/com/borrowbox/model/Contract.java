package com.borrowbox.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * An agreement to lend one item to one member for a fixed run of days, both
 * ends inclusive.
 *
 * <p>Contracts are only ever built through {@link #create}, which throws rather
 * than hand back a loan that breaks the rules. A Contract that exists is a loan
 * that was allowed to happen, so nothing downstream has to re-check it.
 *
 * <p>The fields are not final only because Hibernate has to be able to
 * construct one when reading a row. There is still no way to change a contract
 * after it exists: no setters, and the constructor is private.
 */
@Entity
@Table(name = "contracts")
public class Contract {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "lender_id", nullable = false)
  private Member lender;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "borrower_id", nullable = false)
  private Member borrower;

  private int startDay;
  private int endDay;
  private int cost;

  /** For Hibernate only. */
  protected Contract() {
  }

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
   * @param today the current simulated day
   * @throws LendingNotAllowedException if the period is nonsensical, already
   *     booked, or the borrower is trying to borrow from themselves
   */
  public static Contract create(Item item, Member borrower, int startDay, int endDay, int today) {
    Objects.requireNonNull(item, "item");
    Objects.requireNonNull(borrower, "borrower");

    if (startDay < today) {
      throw new LendingNotAllowedException("A loan cannot start in the past.");
    }
    if (endDay < startDay) {
      throw new LendingNotAllowedException("A loan cannot end before it starts.");
    }
    if (borrower.getMemberId().equals(item.getOwnerId())) {
      throw new LendingNotAllowedException("A member cannot borrow their own item.");
    }
    if (!item.isAvailable(startDay, endDay)) {
      throw new LendingNotAllowedException("The item is already booked for part of that period.");
    }

    return new Contract(item, borrower, startDay, endDay);
  }

  public Long getId() {
    return id;
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
