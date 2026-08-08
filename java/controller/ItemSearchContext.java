package controller;

import java.util.List;
import model.Item;
import model.SearchStrategy;

/**
 * ItemSearchContext for the strategy pattern.
 */
public class ItemSearchContext {
  private SearchStrategy strategy;


  /**
   * Void setStrategy used as a constructor.
   */
  public void setStrategy(SearchStrategy strategy) {
    this.strategy = strategy;
  }

  public List<Item> executeSearch(List<Item> items, String criterion) {
    return strategy.search(items, criterion);
  }
}
