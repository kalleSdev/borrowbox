package com.borrowbox.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The member register and the uniqueness rules it enforces.
 */
class MemberListTest {

  private final Time time = new Time();
  private final MemberList registry = new MemberList(time);

  private Member registerAda() {
    return registry.register("Ada", "ada@example.com", "0700000001");
  }

  @Test
  @DisplayName("signs up a member with an unused email and mobile")
  void signsUpAnUnusedMember() {
    Member ada = registerAda();

    assertThat(ada.getName()).isEqualTo("Ada");
    assertThat(registry.getAllMembers()).containsExactly(ada);
  }

  @Test
  @DisplayName("gives every member a distinct generated id")
  void givesEveryMemberADistinctId() {
    Member ada = registerAda();
    Member grace = registry.register("Grace", "grace@example.com", "0700000002");

    assertThat(ada.getMemberId()).hasSize(6).isNotEqualTo(grace.getMemberId());
  }

  @Test
  @DisplayName("refuses a second member on the same email")
  void refusesADuplicateEmail() {
    registerAda();

    assertThatExceptionOfType(MemberAlreadyExistsException.class)
        .isThrownBy(() -> registry.register("Grace", "ADA@example.com", "0700000002"))
        .withMessageContaining("already registered");
    assertThat(registry.getAllMembers()).hasSize(1);
  }

  @Test
  @DisplayName("refuses a second member on the same mobile")
  void refusesADuplicateMobile() {
    registerAda();

    assertThatExceptionOfType(MemberAlreadyExistsException.class)
        .isThrownBy(() -> registry.register("Grace", "grace@example.com", "0700000001"));
    assertThat(registry.getAllMembers()).hasSize(1);
  }

  @Test
  @DisplayName("frees up the email and mobile when a member leaves")
  void freesUpContactDetailsOnDeletion() {
    Member ada = registerAda();

    assertThat(registry.deleteMember(ada.getMemberId())).isTrue();
    assertThat(registry.register("Grace", "ada@example.com", "0700000001")).isNotNull();
  }

  @Test
  @DisplayName("reports an unknown member id rather than throwing")
  void reportsAnUnknownMemberId() {
    assertThat(registry.getMemberById("zzzzzz")).isNull();
    assertThat(registry.memberExists("zzzzzz")).isFalse();
    assertThat(registry.deleteMember("zzzzzz")).isFalse();
    assertThat(registry.changeMemberInformation("zzzzzz", "X", "x@example.com", "0700000009")).isFalse();
  }

  @Test
  @DisplayName("looks up a member by id")
  void looksUpAMemberById() {
    Member ada = registerAda();

    assertThat(registry.getMemberById(ada.getMemberId())).isSameAs(ada);
    assertThat(registry.memberExists(ada.getMemberId())).isTrue();
  }

  @Test
  @DisplayName("updates contact details when they do not clash")
  void updatesContactDetails() {
    Member ada = registerAda();

    boolean updated = registry.changeMemberInformation(
        ada.getMemberId(), "Ada L", "ada.l@example.com", "0700000009");

    assertThat(updated).isTrue();
    assertThat(ada.getEmail()).isEqualTo("ada.l@example.com");
  }

  @Test
  @DisplayName("lets a member keep their own email on an update")
  void letsAMemberKeepTheirOwnEmail() {
    Member ada = registerAda();

    boolean updated = registry.changeMemberInformation(
        ada.getMemberId(), "Ada L", "ada@example.com", "0700000001");

    assertThat(updated).isTrue();
    assertThat(ada.getName()).isEqualTo("Ada L");
  }

  @Test
  @DisplayName("refuses an update that would steal another member's email")
  void refusesAnUpdateThatClashes() {
    registerAda();
    Member grace = registry.register("Grace", "grace@example.com", "0700000002");

    assertThatExceptionOfType(MemberAlreadyExistsException.class)
        .isThrownBy(() -> registry.changeMemberInformation(
            grace.getMemberId(), "Grace", "ada@example.com", "0700000002"));
    assertThat(grace.getEmail()).isEqualTo("grace@example.com");
  }

  @Test
  @DisplayName("gathers every item across every member for search")
  void gathersEveryItemAcrossEveryMember() {
    registerAda().createItem("Cordless Drill", "18V", "Tools", 40);
    registry.register("Grace", "grace@example.com", "0700000002")
        .createItem("Camping Tent", "Two person", "Outdoors", 25);

    assertThat(registry.getAllItems())
        .extracting(Item::getItemName)
        .containsExactlyInAnyOrder("Cordless Drill", "Camping Tent");
  }

  @Test
  @DisplayName("hands out a copy of the member list, not the list itself")
  void handsOutACopyOfTheMemberList() {
    registerAda();

    registry.getAllMembers().clear();

    assertThat(registry.getAllMembers()).hasSize(1);
  }

  @Test
  @DisplayName("seeds demo members that each have a usable id")
  void seedsDemoMembers() {
    registry.hardCodeMembers();

    assertThat(registry.getAllMembers()).hasSize(3);
    assertThat(registry.getAllMembers())
        .allSatisfy(member -> assertThat(registry.memberExists(member.getMemberId())).isTrue());
    assertThat(registry.getAllItems()).hasSize(2);
  }
}
