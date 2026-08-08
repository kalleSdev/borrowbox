package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Name matching, one half of the item search strategy.
 */
class SearchByNameTest {

  private final Time time = new Time();
  private final SearchStrategy strategy = new SearchByName();
  private final Member owner = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time.getCurrentDay());

  private final Item drill = owner.createItem("Cordless Drill", "18V", "Tools", 40, time.getCurrentDay());
  private final Item tent = owner.createItem("Camping Tent", "Two person", "Outdoors", 25, time.getCurrentDay());
  private final List<Item> catalogue = List.of(drill, tent);

  @Test
  @DisplayName("matches on a substring of the name")
  void matchesOnSubstring() {
    assertThat(strategy.search(catalogue, "drill")).containsExactly(drill);
  }

  @Test
  @DisplayName("ignores case on both sides")
  void ignoresCase() {
    assertThat(strategy.search(catalogue, "TENT")).containsExactly(tent);
  }

  @Test
  @DisplayName("returns nothing when no name matches")
  void returnsNothingWhenNoNameMatches() {
    assertThat(strategy.search(catalogue, "kayak")).isEmpty();
  }

  @Test
  @DisplayName("returns everything for an empty criterion")
  void returnsEverythingForAnEmptyCriterion() {
    assertThat(strategy.search(catalogue, "")).containsExactly(drill, tent);
  }
}
