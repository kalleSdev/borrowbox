package com.borrowbox.model;

/**
 * The kinds of thing that happen in the system and are worth telling someone
 * about. Kept as an enum rather than free text so a consumer can group, filter
 * or icon them without parsing sentences.
 */
public enum EventType {
  MEMBER_JOINED,
  ITEM_LISTED,
  LOAN_AGREED,
  LOAN_STARTED,
  LOAN_ENDED,
  DAY_ADVANCED
}
