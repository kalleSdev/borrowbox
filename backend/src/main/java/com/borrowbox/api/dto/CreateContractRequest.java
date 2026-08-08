package com.borrowbox.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * A request to borrow an item for a run of days.
 *
 * <p>There is no lender field: the lender is whoever owns the item, and letting
 * a client name a different one would only be a way to get it wrong.
 */
public record CreateContractRequest(
    @NotBlank(message = "An item is required")
    String itemId,

    @NotBlank(message = "A borrower is required")
    String borrowerId,

    @Min(value = 0, message = "A start day cannot be negative")
    int startDay,

    @Min(value = 0, message = "An end day cannot be negative")
    int endDay) {
}
