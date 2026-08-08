package com.borrowbox.view;

import com.borrowbox.model.Contract;
import com.borrowbox.model.DomainEvent;
import com.borrowbox.model.Item;
import com.borrowbox.model.Member;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * class used to ensure MVC structure.
 */
public class Viewer {
  private Scanner sc;

  public void initial() {
    System.out.println("The Stuff Lending System");
  }

  /**
   * Strategy pattern implementation.
   */
  public void displayItems(List<Item> items) {
    System.out.println("----------------------");
    System.out.println("List of Items:");
    for (Item item : items) {
      System.out.println("Item Name: " + item.getItemName());
      System.out.println("Item Cost: " + item.getCostDaily());
      System.out.println("-------------------");
    }
  }

  /**
   * Viewer.
   */
  public Viewer() {
    this.sc = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
  }

  /**
   * used to print a msg.
   */
  public void printMessage(String message) {
    System.out.println(message);
  }

  /**
   * used to get a string input.
   */
  public String getStringInput(String prompt) {
    System.out.print(prompt);
    return sc.next();
  }

  /**
   * used to get an integer input.
   */
  public int getIntInput(String prompt) {
    System.out.print(prompt);
    return sc.nextInt();
  }

  /**
   * displays a msg.
   */
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * displays a msg.
   */
  public void displayErrorMessage(String message) {
    System.out.println(message);
  }

  /**
   * displays a msg and asks for an input.
   */
  public String getMemberId() {
    System.out.print("Enter the ID of the member who owns the item: ");
    return sc.next();
  }

  /**
   * displays a msg and asks for an input.
   */
  public String getItemId() {
    System.out.print("Enter the ID of the item to view: ");
    return sc.next();
  }

  /**
   * displays the main menu of choices.
   */
  public int mainMenu() {
    System.out.println("----------------------");
    System.out.println("Choose an option: ");
    System.out.println("1.Member");
    System.out.println("2.Item");
    System.out.println("3.Create contract");
    System.out.println("4.Advance day");
    System.out.println("5.Exit");
    System.out.println("6.Search items");
    System.out.println("7.Recent activity");
    System.out.println("Any other number = back to main menu.");
    int choice = sc.nextInt();
    return choice;
  }

  /**
   * displays the choices related to members.
   */
  public int memberMenu() {
    System.out.println("----------------------");
    System.out.println("Choose an option: ");
    System.out.println("1.Add member");
    System.out.println("2.Show all members");
    System.out.println("3.Remove a member");
    System.out.println("4.Update members info");
    System.out.println("5.Specify a member");
    System.out.println("6.Show all members (Verbose)");
    System.out.println("7.Exit");
    int choice = sc.nextInt();
    return choice;
  }

  /**
   * displays the choices related to items.
   */
  public int itemMenu() {
    System.out.println("----------------------");
    System.out.println("Choose an option: ");
    System.out.println("1.Add item");
    System.out.println("2.Show all items");
    System.out.println("3.Remove an item");
    System.out.println("4.Change item info");
    System.out.println("5.List item contracts(previous, current and upcoming)");
    System.out.println("7.Exit");
    int choice = sc.nextInt();
    return choice;
  }

  /**
   * displays options.
   */
  public int menuIds() {
    System.out.println("----------------------");
    System.out.println("Choose an option: ");
    System.out.println("1.Detailed view");
    System.out.println("2.Exit");
    int choice = sc.nextInt();
    return choice;
  }

  /**
   * used to get an input.
   */
  public String getInput(String prompt) {
    System.out.print(prompt);
    return sc.next();
  }

  /**
   * Method to list all members in a simple way.
   */
  public void listMembersSimple(List<Member> members) {
    System.out.println("----------------------");
    System.out.println("List of Members (Name, Email, Current Credits, Owned Items):");
    for (Member member : members) {
      System.out.println("Name: " + member.getName());
      System.out.println("Email: " + member.getEmail());
      System.out.println("Current Credits: " + member.getCredits());
      System.out.println("Number of Owned Items: " + member.getOwnedItems().size());
      System.out.println();
    }
  }

  /**
   * Method to list all members in a verbose way.
   */
  public void listMembersVerbose(List<Member> members) {
    System.out.println("----------------------");
    System.out.println("List of Members (Verbose):");
    for (Member member : members) {
      System.out.println("Name: " + member.getName());
      System.out.println("Email: " + member.getEmail());
      System.out.println("Owned Items:");

      for (Item item : member.getOwnedItems()) {
        System.out.println("- Item Name: " + item.getItemName());
        System.out.println("  Description: " + item.getDescription());

        // Get contracts associated with this item
        List<Contract> contracts = item.getContracts();
        if (!contracts.isEmpty()) {
          System.out.println("  Contracts:");
          for (Contract contract : contracts) {
            System.out.println("    Lent To: " + contract.getBorrower().getName());
            System.out.println("    Time Period: day " + contract.getStartDay() + " to " + contract.getEndDay());
          }
        } else {
          System.out.println("  No Contracts");
        }
      }
      System.out.println();
    }
  }

  /**
   * Prints the activity feed, newest first.
   */
  public void displayEvents(List<DomainEvent> events) {
    System.out.println("----------------------");
    System.out.println("Recent activity:");
    if (events.isEmpty()) {
      System.out.println("Nothing has happened yet.");
      return;
    }
    for (DomainEvent event : events) {
      System.out.println("[day " + event.day() + "] " + event.description());
    }
  }

  /**
   * displays member info.
   */
  public void displayMemberInformation(Member member) {
    System.out.println("----------------------");
    System.out.println("Member Information:");
    System.out.println("ID: " + member.getMemberId());
    System.out.println("Name: " + member.getName());
    System.out.println("Email: " + member.getEmail());
    System.out.println("Mobile: " + member.getMobile());
    System.out.println("Current Credits: " + member.getCredits());
  }

  /**
   * displays member info.
   */
  public void listFullInfo(List<Member> members) {
    System.out.println("----------------------");
    System.out.println("List of Members (Id, Name, Email):");
    for (Member member : members) {
      System.out.println("Member Id: " + member.getMemberId());
      System.out.println("Name: " + member.getName());
      System.out.println("Email: " + member.getEmail());
      System.out.println("Creation date: " + member.getCreationDate());
      System.out.println();
    }
  }

  /**
   * displays member and item info.
   */
  public void listAllItemsAndInfo(List<Member> members) {
    for (Member member : members) {
      System.out.println("Member Name: " + member.getName());
      List<Item> items = member.getOwnedItems();
      for (Item item : items) {
        System.out.println("Item Name: " + item.getItemName());
        System.out.println("Item Id: " + item.getItemId());
        System.out.println("Item Description: " + item.getDescription());
        System.out.println("Item Category: " + item.getCategory());
        System.out.println("Item Cost Per Day: " + item.getCostDaily());
        System.out.println("Item creation date: " + item.getDayCreation());
        System.out.println();
      }
    }
  }

  /**
   * displays item and contract info.
   */
  public void displayItemAndContractsInformation(Item item) {
    // Display item information
    System.out.println("Item Information:");
    System.out.println("ID: " + item.getItemId());
    System.out.println("Name: " + item.getItemName());
    System.out.println("Description: " + item.getDescription());
    System.out.println("Category: " + item.getCategory());
    System.out.println("Cost per day: " + item.getCostDaily());
    System.out.println("Item creation date: " + item.getDayCreation());

    System.out.println("Owner: " + item.getOwner().getName());

    // Display contracts information
    System.out.println("\nContracts:");
    if (item.getContracts().isEmpty()) {
      System.out.println("No contracts available for this item.");
    } else {
      for (Contract contract : item.getContracts()) {
        System.out.println("Lent to: " + contract.getBorrower().getName());
        System.out.println("Lender: " + contract.getLender().getName());
        System.out.println("Start day: " + contract.getStartDay());
        System.out.println("End day: " + contract.getEndDay());
        System.out.println("Cost: " + contract.getCost() + " credits");
        System.out.println("-----");
      }
    }
  }
}
