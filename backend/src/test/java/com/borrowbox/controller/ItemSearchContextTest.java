package com.borrowbox.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.borrowbox.model.Item;
import com.borrowbox.model.SearchByMaxPrice;
import com.borrowbox.model.SearchByName;
import com.borrowbox.model.Time;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The strategy context: swapping the strategy has to swap the behaviour.
 */
class ItemSearchContextTest {

  private final Time time = new Time();
  private final Item drill = new Item("Cordless Drill", "18V", "Tools", 40, time);
  private final Item tent = new Item("Camping Tent", "Two person", "Outdoors", 25, time);
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
