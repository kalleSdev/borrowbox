package com.borrowbox.model;

/**
 * Raised when an email address or mobile number is already spoken for by
 * another member. Both have to identify exactly one person.
 */
public class MemberAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MemberAlreadyExistsException(String reason) {
    super(reason);
  }
}
