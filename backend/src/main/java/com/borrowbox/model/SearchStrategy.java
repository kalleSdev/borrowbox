package com.borrowbox.model;

import java.util.List;

/**
 * Interface SearchStrategy used for the search feature.
 */
public interface SearchStrategy {
  List<Item> search(List<Item> items, String criterion);
}
