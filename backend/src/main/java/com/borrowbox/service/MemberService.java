package com.borrowbox.service;

import com.borrowbox.model.AlphaNumericGen;
import com.borrowbox.model.Item;
import com.borrowbox.model.Member;
import com.borrowbox.model.MemberAlreadyExistsException;
import com.borrowbox.model.NotFoundException;
import com.borrowbox.repository.ItemRepository;
import com.borrowbox.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The register of everyone using the system.
 *
 * <p>Every member is created through {@link #register}, which is the only place
 * the uniqueness rules on email and mobile are applied. There is deliberately
 * no way to hand this a Member that was built somewhere else.
 */
@Service
public class MemberService {

  private static final int MEMBER_ID_LENGTH = 6;
  private static final AlphaNumericGen ID_GENERATOR = new AlphaNumericGen();

  private final MemberRepository members;
  private final ItemRepository items;
  private final ClockService clock;

  /**
   * Creates the register over its repositories and the shared clock.
   */
  public MemberService(MemberRepository members, ItemRepository items, ClockService clock) {
    this.members = members;
    this.items = items;
    this.clock = clock;
  }

  /**
   * Signs up a new member.
   *
   * @return the member, so the caller can add credits or list items for them
   * @throws MemberAlreadyExistsException if the email or mobile is already in use
   */
  @Transactional
  public Member register(String name, String email, String mobile) {
    requireContactDetailsFree(email, mobile, null);
    return members.save(new Member(name, email, mobile, generateMemberId(), clock.today()));
  }

  /**
   * Removes a member from the register, and the items they own with them.
   *
   * @return whether there was a member with that id to remove
   */
  @Transactional
  public boolean deleteMember(String memberId) {
    return members.findById(memberId).map(member -> {
      members.delete(member);
      return true;
    }).orElse(false);
  }

  /**
   * Changes a member's contact details.
   *
   * @return whether a member with that id exists
   * @throws MemberAlreadyExistsException if the new email or mobile belongs to
   *     someone else
   */
  @Transactional
  public boolean changeMemberInformation(String memberId, String newName, String newEmail,
      String newMobile) {
    Optional<Member> found = members.findById(memberId);
    if (found.isEmpty()) {
      return false;
    }

    requireContactDetailsFree(newEmail, newMobile, memberId);
    Member member = found.get();
    member.setName(newName);
    member.setEmail(newEmail);
    member.setMobile(newMobile);
    return members.save(member) != null;
  }

  @Transactional(readOnly = true)
  public Member getMemberById(String memberId) {
    return members.findById(memberId).orElse(null);
  }

  /**
   * Finds a member by id, or refuses to carry on.
   *
   * @throws NotFoundException if there is no member with that id
   */
  @Transactional(readOnly = true)
  public Member requireMemberById(String memberId) {
    return members.findById(memberId)
        .orElseThrow(() -> new NotFoundException("No member with id " + memberId + "."));
  }

  @Transactional(readOnly = true)
  public boolean memberExists(String memberId) {
    return members.existsById(memberId);
  }

  /**
   * Finds an item by id no matter who owns it.
   *
   * @throws NotFoundException if there is no item with that id
   */
  @Transactional(readOnly = true)
  public Item requireItemById(String itemId) {
    return items.findById(itemId)
        .orElseThrow(() -> new NotFoundException("No item with id " + itemId + "."));
  }

  /** Every item owned by anyone, which is what the search runs over. */
  @Transactional(readOnly = true)
  public List<Item> getAllItems() {
    return items.findAll();
  }

  @Transactional(readOnly = true)
  public List<Member> getAllMembers() {
    return members.findAll();
  }

  /**
   * Lists an item on behalf of a member, paying them the listing bonus.
   */
  @Transactional
  public Item listItem(String ownerId, String name, String description, String category,
      int costPerDay) {
    Member owner = requireMemberById(ownerId);
    Item item = owner.createItem(name, description, category, costPerDay, clock.today());
    members.save(owner);
    return item;
  }

  /**
   * Changes the details of an item its owner is allowed to edit.
   */
  @Transactional
  public Item updateItem(String itemId, String name, String description, String category,
      int costPerDay) {
    Item item = requireItemById(itemId);
    item.changeItemInfo(name, description, category, costPerDay);
    return items.save(item);
  }

  /**
   * Takes an item off the catalogue.
   *
   * @throws IllegalStateException if anyone has booked it, because removing the
   *     item would take the record of those loans with it
   */
  @Transactional
  public void deleteItem(String itemId) {
    Item item = requireItemById(itemId);
    if (!item.getContracts().isEmpty()) {
      throw new IllegalStateException(
          item.getItemName() + " has loans booked against it and cannot be removed.");
    }

    Member owner = item.getOwner();
    owner.deleteItemById(itemId);
    members.save(owner);
  }

  private void requireContactDetailsFree(String email, String mobile, String exceptMemberId) {
    members.findByEmailIgnoreCase(email)
        .filter(other -> !other.getMemberId().equals(exceptMemberId))
        .ifPresent(other -> {
          throw new MemberAlreadyExistsException(email + " is already registered.");
        });

    members.findByMobile(mobile)
        .filter(other -> !other.getMemberId().equals(exceptMemberId))
        .ifPresent(other -> {
          throw new MemberAlreadyExistsException(mobile + " is already registered.");
        });
  }

  private String generateMemberId() {
    String memberId;
    do {
      memberId = ID_GENERATOR.generateAlphaNum(MEMBER_ID_LENGTH);
    } while (members.existsById(memberId));
    return memberId;
  }
}
