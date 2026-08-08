package com.borrowbox.api.dto;

import com.borrowbox.model.Contract;

/**
 * A loan as the API describes it. Names are included alongside ids so a client
 * can render a sentence without fetching both members first.
 */
public record ContractResponse(
    String itemId,
    String itemName,
    String lenderId,
    String lenderName,
    String borrowerId,
    String borrowerName,
    int startDay,
    int endDay,
    int durationInDays,
    int cost) {

  /**
   * Describes a loan for the API.
   */
  public static ContractResponse from(Contract contract) {
    return new ContractResponse(
        contract.getItem().getItemId(),
        contract.getItem().getItemName(),
        contract.getLender().getMemberId(),
        contract.getLender().getName(),
        contract.getBorrower().getMemberId(),
        contract.getBorrower().getName(),
        contract.getStartDay(),
        contract.getEndDay(),
        contract.getDurationInDays(),
        contract.getCost());
  }
}
