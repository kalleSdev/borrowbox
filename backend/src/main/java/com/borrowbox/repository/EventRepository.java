package com.borrowbox.repository;

import com.borrowbox.model.DomainEvent;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The activity feed. Events are only ever appended, so the generated id is
 * also the order they happened in.
 */
public interface EventRepository extends JpaRepository<DomainEvent, Long> {

  List<DomainEvent> findAllByOrderByIdAsc();

  List<DomainEvent> findAllByOrderByIdDesc(Pageable pageable);
}
