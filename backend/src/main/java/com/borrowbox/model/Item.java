package com.borrowbox.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Something a member owns and is willing to lend out.
 *
 * <p>An item always belongs to exactly one member. The only way to create one is
 * {@link Member#createItem}, which keeps that relationship impossible to forget.
 */
public class Item {

  private static final AlphaNumericGen ID_GENERATOR = new AlphaNumericGen();
  private static final int ID_LENGTH = 3;

  private final String itemId;
  private final Member owner;
  private final int dayCreation;
  private String name;
  private String description;
  private String category;
  private int costDaily;
  private final List<Contract> contracts = new ArrayList<>();

  /**
   * Lists a new item on behalf of its owner.
   */
  Item(String name, String description, String category, int costDaily, Member owner, Time time) {
    this.itemId = ID_GENERATOR.generateAlphaNum(ID_LENGTH);
    this.name = name;
    this.description = description;
    this.category = category;
    this.costDaily = costDaily;
    this.owner = Objects.requireNonNull(owner, "An item must have an owner");
    this.dayCreation = time.getCurrentDay();
  }

  /**
   * Replaces the details a member is allowed to edit. Identity, owner and
   * listing date are fixed for the life of the item.
   */
  public void changeItemInfo(String name, String description, String category, int costDaily) {
    this.name = name;
    this.description = description;
    this.category = category;
    this.costDaily = costDaily;
  }

  // Getters

  public String getItemName() {
    return name;
  }

  public String getItemId() {
    return itemId;
  }

  public Member getOwner() {
    return owner;
  }

  public String getOwnerId() {
    return owner.getMemberId();
  }

  public String getDescription() {
    return description;
  }

  public String getCategory() {
    return category;
  }

  public int getDayCreation() {
    return dayCreation;
  }

  public int getCostDaily() {
    return costDaily;
  }

  public List<Contract> getContracts() {
    return new ArrayList<>(contracts);
  }

  // Setters

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public void setCostDaily(int costDaily) {
    this.costDaily = costDaily;
  }

  public void addContract(Contract contract) {
    contracts.add(contract);
  }

  /**
   * Whether the item is free for the whole of the given period. Both ends are
   * inclusive: an item booked for days 2 to 4 is busy on day 4.
   */
  public boolean isAvailable(int startDate, int endDate) {
    for (Contract contract : contracts) {
      if (!(endDate < contract.getStartDay() || startDate > contract.getEndDay())) {
        return false;
      }
    }
    return true;
  }

}
