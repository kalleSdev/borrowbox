package com.borrowbox.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The details of an item a member is allowed to change. Not the owner, and not
 * the id.
 */
public record UpdateItemRequest(
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
