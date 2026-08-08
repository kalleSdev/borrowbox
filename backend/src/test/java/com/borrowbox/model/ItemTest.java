package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Items, and the booking calendar each one carries around.
 */
class ItemTest {

  private final Time time = new Time();
  private final Member owner = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time);

  private Item drill() {
    return owner.createItem("Cordless Drill", "18V", "Tools", 40);
  }

  @Test
  @DisplayName("is given a short generated id")
  void isGivenAShortGeneratedId() {
    assertThat(drill().getItemId()).hasSize(3).matches("[a-zA-Z0-9]+");
  }

  @Test
  @DisplayName("belongs to the member who listed it")
  void belongsToTheMemberWhoListedIt() {
    Item item = drill();

    assertThat(item.getOwner()).isSameAs(owner);
    assertThat(item.getOwnerId()).isEqualTo("aaaaaa");
  }

  @Test
  @DisplayName("records the day it was listed")
  void recordsTheDayItWasListed() {
    time.advanceDay();
    time.advanceDay();

    assertThat(drill().getDayCreation()).isEqualTo(2);
  }

  @Test
  @DisplayName("takes new details without changing its id or owner")
  void takesNewDetailsWithoutChangingItsIdOrOwner() {
    Item item = drill();
    String originalId = item.getItemId();

    item.changeItemInfo("Hammer Drill", "SDS plus", "Power Tools", 55);

    assertThat(item.getItemName()).isEqualTo("Hammer Drill");
    assertThat(item.getDescription()).isEqualTo("SDS plus");
    assertThat(item.getCategory()).isEqualTo("Power Tools");
    assertThat(item.getCostDaily()).isEqualTo(55);
    assertThat(item.getItemId()).isEqualTo(originalId);
    assertThat(item.getOwner()).isSameAs(owner);
  }

  @Test
  @DisplayName("hands out a copy of its contracts, not the list itself")
  void handsOutACopyOfItsContracts() {
    Item item = drill();

    item.getContracts().clear();

    assertThat(item.getContracts()).isEmpty();
  }

  @Test
  @DisplayName("is free for any period while it has no bookings")
  void isFreeWhileItHasNoBookings() {
    assertThat(drill().isAvailable(0, 100)).isTrue();
  }

  @Test
  @DisplayName("is unavailable for a period that overlaps a booking")
  void isUnavailableForAnOverlappingPeriod() {
    assertThat(bookedItem().isAvailable(3, 5)).isFalse();
  }

  @Test
  @DisplayName("is unavailable on the last day of a booking")
  void isUnavailableOnTheLastDayOfABooking() {
    assertThat(bookedItem().isAvailable(4, 7)).isFalse();
  }

  @Test
  @DisplayName("is available again once a booking has ended")
  void isAvailableAgainOnceABookingHasEnded() {
    assertThat(bookedItem().isAvailable(5, 6)).isTrue();
  }

  /** An item with a single booking running from day 2 to day 4 inclusive. */
  private Item bookedItem() {
    Item item = owner.createItem("Cordless Drill", "18V", "Tools", 10);
    Member borrower = new Member("Linus", "linus@example.com", "0700000002", "bbbbbb", time);

    item.addContract(Contract.create(item, borrower, 2, 4, time));
    return item;
  }
}
