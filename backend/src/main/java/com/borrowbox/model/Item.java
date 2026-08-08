package com.borrowbox.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Something a member owns and is willing to lend out.
 *
 * <p>An item always belongs to exactly one member. The only way to create one
 * is {@link Member#createItem}, which keeps that impossible to forget.
 */
@Entity
@Table(name = "items")
public class Item {

  private static final AlphaNumericGen ID_GENERATOR = new AlphaNumericGen();
  private static final int ID_LENGTH = 3;

  @Id
  private String itemId;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "owner_id", nullable = false)
  private Member owner;

  /** The simulated day this item was put up for loan. */
  private int listedOnDay;

  private String name;
  private String description;
  private String category;
  private int costDaily;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<Contract> contracts = new ArrayList<>();

  /** For Hibernate only. */
  protected Item() {
  }

  Item(String name, String description, String category, int costDaily, Member owner, int today) {
    this.itemId = ID_GENERATOR.generateAlphaNum(ID_LENGTH);
    this.name = name;
    this.description = description;
    this.category = category;
    this.costDaily = costDaily;
    this.owner = Objects.requireNonNull(owner, "An item must have an owner");
    this.listedOnDay = today;
  }

  /**
   * Replaces the details a member is allowed to edit. Identity, owner and
   * listing day are fixed for the life of the item.
   */
  public void changeItemInfo(String name, String description, String category, int costDaily) {
    this.name = name;
    this.description = description;
    this.category = category;
    this.costDaily = costDaily;
  }

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

  public int getListedOnDay() {
    return listedOnDay;
  }

  public int getCostDaily() {
    return costDaily;
  }

  public List<Contract> getContracts() {
    return new ArrayList<>(contracts);
  }

  /**
   * Files a loan against this item's calendar. Both sides of the association
   * have to be set for the item to know it is busy before the next read.
   */
  public void addContract(Contract contract) {
    contracts.add(contract);
  }

  /**
   * Whether the item is free for the whole of the given period. Both ends are
   * inclusive: an item booked for days 2 to 4 is busy on day 4.
   */
  public boolean isAvailable(int startDay, int endDay) {
    for (Contract contract : contracts) {
      if (!(endDay < contract.getStartDay() || startDay > contract.getEndDay())) {
        return false;
      }
    }
    return true;
  }
}
