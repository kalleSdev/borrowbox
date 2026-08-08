package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The member registry and the uniqueness rules it enforces.
 */
class MemberListTest {

  private final Time time = new Time();
  private final MemberList registry = new MemberList(time);

  private Member member(String name, String email, String mobile, String id) {
    return new Member(name, email, mobile, id, time);
  }

  @Test
  @DisplayName("accepts a member with an unused email and mobile")
  void acceptsAnUnusedMember() {
    assertThat(registry.addMember(member("Ada", "ada@example.com", "0700000001", "aaaaaa"))).isTrue();
    assertThat(registry.getAllMembers()).hasSize(1);
  }

  @Test
  @DisplayName("rejects a second member on the same email")
  void rejectsADuplicateEmail() {
    registry.addMember(member("Ada", "ada@example.com", "0700000001", "aaaaaa"));

    boolean accepted = registry.addMember(member("Grace", "ada@example.com", "0700000002", "bbbbbb"));

    assertThat(accepted).isFalse();
    assertThat(registry.getAllMembers()).hasSize(1);
  }

  @Test
  @DisplayName("rejects a second member on the same mobile")
  void rejectsADuplicateMobile() {
    registry.addMember(member("Ada", "ada@example.com", "0700000001", "aaaaaa"));

    boolean accepted = registry.addMember(member("Grace", "grace@example.com", "0700000001", "bbbbbb"));

    assertThat(accepted).isFalse();
  }

  @Test
  @DisplayName("frees up the email and mobile when a member leaves")
  void freesUpContactDetailsOnDeletion() {
    registry.addMember(member("Ada", "ada@example.com", "0700000001", "aaaaaa"));

    assertThat(registry.deleteMember("aaaaaa")).isTrue();
    assertThat(registry.addMember(member("Grace", "ada@example.com", "0700000001", "bbbbbb"))).isTrue();
  }

  @Test
  @DisplayName("reports an unknown member id rather than throwing")
  void reportsAnUnknownMemberId() {
    assertThat(registry.getMemberById("zzzzzz")).isNull();
    assertThat(registry.memberExists("zzzzzz")).isFalse();
    assertThat(registry.deleteMember("zzzzzz")).isFalse();
  }

  @Test
  @DisplayName("looks up a member by id")
  void looksUpAMemberById() {
    Member ada = member("Ada", "ada@example.com", "0700000001", "aaaaaa");
    registry.addMember(ada);

    assertThat(registry.getMemberById("aaaaaa")).isSameAs(ada);
    assertThat(registry.memberExists("aaaaaa")).isTrue();
  }

  @Test
  @DisplayName("updates contact details when they do not clash")
  void updatesContactDetails() {
    registry.addMember(member("Ada", "ada@example.com", "0700000001", "aaaaaa"));

    boolean updated = registry.changeMemberInformation("aaaaaa", "Ada L", "ada.l@example.com", "0700000009");

    assertThat(updated).isTrue();
    assertThat(registry.getMemberById("aaaaaa").getEmail()).isEqualTo("ada.l@example.com");
  }

  @Test
  @DisplayName("refuses an update that would steal another member's email")
  void refusesAnUpdateThatClashes() {
    registry.addMember(member("Ada", "ada@example.com", "0700000001", "aaaaaa"));
    registry.addMember(member("Grace", "grace@example.com", "0700000002", "bbbbbb"));

    boolean updated = registry.changeMemberInformation("bbbbbb", "Grace", "ada@example.com", "0700000002");

    assertThat(updated).isFalse();
    assertThat(registry.getMemberById("bbbbbb").getEmail()).isEqualTo("grace@example.com");
  }

  @Test
  @DisplayName("gathers every item across every member for search")
  void gathersEveryItemAcrossEveryMember() {
    Member ada = member("Ada", "ada@example.com", "0700000001", "aaaaaa");
    Member grace = member("Grace", "grace@example.com", "0700000002", "bbbbbb");
    ada.createItem("Cordless Drill", "18V", "Tools", 40, time);
    grace.createItem("Camping Tent", "Two person", "Outdoors", 25, time);
    registry.addMember(ada);
    registry.addMember(grace);

    assertThat(registry.getAllItems())
        .extracting(Item::getItemName)
        .containsExactlyInAnyOrder("Cordless Drill", "Camping Tent");
  }

  @Test
  @DisplayName("hands out a copy of the member list, not the list itself")
  void handsOutACopyOfTheMemberList() {
    registry.addMember(member("Ada", "ada@example.com", "0700000001", "aaaaaa"));

    registry.getAllMembers().clear();

    assertThat(registry.getAllMembers()).hasSize(1);
  }
}
