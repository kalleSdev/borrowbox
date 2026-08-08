package com.borrowbox.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps the items costing no more than a given number of credits per day.
 */
public class SearchByMaxPrice implements SearchStrategy {

  @Override
  public List<Item> search(List<Item> items, String criterion) {
    int maxPrice;
    try {
      maxPrice = Integer.parseInt(criterion.trim());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("\"" + criterion + "\" is not a number of credits.", e);
    }

    return items.stream()
        .filter(item -> item.getCostDaily() <= maxPrice)
        .collect(Collectors.toList());
  }
}
