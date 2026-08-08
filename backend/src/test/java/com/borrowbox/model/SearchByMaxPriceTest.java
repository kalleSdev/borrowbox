package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Price filtering, the other half of the item search strategy.
 */
class SearchByMaxPriceTest {

  private final Time time = new Time();
  private final SearchStrategy strategy = new SearchByMaxPrice();
  private final Member owner = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time.getCurrentDay());

  private final Item drill = owner.createItem("Cordless Drill", "18V", "Tools", 40, time.getCurrentDay());
  private final Item tent = owner.createItem("Camping Tent", "Two person", "Outdoors", 25, time.getCurrentDay());
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

  @Test
  @DisplayName("ignores surrounding whitespace")
  void ignoresSurroundingWhitespace() {
    assertThat(strategy.search(catalogue, "  25 ")).containsExactly(tent);
  }

  @Test
  @DisplayName("complains about a criterion that is not a number")
  void complainsAboutANonNumericCriterion() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> strategy.search(catalogue, "cheap"))
        .withMessageContaining("\"cheap\" is not a number of credits");
  }
}
