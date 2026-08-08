package com.borrowbox.model;

/**
 * Raised when something is asked for by id and there is no such thing.
 */
public class NotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public NotFoundException(String reason) {
    super(reason);
  }
}
