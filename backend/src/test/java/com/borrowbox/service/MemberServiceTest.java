package com.borrowbox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.borrowbox.model.Item;
import com.borrowbox.model.Member;
import com.borrowbox.model.MemberAlreadyExistsException;
import com.borrowbox.model.NotFoundException;
import com.borrowbox.repository.ContractRepository;
import com.borrowbox.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The member register and the uniqueness rules it enforces, against a real
 * database.
 */
@IntegrationTest
class MemberServiceTest {

  @Autowired
  private MemberService registry;

  @Autowired
  private MemberRepository members;

  @Autowired
  private ContractRepository contracts;

  @BeforeEach
  void startFromEmpty() {
    contracts.deleteAll();
    members.deleteAll();
  }

  private Member registerAda() {
    return registry.register("Ada", "ada@example.com", "0700000001");
  }

  @Test
  @DisplayName("signs up a member with an unused email and mobile")
  void signsUpAnUnusedMember() {
    Member ada = registerAda();

    assertThat(ada.getName()).isEqualTo("Ada");
    assertThat(registry.getAllMembers()).extracting(Member::getName).containsExactly("Ada");
  }

  @Test
  @DisplayName("gives every member a distinct generated id")
  void givesEveryMemberADistinctId() {
    Member ada = registerAda();
    Member grace = registry.register("Grace", "grace@example.com", "0700000002");

    assertThat(ada.getMemberId()).hasSize(6).isNotEqualTo(grace.getMemberId());
  }

  @Test
  @DisplayName("refuses a second member on the same email, whatever the casing")
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
  @DisplayName("takes a member's items with them when they leave")
  void deletingAMemberTakesTheirItems() {
    Member ada = registerAda();
    registry.listItem(ada.getMemberId(), "Drill", "18V", "Tools", 10);

    registry.deleteMember(ada.getMemberId());

    assertThat(registry.getAllItems()).isEmpty();
  }

  @Test
  @DisplayName("reports an unknown member id rather than throwing")
  void reportsAnUnknownMemberId() {
    assertThat(registry.getMemberById("zzzzzz")).isNull();
    assertThat(registry.memberExists("zzzzzz")).isFalse();
    assertThat(registry.deleteMember("zzzzzz")).isFalse();
    assertThat(registry.changeMemberInformation("zzzzzz", "X", "x@example.com", "0700000009"))
        .isFalse();
  }

  @Test
  @DisplayName("refuses to carry on when a required member is missing")
  void requireThrowsForAnUnknownId() {
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> registry.requireMemberById("zzzzzz"))
        .withMessage("No member with id zzzzzz.");
    assertThatExceptionOfType(NotFoundException.class)
        .isThrownBy(() -> registry.requireItemById("zzz"))
        .withMessage("No item with id zzz.");
  }

  @Test
  @DisplayName("updates contact details when they do not clash")
  void updatesContactDetails() {
    Member ada = registerAda();

    boolean updated = registry.changeMemberInformation(
        ada.getMemberId(), "Ada L", "ada.l@example.com", "0700000009");

    assertThat(updated).isTrue();
    assertThat(registry.requireMemberById(ada.getMemberId()).getEmail())
        .isEqualTo("ada.l@example.com");
  }

  @Test
  @DisplayName("lets a member keep their own email on an update")
  void letsAMemberKeepTheirOwnEmail() {
    Member ada = registerAda();

    boolean updated = registry.changeMemberInformation(
        ada.getMemberId(), "Ada L", "ada@example.com", "0700000001");

    assertThat(updated).isTrue();
    assertThat(registry.requireMemberById(ada.getMemberId()).getName()).isEqualTo("Ada L");
  }

  @Test
  @DisplayName("refuses an update that would steal another member's email")
  void refusesAnUpdateThatClashes() {
    registerAda();
    Member grace = registry.register("Grace", "grace@example.com", "0700000002");

    assertThatExceptionOfType(MemberAlreadyExistsException.class)
        .isThrownBy(() -> registry.changeMemberInformation(
            grace.getMemberId(), "Grace", "ada@example.com", "0700000002"));
  }

  @Test
  @DisplayName("pays the listing bonus when an item goes up")
  void paysTheListingBonus() {
    Member ada = registerAda();

    Item item = registry.listItem(ada.getMemberId(), "Drill", "18V", "Tools", 10);

    assertThat(item.getOwnerId()).isEqualTo(ada.getMemberId());
    assertThat(registry.requireMemberById(ada.getMemberId()).getCredits())
        .isEqualTo(Member.LISTING_BONUS);
  }

  @Test
  @DisplayName("gathers every item across every member for search")
  void gathersEveryItemAcrossEveryMember() {
    Member ada = registerAda();
    Member grace = registry.register("Grace", "grace@example.com", "0700000002");
    registry.listItem(ada.getMemberId(), "Cordless Drill", "18V", "Tools", 40);
    registry.listItem(grace.getMemberId(), "Camping Tent", "Two person", "Outdoors", 25);

    assertThat(registry.getAllItems())
        .extracting(Item::getItemName)
        .containsExactlyInAnyOrder("Cordless Drill", "Camping Tent");
  }
}
