package com.borrowbox.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The register of everyone using the system.
 *
 * <p>Every member is created through {@link #register}, which is the only place
 * the uniqueness rules on email and mobile are applied. There is deliberately no
 * way to hand this class a Member that was built somewhere else.
 */
public class MemberList implements Persistence {

  private static final int MEMBER_ID_LENGTH = 6;
  private static final AlphaNumericGen ID_GENERATOR = new AlphaNumericGen();

  private final Time time;
  private final List<Member> members = new ArrayList<>();

  public MemberList(Time time) {
    this.time = time;
  }

  /**
   * Signs up a new member.
   *
   * @return the member, so the caller can add credits or list items for them
   * @throws MemberAlreadyExistsException if the email or mobile is already in use
   */
  public Member register(String name, String email, String mobile) {
    requireContactDetailsFree(email, mobile, null);

    Member member = new Member(name, email, mobile, generateMemberId(), time);
    members.add(member);
    return member;
  }

  /**
   * Removes a member from the register.
   *
   * @return whether there was a member with that id to remove
   */
  public boolean deleteMember(String memberId) {
    return members.removeIf(member -> member.getMemberId().equals(memberId));
  }

  /**
   * Changes a member's contact details.
   *
   * @return whether a member with that id exists
   * @throws MemberAlreadyExistsException if the new email or mobile belongs to
   *     someone else
   */
  public boolean changeMemberInformation(String memberId, String newName, String newEmail, String newMobile) {
    Member member = getMemberById(memberId);
    if (member == null) {
      return false;
    }

    requireContactDetailsFree(newEmail, newMobile, memberId);
    member.setName(newName);
    member.setEmail(newEmail);
    member.setMobile(newMobile);
    return true;
  }

  /**
   * Finds a member by id, or null if there is no such member.
   */
  public Member getMemberById(String memberId) {
    for (Member member : members) {
      if (member.getMemberId().equals(memberId)) {
        return member;
      }
    }
    return null;
  }

  /**
   * Finds a member by id, or refuses to carry on.
   *
   * @throws NotFoundException if there is no member with that id
   */
  public Member requireMemberById(String memberId) {
    Member member = getMemberById(memberId);
    if (member == null) {
      throw new NotFoundException("No member with id " + memberId + ".");
    }
    return member;
  }

  public boolean memberExists(String memberId) {
    return getMemberById(memberId) != null;
  }

  /**
   * Whether an email or mobile is already taken, ignoring the member with
   * {@code exceptMemberId} so a member can keep their own details on an update.
   */
  public boolean isEmailOrMobileExists(String email, String mobile, String exceptMemberId) {
    for (Member member : members) {
      if (member.getMemberId().equals(exceptMemberId)) {
        continue;
      }
      if (member.getEmail().equalsIgnoreCase(email) || member.getMobile().equals(mobile)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Every item owned by anyone, which is what the search runs over.
   */
  public List<Item> getAllItems() {
    List<Item> allItems = new ArrayList<>();
    for (Member member : members) {
      allItems.addAll(member.getOwnedItems());
    }
    return allItems;
  }

  public List<Member> getAllMembers() {
    return new ArrayList<>(members);
  }

  public Time getTime() {
    return time;
  }

  @Override
  public void hardCodeMembers() {
    Member alice = register("Alice", "alice@example.com", "0700000001");
    Member bob = register("Bob", "bob@example.com", "0700000002");
    Member sid = register("Sid", "sid@example.com", "0700000003");

    alice.addCredits(330);
    bob.addCredits(100);
    sid.addCredits(100);

    alice.createItem("Laptop", "Performance laptop", "Electronics", 50);
    alice.createItem("Mountain bike", "Hardtail, medium frame", "Sports", 10);
  }

  private void requireContactDetailsFree(String email, String mobile, String exceptMemberId) {
    for (Member member : members) {
      if (member.getMemberId().equals(exceptMemberId)) {
        continue;
      }
      if (member.getEmail().equalsIgnoreCase(email)) {
        throw new MemberAlreadyExistsException(email + " is already registered.");
      }
      if (member.getMobile().equals(mobile)) {
        throw new MemberAlreadyExistsException(mobile + " is already registered.");
      }
    }
  }

  private String generateMemberId() {
    String memberId;
    do {
      memberId = ID_GENERATOR.generateAlphaNum(MEMBER_ID_LENGTH);
    } while (memberExists(memberId));
    return memberId;
  }
}
