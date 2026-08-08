package com.borrowbox.controller;

import com.borrowbox.model.MemberList;
import com.borrowbox.model.Time;
import com.borrowbox.view.Viewer;

/**
 * Class app.
 */
public class App {

  /**
   * Main.
   */
  public static void main(String[] args) {

    Viewer viewer = new Viewer();
    Time time = new Time();
    MemberList memberList = new MemberList(time);
    ControlTower controller = new ControlTower(viewer, memberList, time);
    controller.start();
  }
}
