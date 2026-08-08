package com.borrowbox.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Something that happened, on the day it happened.
 *
 * <p>Stored rather than only printed, so the activity feed can be read back at
 * any point and survives a restart along with the loans it describes.
 */
@Entity
@Table(name = "events")
public class DomainEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** "day" on its own is a reserved word in H2, hence the column name. */
  @Column(name = "event_day")
  private int day;

  @Enumerated(EnumType.STRING)
  private EventType type;

  private String description;

  /** For Hibernate only. */
  protected DomainEvent() {
  }

  /**
   * Records that something happened.
   *
   * @param day the simulated day it occurred on
   * @param type what kind of event it was
   * @param description a sentence fit to show a person
   */
  public DomainEvent(int day, EventType type, String description) {
    this.day = day;
    this.type = type;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public int getDay() {
    return day;
  }

  public EventType getType() {
    return type;
  }

  public String getDescription() {
    return description;
  }
}
