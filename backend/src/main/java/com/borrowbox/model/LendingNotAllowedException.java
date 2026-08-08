package com.borrowbox.model;

/**
 * Raised when a loan is asked for that the lending rules will not permit:
 * dates in the past, an item that is already spoken for, a borrower who cannot
 * cover the cost.
 *
 * <p>Carries a message written for the person who asked, not for a log file.
 */
public class LendingNotAllowedException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public LendingNotAllowedException(String reason) {
    super(reason);
  }
}
