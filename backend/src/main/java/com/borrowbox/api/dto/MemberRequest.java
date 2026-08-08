package com.borrowbox.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The details needed to sign a member up, or to change the ones they have.
 * Both operations take exactly the same fields.
 */
public record MemberRequest(
    @NotBlank(message = "A name is required")
    @Size(max = 80, message = "A name can be at most 80 characters")
    String name,

    @NotBlank(message = "An email address is required")
    @Email(message = "That does not look like an email address")
    String email,

    @NotBlank(message = "A mobile number is required")
    @Size(max = 30, message = "A mobile number can be at most 30 characters")
    String mobile) {
}
