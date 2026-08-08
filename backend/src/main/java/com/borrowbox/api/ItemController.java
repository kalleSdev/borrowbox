package com.borrowbox.api;

import com.borrowbox.api.dto.CreateItemRequest;
import com.borrowbox.api.dto.ItemResponse;
import com.borrowbox.api.dto.UpdateItemRequest;
import com.borrowbox.model.Item;
import com.borrowbox.model.ItemSearchContext;
import com.borrowbox.model.Member;
import com.borrowbox.model.MemberList;
import com.borrowbox.model.SearchByMaxPrice;
import com.borrowbox.model.SearchByName;
import com.borrowbox.model.Time;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The catalogue: everything anyone is willing to lend.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

  private final MemberList members;
  private final Time time;

  public ItemController(MemberList members, Time time) {
    this.members = members;
    this.time = time;
  }

  /**
   * Lists the catalogue, optionally filtered.
   *
   * <p>Each filter is a search strategy; the context is handed a different one
   * per parameter and applied in turn, so passing both narrows by both.
   */
  @GetMapping
  public List<ItemResponse> list(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String maxPrice) {

    List<Item> results = members.getAllItems();
    ItemSearchContext search = new ItemSearchContext();

    if (name != null && !name.isBlank()) {
      search.setStrategy(new SearchByName());
      results = search.executeSearch(results, name);
    }
    if (maxPrice != null && !maxPrice.isBlank()) {
      search.setStrategy(new SearchByMaxPrice());
      results = search.executeSearch(results, maxPrice);
    }

    return describe(results);
  }

  @GetMapping("/{id}")
  public ItemResponse get(@PathVariable String id) {
    return ItemResponse.from(members.requireItemById(id), time.getCurrentDay());
  }

  /**
   * Puts a new item up for loan on behalf of its owner.
   */
  @PostMapping
  public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
    Member owner = members.requireMemberById(request.ownerId());
    Item item = owner.createItem(
        request.name(), request.description(), request.category(), request.costPerDay());

    return ResponseEntity
        .created(URI.create("/api/items/" + item.getItemId()))
        .body(ItemResponse.from(item, time.getCurrentDay()));
  }

  @PutMapping("/{id}")
  public ItemResponse update(@PathVariable String id, @Valid @RequestBody UpdateItemRequest request) {
    Item item = members.requireItemById(id);
    item.changeItemInfo(request.name(), request.description(), request.category(), request.costPerDay());
    return ItemResponse.from(item, time.getCurrentDay());
  }

  /**
   * Takes an item off the catalogue.
   *
   * <p>Refused if anyone has ever booked it, because removing the item would
   * take the record of those loans with it.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    Item item = members.requireItemById(id);
    if (!item.getContracts().isEmpty()) {
      throw new IllegalStateException(
          item.getItemName() + " has loans booked against it and cannot be removed.");
    }

    item.getOwner().deleteItemById(id);
    return ResponseEntity.noContent().build();
  }

  private List<ItemResponse> describe(List<Item> items) {
    int today = time.getCurrentDay();
    return items.stream().map(item -> ItemResponse.from(item, today)).toList();
  }
}
