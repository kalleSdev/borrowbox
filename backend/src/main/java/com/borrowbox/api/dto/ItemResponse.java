package com.borrowbox.api.dto;

import com.borrowbox.model.Item;
import java.util.List;

/**
 * An item in the catalogue, with the loans booked against it.
 *
 * <p>{@code availableToday} is worked out against the current simulated day so
 * a catalogue can be rendered without the client knowing the booking rules.
 */
public record ItemResponse(
    String id,
    String name,
    String description,
    String category,
    int costPerDay,
    String ownerId,
    String ownerName,
    int listedOnDay,
    boolean availableToday,
    List<ContractResponse> contracts) {

  /**
   * Describes an item for the API as of {@code today}.
   */
  public static ItemResponse from(Item item, int today) {
    return new ItemResponse(
        item.getItemId(),
        item.getItemName(),
        item.getDescription(),
        item.getCategory(),
        item.getCostDaily(),
        item.getOwner().getMemberId(),
        item.getOwner().getName(),
        item.getDayCreation(),
        item.isAvailable(today, today),
        item.getContracts().stream().map(ContractResponse::from).toList());
  }
}
