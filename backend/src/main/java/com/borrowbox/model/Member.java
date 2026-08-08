package com.borrowbox.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Somebody using the system: what they own, and what they can afford.
 */
@Entity
@Table(name = "members")
public class Member {

  /** Credits awarded once for putting an item up for loan. */
  public static final int LISTING_BONUS = 100;

  @Id
  private String memberId;

  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String mobile;

  private int credits;

  /** The simulated day this member signed up on. */
  private int joinedOnDay;

  // Eager because open-in-view is off and the API describes a member by how
  // many items they own. The dataset is small enough that this costs nothing;
  // it would want revisiting before anyone owned thousands of things.
  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<Item> ownedItems = new ArrayList<>();

  /** For Hibernate only. */
  protected Member() {
  }

  /**
   * Creates a member. Going through {@link MemberService#register} instead is
   * what applies the uniqueness rules.
   */
  public Member(String name, String email, String mobile, String memberId, int joinedOnDay) {
    this.memberId = memberId;
    this.name = name;
    this.email = email;
    this.mobile = mobile;
    this.credits = 0;
    this.joinedOnDay = joinedOnDay;
  }

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

  public int getCredits() {
    return credits;
  }

  public int getJoinedOnDay() {
    return joinedOnDay;
  }

  public List<Item> getOwnedItems() {
    return new ArrayList<>(ownedItems);
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

  public void addCredits(int amount) {
    credits += amount;
  }

  /**
   * Takes credits off the balance.
   *
   * @throws InsufficientCreditsException if the balance will not cover it
   */
  public void deductCredits(int amount) {
    if (credits < amount) {
      throw new InsufficientCreditsException(
          name + " has " + credits + " credits but " + amount + " are needed.");
    }
    credits -= amount;
  }

  /**
   * Lists a new item owned by this member. Listing something earns the owner a
   * one-off bonus, which is what keeps credits flowing into the system.
   */
  public Item createItem(String itemName, String itemDescription, String itemCategory,
      int itemCostDaily, int today) {
    Item newItem = new Item(itemName, itemDescription, itemCategory, itemCostDaily, this, today);
    ownedItems.add(newItem);
    addCredits(LISTING_BONUS);
    return newItem;
  }

  /**
   * Removes one of this member's items.
   *
   * @return whether they owned an item with that id
   */
  public boolean deleteItemById(String itemId) {
    return ownedItems.removeIf(item -> item.getItemId().equals(itemId));
  }

  /**
   * Finds one of this member's items, or null if they do not own it.
   */
  public Item getItemById(String itemId) {
    for (Item item : ownedItems) {
      if (item.getItemId().equals(itemId)) {
        return item;
      }
    }
    return null;
  }
}
