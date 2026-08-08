package com.borrowbox.config;

import com.borrowbox.model.EventLog;
import com.borrowbox.model.EventPublisher;
import com.borrowbox.model.LendingService;
import com.borrowbox.model.MemberList;
import com.borrowbox.model.Simulation;
import com.borrowbox.model.Time;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the domain objects and hands them to Spring to manage.
 *
 * <p>The model classes have no Spring annotations on them on purpose. They are
 * plain Java that can be constructed and tested without a container, and this
 * is the one file that knows they are being used inside one.
 */
@Configuration
public class DomainConfig {

  @Bean
  public Time time() {
    return new Time();
  }

  @Bean
  public EventPublisher eventPublisher() {
    return new EventPublisher();
  }

  @Bean
  public EventLog eventLog(EventPublisher eventPublisher) {
    EventLog log = new EventLog();
    eventPublisher.subscribe(log);
    return log;
  }

  @Bean
  public MemberList memberList(Time time) {
    MemberList members = new MemberList(time);
    members.hardCodeMembers();
    return members;
  }

  @Bean
  public LendingService lendingService(Time time, EventPublisher eventPublisher) {
    return new LendingService(time, eventPublisher);
  }

  @Bean
  public Simulation simulation(Time time, MemberList members, EventPublisher eventPublisher) {
    return new Simulation(time, members, eventPublisher);
  }
}
