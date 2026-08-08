package com.borrowbox.controller;

import com.borrowbox.model.Contract;
import com.borrowbox.model.EventLogger;
import com.borrowbox.model.Item;
import com.borrowbox.model.Member;
import com.borrowbox.model.MemberList;
import com.borrowbox.model.SearchByMaxPrice;
import com.borrowbox.model.SearchByName;
import com.borrowbox.model.Time;
import com.borrowbox.view.Viewer;
import java.util.List;


/**
 * setters and code changers.
 */
public class ControlTower {
  private Viewer viewer;
  private MemberList memberlist;
  private Time time;
  private EventLogger eventLogger = new EventLogger();

  /**
   * constructor.
   */
  public ControlTower(Viewer viewer, MemberList memberList, Time time) {
    this.memberlist = new MemberList(time);
    this.viewer = new Viewer();
    this.time = time;

    initializeMembers();
  }

  /**
   * used to initialize and add the hardcoded members.
   */
  private void initializeMembers() {
    memberlist.hardCodeMembers();
  }

  /**
   * Methods.
   */
  public void createMember() {
    String name = viewer.getInput("Enter name: ");
    String email = viewer.getInput("Enter email: ");
    String mobile = viewer.getInput("Enter mobile number: ");

    // Check if the email or mobile number already exists
    if (memberlist.isEmailOrMobileExists(email, mobile)) {
      viewer.displayMessage("A member with this email or mobile number already exists.");
    } else {
      memberlist.memberCreation(name, email, mobile);
      viewer.displayMessage("Member created successfully.");
    }
  }

  /**
   * used to remove a member.
   */
  public void removeMember() {
    String memberIdToRemove = viewer.getInput("Enter the ID of the member to remove: ");

    // Check if the member with the given ID exists
    if (memberlist.memberExists(memberIdToRemove)) {
      memberlist.deleteMember(memberIdToRemove);
      viewer.displayMessage("Member removed successfully.");
    } else {
      viewer.displayMessage("Error: No member found with the given ID.");
    }
  }

  /**
   * updates a members info.
   */
  public void updateMember() {
    String memberIdToUpdate = viewer.getInput("Enter the ID of the member to update: ");

    if (memberlist.memberExists(memberIdToUpdate)) {
      String newName = viewer.getInput("Enter new name: ");
      String newEmail = viewer.getInput("Enter new email: ");
      String newMobile = viewer.getInput("Enter new mobile: ");

      memberlist.changeMemberInformation(memberIdToUpdate, newName, newEmail, newMobile);
      viewer.displayMessage("Member updated successfully.");
    } else {
      viewer.displayErrorMessage("Member not found with ID: " + memberIdToUpdate);
    }
  }

  /**
   * used to view members info.
   */
  public void viewMemberInformation() {
    String memberIdToView = viewer.getInput("Enter the ID of the member to view: ");
    Member memberToView = memberlist.getMemberById(memberIdToView);

    if (memberToView != null) {
      viewer.displayMemberInformation(memberToView);
    } else {
      viewer.displayErrorMessage("Member not found with ID: " + memberIdToView);
    }
  }

  /**
   * NEW SEARCH ENGINE STRATEGY PATTERN.
   */
  public void searchItems() {
    ItemSearchContext searchContext = new ItemSearchContext();
    viewer.displayMessage("Search by: \n1. Name\n2. Max Price");
    int choice = viewer.getIntInput("Choose an option: ");
    String criterion = viewer.getStringInput("Enter search criterion: ");

    switch (choice) {
      case 1:
        searchContext.setStrategy(new SearchByName());
        break;
      case 2:
        searchContext.setStrategy(new SearchByMaxPrice());
        break;
      default:
        viewer.displayErrorMessage("Invalid search option.");
        return;
    }

    List<Item> results = searchContext.executeSearch(memberlist.getAllItems(), criterion);
    if (results.isEmpty()) {
      viewer.displayMessage("No items found.");
    } else {
      viewer.displayItems(results);
    }
  }

  /**
   * used to create an item for a member.
   */
  public void createItem() {
    String itemName = viewer.getInput("Enter item name: ");
    String itemDescription = viewer.getInput("Enter item description: ");
    String itemCategory = viewer.getInput("Enter item category: ");
    int itemCost = viewer.getIntInput("Cost Daily(integer): ");

    String ownerId = viewer.getInput("Choose owner using Id: ");
    Member owner = memberlist.getMemberById(ownerId);

    if (owner != null) {
      owner.createItem(itemName, itemDescription, itemCategory, itemCost);
      viewer.displayMessage("Item created successfully!");
    } else {
      viewer.displayMessage("Member not found with ID: " + ownerId);
    }
  }

  /**
   * used to delete an item.
   */
  public void deleteItem() {
    String memberId = viewer.getInput("Enter the ID of the member who owns the item: ");
    Member member = memberlist.getMemberById(memberId);

    if (member != null) {
      String itemId = viewer.getInput("Enter the ID of the item to delete: ");
      if (member.deleteItemById(itemId)) {
        viewer.displayMessage("Item deleted successfully.");
      } else {
        viewer.displayErrorMessage("Item not found.");
      }
    } else {
      viewer.displayErrorMessage("Member not found.");
    }
  }

  /**
   * used to display the contracts for an item.
   */
  public void displayItemContracts() {
    String memberId = viewer.getMemberId();
    String itemId = viewer.getItemId();

    Member member = memberlist.getMemberById(memberId);
    if (member != null) {
      Item item = member.getItemById(itemId);
      if (item != null) {
        viewer.displayItemAndContractsInformation(item);
      } else {
        viewer.displayErrorMessage("Item with ID " + itemId + " not found for member with ID " + memberId);
      }
    } else {
      viewer.displayErrorMessage("Member with ID " + memberId + " not found");
    }
  }

  /**
   * Used to initiate.
   */
  public void start() {

    boolean isRunning = true;
    viewer.initial();
    while (isRunning) {
      int choice = viewer.mainMenu();
      switch (choice) {
        case 1:
          int choice1 = viewer.memberMenu();
          switch (choice1) {
            case 1:
              // Create a member by asking for inputs
              createMember();
              break;

            case 2:
              List<Member> members = memberlist.getAllMembers(); // Retrieve all members from the model
              viewer.listMembersSimple(members); // Call the Viewer's method
              int choice2 = viewer.menuIds();
              switch (choice2) {
                case 1:
                  // Case 1: View all members (with IDs)
                  viewer.listFullInfo(members);
                  break;

                case 2:
                  // return to menu
                  break;

                default:
                  viewer.displayErrorMessage("Incorrect input. Please try again.");
                  break;
              }
              break;
            case 3:
              removeMember();
              break;

            case 4:
              // Case 4 logic to update a member's information
              updateMember();
              break;

            case 5:
              // Case 5 logic to view full information of a specific member
              viewMemberInformation();
              break;

            case 6:
              // Case: List members in a verbose way
              List<Member> allMembers = memberlist.getAllMembers();
              viewer.listMembersVerbose(allMembers);
              break;

            case 7:
              isRunning = false;
              break;

            default:
              viewer.displayErrorMessage("Incorrect input. Please try again.");
              break;
          }
          break;
        // case 1 done.

        case 2:
          int choice2 = viewer.itemMenu();
          switch (choice2) {
            case 1:
              // Case 1: Create an item
              createItem();
              break;

            case 2:
              List<Member> members = memberlist.getAllMembers(); // Retrieve all members from the model
              viewer.listAllItemsAndInfo(members);
              break;

            case 3:
              // delete item.
              deleteItem();
              break;

            case 4:
              // change item info.
              String ownerId = viewer.getInput("Enter the ID of the member who owns the item: ");
              Member ownerr = memberlist.getMemberById(ownerId);
              if (ownerr != null) {
                String itemId = viewer.getInput("Enter the ID of the item to update: ");
                Item itemToUpdate = ownerr.getItemById(itemId);
                if (itemToUpdate != null) {
                  String newName = viewer.getInput("Enter new name: ");
                  String newDescription = viewer.getInput("Enter new description: ");
                  String newCategory = viewer.getInput("Enter new category: ");
                  int newCostDaily = Integer.parseInt(viewer.getInput("Enter new daily cost: "));
                  itemToUpdate.changeItemInfo(newName, newDescription, newCategory, newCostDaily);
                  viewer.displayMessage("Item information updated successfully.");
                } else {
                  viewer.displayMessage("Item not found.");
                }
              } else {
                viewer.displayMessage("Member not found.");
              }
              break;

            case 5:
              // list item contracts.
              displayItemContracts();
              break;

            default:
              viewer.displayErrorMessage("Incorrect input. Please try again.");
              break;

          }

          // break for case 2.
          break;

        case 3:
          // Create a new contract
          String lenderId = viewer.getStringInput("Enter the lender ID: ");
          Member lender = memberlist.getMemberById(lenderId);

          if (lender != null) {
            String itemId = viewer.getStringInput("Enter the item ID: ");
            Item item = lender.getItemById(itemId);

            if (item != null) {
              String borrowerId = viewer.getStringInput("Enter the borrower ID: ");
              Member borrower = memberlist.getMemberById(borrowerId);

              if (borrower != null) {
                int startDate = viewer.getIntInput("Enter the start date: ");
                int endDate = viewer.getIntInput("Enter the end date: ");

                Contract contract = new Contract(item, lender, borrower, startDate, endDate, time);
                EventLogger logger = new EventLogger();
                contract.attach(logger);

                if (contract.isValid()) { // Add this method to check if the contract is valid
                  item.addContract(contract);
                  lender.addContract(contract);
                  borrower.addContract(contract);
                  viewer.displayMessage("Contract created successfully.");
                } else {
                  viewer.displayMessage("Failed to create contract. Check the entered details and try again.");
                }
              } else {
                viewer.displayMessage("Borrower not found.");
              }
            } else {
              viewer.displayMessage("Item not found.");
            }
          } else {
            viewer.displayMessage("Lender not found.");
          }
          break;

        case 4:
          // Advance the day
          time.advanceDay();
          viewer.displayMessage("Day advanced successfully.");
          int currentDay = time.getCurrentDay();
          viewer.displayMessage("Current day: " + currentDay);

          // Initialize the EventLogger
          EventLogger eventLogger = new EventLogger();

          // Loop through all members
          for (Member member : memberlist.getAllMembers()) {
            // Loop through all items owned by the member
            for (Item item : member.getOwnedItems()) {
              // Loop through all contracts associated with the item
              for (Contract contract : item.getContracts()) {
                // Check if a contract starts today
                if (contract.getStartDate() == currentDay) {
                  eventLogger.update(
                      "Contract for item " + item.getItemName() + " starts today. Item ID: " + item.getItemId());
                }
                // Check if a contract ends today
                if (contract.getEndDate() == currentDay) {
                  eventLogger
                      .update("Contract for item " + item.getItemName() + " ends today. Item ID: " + item.getItemId());
                }
              }
            }
          }

          break;

        case 5:
          isRunning = false;
          break;

        case 6:
          // Search items
          searchItems();
          break;

        default:
          viewer.displayErrorMessage("Incorrect input. Please try again.");
          break;

      }
    }
  }

}
