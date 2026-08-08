package com.borrowbox.repository;

import com.borrowbox.model.Time;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The simulated clock. Exactly one row, always id 1.
 */
public interface ClockRepository extends JpaRepository<Time, Long> {

  Long SINGLETON_ID = 1L;
}
