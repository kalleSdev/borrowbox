package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Price filtering, the other half of the item search strategy.
 */
class SearchByMaxPriceTest {

  private final Time time = new Time();
  private final SearchStrategy strategy = new SearchByMaxPrice();

  private final Item drill = new Item("Cordless Drill", "18V", "Tools", 40, time);
  private final Item tent = new Item("Camping Tent", "Two person", "Outdoors", 25, time);
  private final List<Item> catalogue = List.of(drill, tent);

  @Test
  @DisplayName("keeps items at or below the ceiling")
  void keepsItemsAtOrBelowTheCeiling() {
    assertThat(strategy.search(catalogue, "25")).containsExactly(tent);
    assertThat(strategy.search(catalogue, "40")).containsExactly(drill, tent);
  }

  @Test
  @DisplayName("returns nothing when everything is too expensive")
  void returnsNothingWhenEverythingIsTooExpensive() {
    assertThat(strategy.search(catalogue, "10")).isEmpty();
  }
}
