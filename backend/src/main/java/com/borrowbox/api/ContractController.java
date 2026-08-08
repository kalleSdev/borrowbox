package com.borrowbox.api;

import com.borrowbox.api.dto.ContractResponse;
import com.borrowbox.api.dto.CreateContractRequest;
import com.borrowbox.model.Contract;
import com.borrowbox.model.Item;
import com.borrowbox.model.LendingService;
import com.borrowbox.model.Member;
import com.borrowbox.model.MemberList;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Loans: agreeing them, and reading back the ones on the books.
 */
@RestController
@RequestMapping("/api/contracts")
public class ContractController {

  private final MemberList members;
  private final LendingService lending;

  public ContractController(MemberList members, LendingService lending) {
    this.members = members;
    this.lending = lending;
  }

  /**
   * Every loan ever agreed, optionally only those involving one member as
   * either the lender or the borrower.
   */
  @GetMapping
  public List<ContractResponse> list(@RequestParam(required = false) String memberId) {
    return members.getAllItems().stream()
        .flatMap(item -> item.getContracts().stream())
        .filter(contract -> memberId == null || involves(contract, memberId))
        .map(ContractResponse::from)
        .toList();
  }

  /**
   * Books an item out. Every rule about whether this is allowed lives in the
   * model; a refusal arrives here as an exception and leaves as a 422.
   */
  @PostMapping
  public ResponseEntity<ContractResponse> create(@Valid @RequestBody CreateContractRequest request) {
    Item item = members.requireItemById(request.itemId());
    Member borrower = members.requireMemberById(request.borrowerId());

    Contract contract = lending.lend(item, borrower, request.startDay(), request.endDay());

    return ResponseEntity.status(HttpStatus.CREATED).body(ContractResponse.from(contract));
  }

  private static boolean involves(Contract contract, String memberId) {
    return contract.getLender().getMemberId().equals(memberId)
        || contract.getBorrower().getMemberId().equals(memberId);
  }
}
