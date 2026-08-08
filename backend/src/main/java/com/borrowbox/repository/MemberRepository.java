package com.borrowbox.repository;

import com.borrowbox.model.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Members, by id and by the two details that have to be unique.
 */
public interface MemberRepository extends JpaRepository<Member, String> {

  Optional<Member> findByEmailIgnoreCase(String email);

  Optional<Member> findByMobile(String mobile);
}
