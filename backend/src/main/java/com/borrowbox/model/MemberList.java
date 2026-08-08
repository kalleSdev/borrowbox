package com.borrowbox.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class used to keep track of member addition and deletion.
 */
public class MemberList implements Persistence {

  private Time time;
  private List<Member> members;
  private Set<String> registeredEmails;
  private Set<String> registeredMobiles;

  /**
   * Memberlist constructor.
   */
  public MemberList(Time time) {
    this.members = new ArrayList<>();
    this.registeredEmails = new HashSet<>();
    this.registeredMobiles = new HashSet<>();
    this.time = time;
  }

  /**
   * New strategy pattern method.
   */
  public List<Item> getAllItems() {
    List<Item> allItems = new ArrayList<>();
    for (Member member : members) {
      allItems.addAll(member.getOwnedItems());
    }
    return allItems;
  }

  /**
   * Method to register a member if it is unique.
   */
  public boolean addMember(Member member) {
    if (!isEmailUnique(member.getEmail()) || !isMobileUnique(member.getMobile())) {
      // Email or mobile is not unique; member registration failed
      return false;
    }

    // Email and mobile are unique; add the member to the registry
    members.add(member);
    registeredEmails.add(member.getEmail());
    registeredMobiles.add(member.getMobile());
    return true;
  }

  /**
   * used to create a member directly for an owner.
   */
  public Member memberCreation(String name, String email, String mobile) {
    String memberId;
    AlphaNumericGen ran = new AlphaNumericGen();

    do {
      memberId = ran.generateAlphaNum(6); // Generate a new ID
    } while (isMemberIdExists(memberId)); // Check if the ID already exists
    Member member = new Member(name, email, mobile, memberId, time);
    members.add(member);
    return member;
  }

  private boolean isMemberIdExists(String memberId) {
    for (Member member : members) {
      if (member.getMemberId().equals(memberId)) {
        return true; // ID already exists
      }
    }
    return false; // ID is unique
  }

  /**
   * Method to delete a member by member ID.
   */
  public boolean deleteMember(String memberId) {
    for (Member member : members) {
      if (member.getMemberId().equals(memberId)) {
        // Found the member; remove them from the registry
        members.remove(member);
        registeredEmails.remove(member.getEmail());
        registeredMobiles.remove(member.getMobile());
        return true;
      }
    }

    // Member not found; deletion failed
    return false;
  }

  /**
   * Used to ensure uniqueness.
   */
  public boolean isEmailOrMobileExists(String email, String mobile) {
    for (Member member : this.members) {
      if (member.getEmail().equalsIgnoreCase(email) || member.getMobile().equals(mobile)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Ensures member existing.
   */
  public boolean memberExists(String memberId) {
    for (Member member : this.members) {
      if (member.getMemberId().equals(memberId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Method to change a members info.
   */
  public boolean changeMemberInformation(String memberId, String newName, String newEmail, String newMobile) {
    for (Member member : members) {
      if (member.getMemberId().equals(memberId)) {
        // Check if the new email and mobile are unique
        if (!isEmailUniqueC(newEmail, memberId) || !isMobileUniqueC(newMobile, memberId)) {
          // Email or mobile is not unique; information change failed
          return false;
        }

        // Update member information
        member.setName(newName);
        member.setEmail(newEmail);
        member.setMobile(newMobile);
        return true;
      }
    }

    // Member not found; information change failed
    return false;
  }

  /**
   * Method to get the full information of a specific member by member ID.
   */
  public Member getMemberById(String memberId) {
    for (Member member : members) {
      if (member.getMemberId().equals(memberId)) {
        // Found the member; return their information
        return member;
      }
    }

    // Member not found; return null or handle accordingly
    return null;
  }

  /**
   * Method to check if an email is unique.
   */
  private boolean isEmailUnique(String email) {
    return !registeredEmails.contains(email);
  }

  /**
   * Method to check if a mobile number is unique.
   */
  private boolean isMobileUnique(String mobile) {
    return !registeredMobiles.contains(mobile);
  }

  // New helper methods for uniqueness when changing info.

  /**
   * ensures uniqueness when changing info.
   */
  private boolean isEmailUniqueC(String email, String memberId) {
    for (Member member : members) {
      if (!member.getMemberId().equals(memberId) && member.getEmail().equalsIgnoreCase(email)) {
        return false; // Email is not unique
      }
    }
    return true;
  }

  /**
   * ensures uniqueness when changing info.
   */
  private boolean isMobileUniqueC(String mobile, String memberId) {
    for (Member member : members) {
      if (!member.getMemberId().equals(memberId) && member.getMobile().equals(mobile)) {
        return false; // Mobile is not unique
      }
    }
    return true;
  }

  /**
   * get members.
   */
  public List<Member> getAllMembers() {
    return new ArrayList<>(members);
  }

  /**
   * get time.
   */
  public Time getTime() {
    return time;
  }

  @Override
  public void hardCodeMembers() {
    // Create members
    Member m1 = new Member("Alice", "alice@example.com", "123", generateMemberId(), time);
    Member m2 = new Member("Bob", "bob@example.com", "321", generateMemberId(), time);
    Member m3 = new Member("Sid", "sid@example.com", "332211", generateMemberId(), time);

    // Add credits to members
    m1.addCredits(330);
    m2.addCredits(100);
    m3.addCredits(100);

    // Create items and add them to members
    m1.createItem("laptop", "performance laptop", "electronic", 50);
    m1.createItem("lappar", "A mountain bike", "Sports", 10);

    // Add members to the member list
    addMember(m1);
    addMember(m2);
    addMember(m3);
  }

  private String generateMemberId() {
    AlphaNumericGen ran = new AlphaNumericGen();
    String memberId;
    do {
      memberId = ran.generateAlphaNum(6);
    } while (isMemberIdExists(memberId));
    return memberId;
  }

}
