package com.borrowbox.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating the contract.
 */
public class Contract {

  // attributes
  private int startDate;
  private int endDate;
  private Item item;
  private Member lender;
  private Member borrower;
  private boolean status;
  private int cost;
  private Time time;
  private List<Observer> observers = new ArrayList<>();

  /**
   * Contract constructor.
   */
  public Contract(Item item, Member lender, Member borrower, int startDate, int endDate, Time time) {
    if (startDate < time.getCurrentDay() || endDate < startDate) {
      System.out.println("Invalid contract dates.");
      return;
    }

    // Check if the lender has enough credits
    if (lender.getCredits() < item.getCostDaily() * (endDate - startDate + 1)) {
      System.out.println("The lender does not have enough credits.");
      return;
    }

    // Check if the item is available during the specified time period
    if (!item.isAvailable(startDate, endDate)) {
      System.out.println("The item is not available during the specified time period.");
      return;
    }

    // Initialize the contract
    this.item = item;
    this.startDate = startDate;
    this.endDate = endDate;
    this.lender = lender;
    this.borrower = borrower;
    this.status = true;
    this.cost = item.getCostDaily() * (endDate - startDate + 1);
    this.time = time;

    // Notify observers when a contract is created (and thus, takes effect)
    notifyObservers("Contract for " + item.getItemName() + " has taken effect.");

    // Deduct credits from the lender
    lender.deductCredits(this.cost);
  }

  public void attach(Observer observer) {
    observers.add(observer);
  }

  /**
   * Used to notify observers.
   */
  public void notifyObservers(String message) {
    for (Observer observer : observers) {
      observer.update(message);
    }
  }

  /**
   * checks the validity.
   */
  public boolean isValid() {
    // check if item is null.
    if (this.item == null) {
      return false;
    }
    // Check if the lender has enough credits
    int totalCost = item.getCostDaily() * (endDate - startDate + 1);
    if (lender.getCredits() < totalCost) {
      return false;
    }

    // Check if the item is available during the specified period
    if (!item.isAvailable(startDate, endDate)) {
      return false;
    }

    // Check if the start date is before or equal to the end date
    if (startDate > endDate) {
      return false;
    }

    return true;
  }

  // getters

  public Item getItem() {
    return item;
  }

  public boolean getStatus() {
    return status;
  }

  public int getEndDate() {
    return endDate;
  }

  public int getStartDate() {
    return startDate;
  }

  public Member getborrower() {
    return borrower;
  }

  public Member getOwner() {
    return lender; // Assuming Member has a copy constructor
  }

  // setters

  public void setItem(Item item) {
    this.item = item;
  }

  public void setEndDate(int endDate) {
    this.endDate = endDate;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

}
