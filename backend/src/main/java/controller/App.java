package controller;

import model.MemberList;
import model.Time;
import view.Viewer;

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
