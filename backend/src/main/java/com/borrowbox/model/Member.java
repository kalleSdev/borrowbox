package com.borrowbox.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating a member.
 */
public class Member {

  /** Credits awarded once for putting an item up for loan. */
  public static final int LISTING_BONUS = 100;

  private String memberId;
  private String name;
  private String email;
  private String mobile;
  private float credits;
  private List<Item> ownedItems;
  private Integer creationDate;
  private List<Contract> currentContracts;
  private Time time;

  /**
   * Member constructor.
   */
  public Member(String name, String email, String mobile, String memberId, Time time) {
    this.memberId = memberId;
    this.name = name;
    this.email = email;
    this.mobile = mobile;
    this.credits = 0;
    this.ownedItems = new ArrayList<>();
    this.currentContracts = new ArrayList<>();
    this.time = time;

    this.creationDate = time.getCurrentDay();
  }

  // Getters
  public String getMemberId() {
    return memberId;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getMobile() {
    return mobile;
  }

  public float getCredits() {
    return credits;
  }

  public List<Item> getOwnedItems() {
    return new ArrayList<>(ownedItems);
  }

  public Integer getCreationDate() {
    return creationDate;
  }

  public List<Contract> getContracts() {
    return new ArrayList<>(currentContracts);
  }

  // setters
  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public void setCredits(float credits) {
    this.credits = credits;
  }

  public void setOwnedItems(List<Item> ownedItems) {
    this.ownedItems = new ArrayList<>(ownedItems);
  }

  public void setCreationDate(Integer creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Add credits.
   */
  public void addCredits(float amount) {
    credits += amount;
  }

  /**
   * Deduct credits.
   */
  public void deductCredits(float amount) {
    if (credits >= amount) {
      credits -= amount;
    } else {
      System.out.println("Insufficient credits.");
    }
  }

  /**
   * Deletion of items.
   */
  public boolean deleteItemById(String itemId) {
    Item itemToDelete = null;
    for (Item item : ownedItems) {
      if (item.getItemId().equals(itemId)) {
        itemToDelete = item;
        break;
      }
    }

    if (itemToDelete != null) {
      ownedItems.remove(itemToDelete);
      return true; // Item deleted successfully
    }

    return false; // Item not found
  }

  /**
   * Lists a new item owned by this member. Listing something earns the owner a
   * one-off bonus, which is what keeps credits flowing into the system.
   */
  public Item createItem(String itemName, String itemDescription, String itemCategory, int itemCostDaily) {
    Item newItem = new Item(itemName, itemDescription, itemCategory, itemCostDaily, this, time);
    ownedItems.add(newItem);
    addCredits(LISTING_BONUS);
    return newItem;
  }

  /**
   * update member info.
   */
  public void updateMemberInformation(String name, String email, String mobile) {
    this.name = name;
    this.email = email;
    this.mobile = mobile;
  }

  /**
   * Files a contract against this member. Recording only: the credits for a
   * loan are moved once, by {@link LendingService}.
   */
  public void addContract(Contract contract) {
    currentContracts.add(contract);
  }

  /**
   * gets an item by its id.
   */
  public Item getItemById(String itemId) {
    for (Item item : ownedItems) {
      if (item.getItemId().equals(itemId)) {
        return item; // Return the item if the ID matches
      }
    }
    return null; // Return null if no item found with the given ID
  }

}
