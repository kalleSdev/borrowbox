package com.borrowbox.api.dto;

import com.borrowbox.model.Member;

/**
 * A member as the API describes them.
 *
 * <p>Separate from the Member class on purpose: the domain object holds
 * references to its items and contracts, which would serialise into a cycle,
 * and the shape a client depends on should not change every time the model does.
 */
public record MemberResponse(
    String id,
    String name,
    String email,
    String mobile,
    int credits,
    int joinedOnDay,
    int ownedItemCount) {

  /**
   * Describes a member for the API.
   */
  public static MemberResponse from(Member member) {
    return new MemberResponse(
        member.getMemberId(),
        member.getName(),
        member.getEmail(),
        member.getMobile(),
        member.getCredits(),
        member.getJoinedOnDay(),
        member.getOwnedItems().size());
  }
}
