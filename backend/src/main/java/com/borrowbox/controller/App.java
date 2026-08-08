package com.borrowbox.controller;

import com.borrowbox.model.ConsoleEventPrinter;
import com.borrowbox.model.EventLog;
import com.borrowbox.model.EventPublisher;
import com.borrowbox.model.LendingService;
import com.borrowbox.model.MemberList;
import com.borrowbox.model.Simulation;
import com.borrowbox.model.Time;
import com.borrowbox.view.Viewer;

/**
 * Entry point. Builds every part of the system, connects them together and
 * hands the finished set to the menu loop.
 *
 * <p>This is the only place that decides what anything is made of, which is why
 * it is the only place that says "new".
 */
public class App {

  /**
   * Main.
   */
  public static void main(String[] args) {
    Time time = new Time();
    MemberList members = new MemberList(time);
    members.hardCodeMembers();

    EventPublisher events = new EventPublisher();
    EventLog eventLog = new EventLog();
    events.subscribe(eventLog);
    events.subscribe(new ConsoleEventPrinter());

    LendingService lendingService = new LendingService(time, events);
    Simulation simulation = new Simulation(time, members, events);

    new ControlTower(new Viewer(), members, lendingService, simulation, eventLog).start();
  }
}
