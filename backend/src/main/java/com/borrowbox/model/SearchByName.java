package com.borrowbox.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class SearchByName implementing SearchStrategy. Used to search by the name.
 */
public class SearchByName implements SearchStrategy {

  @Override
  public List<Item> search(List<Item> items, String criterion) {
    return items.stream()
        .filter(item -> item.getItemName().toLowerCase().contains(criterion.toLowerCase()))
        .collect(Collectors.toList());
  }
}
