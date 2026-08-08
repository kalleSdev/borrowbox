package com.borrowbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.borrowbox.service.EventLog;
import com.borrowbox.service.LendingService;
import com.borrowbox.service.MemberService;
import com.borrowbox.service.Simulation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Checks the application actually wires itself together, which is the one thing
 * a container can get wrong that the unit tests would never notice.
 */
@SpringBootTest
class BorrowBoxApplicationTests {

  @Autowired
  private MemberService members;

  @Autowired
  private LendingService lendingService;

  @Autowired
  private Simulation simulation;

  @Autowired
  private EventLog eventLog;

  @Test
  @DisplayName("starts up with every domain object available")
  void contextLoads() {
    assertThat(members).isNotNull();
    assertThat(lendingService).isNotNull();
    assertThat(simulation).isNotNull();
    assertThat(eventLog).isNotNull();
  }

  @Test
  @DisplayName("starts with the demo data already seeded")
  void startsWithDemoData() {
    assertThat(members.getAllMembers()).hasSize(3);
    assertThat(members.getAllItems()).hasSize(2);
  }

  @Test
  @DisplayName("shares one event stream between the services and the log")
  void sharesOneEventStream() {
    long before = eventLog.size();

    simulation.advanceDay();

    assertThat(eventLog.size()).isGreaterThan(before);
  }
}
