package com.borrowbox.service;

import com.borrowbox.model.Time;
import com.borrowbox.repository.ClockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and writes the one clock row.
 *
 * <p>Everything else asks this what day it is rather than holding a clock of
 * its own, so there is a single answer and it is the one on disk.
 */
@Service
public class ClockService {

  private final ClockRepository clocks;

  public ClockService(ClockRepository clocks) {
    this.clocks = clocks;
  }

  /** The clock, creating it on first use. */
  @Transactional
  public Time clock() {
    return clocks.findById(ClockRepository.SINGLETON_ID).orElseGet(() -> clocks.save(new Time()));
  }

  @Transactional(readOnly = true)
  public int today() {
    return clocks.findById(ClockRepository.SINGLETON_ID).map(Time::getCurrentDay).orElse(0);
  }
}
