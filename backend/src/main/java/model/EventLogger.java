package model;

/**
 * Eventlogger class implementing Observer.
 */
public class EventLogger implements Observer {
  @Override
  public void update(String message) {
    System.out.println(message);
  }
}
