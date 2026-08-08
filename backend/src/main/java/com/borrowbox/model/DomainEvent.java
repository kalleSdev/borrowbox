package com.borrowbox.model;

/**
 * Something that happened, on the day it happened.
 *
 * @param day the simulated day the event occurred on
 * @param type what kind of event it was
 * @param description a sentence fit to show a person
 */
public record DomainEvent(int day, EventType type, String description) {
}
