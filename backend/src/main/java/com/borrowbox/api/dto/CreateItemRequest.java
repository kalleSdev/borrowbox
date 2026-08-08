package com.borrowbox.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * What is needed to put an item up for loan. The owner is set once here and
 * cannot be changed afterwards, which is why editing uses a different shape.
 */
public record CreateItemRequest(
    @NotBlank(message = "An owner is required")
    String ownerId,

    @NotBlank(message = "A name is required")
    @Size(max = 80, message = "A name can be at most 80 characters")
    String name,

    @NotBlank(message = "A description is required")
    @Size(max = 500, message = "A description can be at most 500 characters")
    String description,

    @NotBlank(message = "A category is required")
    @Size(max = 40, message = "A category can be at most 40 characters")
    String category,

    @Min(value = 0, message = "A daily cost cannot be negative")
    int costPerDay) {
}
