package com.borrowbox.config;

import com.borrowbox.model.Member;
import com.borrowbox.repository.MemberRepository;
import com.borrowbox.service.ClockService;
import com.borrowbox.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puts a few members and items in an empty database so there is something to
 * click on first run.
 *
 * <p>Only seeds when the database is empty, so a restart does not duplicate
 * the demo data or undo anything that has happened since.
 */
@Component
public class DataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final MemberRepository members;
  private final MemberService memberService;
  private final ClockService clock;

  /**
   * Creates the seeder over the member register and the clock.
   */
  public DataSeeder(MemberRepository members, MemberService memberService, ClockService clock) {
    this.members = members;
    this.memberService = memberService;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(String... args) {
    clock.clock();

    if (members.count() > 0) {
      log.info("Database already has {} members, skipping the demo data.", members.count());
      return;
    }

    Member alice = memberService.register("Alice", "alice@example.com", "0700000001");
    Member bob = memberService.register("Bob", "bob@example.com", "0700000002");
    Member sid = memberService.register("Sid", "sid@example.com", "0700000003");

    alice.addCredits(330);
    bob.addCredits(100);
    sid.addCredits(100);
    members.saveAll(java.util.List.of(alice, bob, sid));

    memberService.listItem(alice.getMemberId(), "Laptop", "Performance laptop", "Electronics", 50);
    memberService.listItem(alice.getMemberId(), "Mountain bike", "Hardtail, medium frame", "Sports", 10);

    log.info("Seeded {} members and their items.", members.count());
  }
}
