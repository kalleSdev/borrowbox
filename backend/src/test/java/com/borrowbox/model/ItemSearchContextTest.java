package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The strategy context: swapping the strategy has to swap the behaviour.
 */
class ItemSearchContextTest {

  private final Time time = new Time();
  private final Member owner = new Member("Ada", "ada@example.com", "0700000001", "aaaaaa", time);
  private final Item drill = owner.createItem("Cordless Drill", "18V", "Tools", 40);
  private final Item tent = owner.createItem("Camping Tent", "Two person", "Outdoors", 25);
  private final List<Item> catalogue = List.of(drill, tent);

  @Test
  @DisplayName("delegates to whichever strategy is currently set")
  void delegatesToTheCurrentStrategy() {
    ItemSearchContext context = new ItemSearchContext();

    context.setStrategy(new SearchByName());
    assertThat(context.executeSearch(catalogue, "drill")).containsExactly(drill);

    context.setStrategy(new SearchByMaxPrice());
    assertThat(context.executeSearch(catalogue, "30")).containsExactly(tent);
  }
}
