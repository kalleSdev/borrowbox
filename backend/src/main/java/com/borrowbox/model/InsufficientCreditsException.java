package com.borrowbox.model;

/**
 * Raised when a member is asked to pay more credits than they hold.
 *
 * <p>A balance going negative would be a silent corruption of the ledger, so
 * this refuses loudly rather than letting it happen.
 */
public class InsufficientCreditsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InsufficientCreditsException(String reason) {
    super(reason);
  }
}
