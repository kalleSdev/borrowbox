package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Items, and the booking calendar each one carries around.
 */
class ItemTest {

  private final Time time = new Time();

  @Test
  @DisplayName("is given a short generated id")
  void isGivenAShortGeneratedId() {
    Item item = new Item("Cordless Drill", "18V", "Tools", 40, time);

    assertThat(item.getItemId()).hasSize(3).matches("[a-zA-Z0-9]+");
  }

  @Test
  @DisplayName("records the day it was listed")
  void recordsTheDayItWasListed() {
    time.advanceDay();
    time.advanceDay();

    Item item = new Item("Cordless Drill", "18V", "Tools", 40, time);

    assertThat(item.getDayCreation()).isEqualTo(2);
  }

  @Test
  @DisplayName("takes new details without changing its id")
  void takesNewDetailsWithoutChangingItsId() {
    Item item = new Item("Cordless Drill", "18V", "Tools", 40, time);
    String originalId = item.getItemId();

    item.changeItemInfo("Hammer Drill", "SDS plus", "Power Tools", 55);

    assertThat(item.getItemName()).isEqualTo("Hammer Drill");
    assertThat(item.getDescription()).isEqualTo("SDS plus");
    assertThat(item.getCategory()).isEqualTo("Power Tools");
    assertThat(item.getCostDaily()).isEqualTo(55);
    assertThat(item.getItemId()).isEqualTo(originalId);
  }

  @Test
  @DisplayName("hands out a copy of its contracts, not the list itself")
  void handsOutACopyOfItsContracts() {
    Item item = new Item("Cordless Drill", "18V", "Tools", 40, time);

    item.getContracts().clear();

    assertThat(item.getContracts()).isEmpty();
  }

  @Test
  @DisplayName("is free for any period while it has no bookings")
  void isFreeWhileItHasNoBookings() {
    Item item = new Item("Cordless Drill", "18V", "Tools", 40, time);

    assertThat(item.isAvailable(0, 100)).isTrue();
  }

  @Test
  @DisplayName("is unavailable for a period that overlaps a booking")
  void isUnavailableForAnOverlappingPeriod() {
    Item item = bookedItem();

    assertThat(item.isAvailable(3, 5)).isFalse();
  }

  @Test
  @DisplayName("is available again once a booking has ended")
  void isAvailableAgainOnceABookingHasEnded() {
    Item item = bookedItem();

    assertThat(item.isAvailable(5, 6)).isTrue();
  }

  /** An item with a single booking running from day 2 to day 4. */
  private Item bookedItem() {
    Item item = new Item("Cordless Drill", "18V", "Tools", 10, time);
    Member lender = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time);
    Member borrower = new Member("Linus", "linus@example.com", "0700000002", "bbbbbb", time);
    lender.addCredits(500);

    item.addContract(new Contract(item, lender, borrower, 2, 4, time));
    return item;
  }
}
