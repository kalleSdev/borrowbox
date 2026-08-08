package com.borrowbox.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class implementing SearchStrategy. Used to check by max price.
 */
public class SearchByMaxPrice implements SearchStrategy {

  @Override
  public List<Item> search(List<Item> items, String criterion) {
    int maxPrice;
    try {
      maxPrice = Integer.parseInt(criterion);
    } catch (NumberFormatException e) {
      System.out.println("Invalid max price format");
      return List.of();
    }

    return items.stream()
        .filter(item -> item.getCostDaily() <= maxPrice)
        .collect(Collectors.toList());
  }
}
