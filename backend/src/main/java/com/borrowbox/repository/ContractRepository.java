package com.borrowbox.repository;

import com.borrowbox.model.Contract;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Loans, either all of them or the ones a given member is a party to.
 */
public interface ContractRepository extends JpaRepository<Contract, Long> {

  List<Contract> findByLenderMemberIdOrBorrowerMemberId(String lenderId, String borrowerId);
}
