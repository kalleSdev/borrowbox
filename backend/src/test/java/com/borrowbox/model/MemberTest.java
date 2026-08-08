package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Members, their credit balance and the items they own.
 */
class MemberTest {

  private final Time time = new Time();

  private Member newMember() {
    return new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time);
  }

  @Test
  @DisplayName("starts with no credits and no items")
  void startsEmpty() {
    Member member = newMember();

    assertThat(member.getCredits()).isZero();
    assertThat(member.getOwnedItems()).isEmpty();
    assertThat(member.getContracts()).isEmpty();
  }

  @Test
  @DisplayName("records the day it joined")
  void recordsTheDayItJoined() {
    time.advanceDay();

    assertThat(newMember().getCreationDate()).isEqualTo(1);
  }

  @Test
  @DisplayName("adds and deducts credits")
  void addsAndDeductsCredits() {
    Member member = newMember();

    member.addCredits(120);
    member.deductCredits(45);

    assertThat(member.getCredits()).isEqualTo(75);
  }

  @Test
  @DisplayName("refuses a deduction that would overdraw the balance")
  void refusesToOverdraw() {
    Member member = newMember();
    member.addCredits(30);

    member.deductCredits(50);

    assertThat(member.getCredits()).isEqualTo(30);
  }

  @Test
  @DisplayName("earns a listing bonus for every item put up for loan")
  void earnsAListingBonusPerItem() {
    Member member = newMember();

    member.createItem("Cordless Drill", "18V", "Tools", 40, time);
    member.addItemToOwnedItems(new Item("Camping Tent", "Two person", "Outdoors", 25, time));

    assertThat(member.getCredits()).isEqualTo(200);
    assertThat(member.getOwnedItems()).hasSize(2);
  }

  @Test
  @DisplayName("finds and removes its own items by id")
  void findsAndRemovesItsOwnItemsById() {
    Member member = newMember();
    Item item = member.createItem("Cordless Drill", "18V", "Tools", 40, time);

    assertThat(member.getItemById(item.getItemId())).isSameAs(item);
    assertThat(member.deleteItemById(item.getItemId())).isTrue();
    assertThat(member.getOwnedItems()).isEmpty();
  }

  @Test
  @DisplayName("reports an unknown item id rather than throwing")
  void reportsAnUnknownItemId() {
    Member member = newMember();

    assertThat(member.getItemById("zzz")).isNull();
    assertThat(member.deleteItemById("zzz")).isFalse();
  }

  @Test
  @DisplayName("hands out a copy of its items, not the list itself")
  void handsOutACopyOfItsItems() {
    Member member = newMember();
    member.createItem("Cordless Drill", "18V", "Tools", 40, time);

    member.getOwnedItems().clear();

    assertThat(member.getOwnedItems()).hasSize(1);
  }
}
