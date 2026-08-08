package com.borrowbox.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.borrowbox.model.Item;
import com.borrowbox.service.LendingService;
import com.borrowbox.model.Member;
import com.borrowbox.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The catalogue endpoints, including the search strategies behind the query
 * parameters.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private MemberService members;

  @Autowired
  private LendingService lending;

  private Member alice() {
    return members.getAllMembers().get(0);
  }

  private Item laptop() {
    return alice().getOwnedItems().get(0);
  }

  @Test
  @DisplayName("GET /api/items lists the whole catalogue")
  void listsTheCatalogue() throws Exception {
    mvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[*].name", containsInAnyOrder("Laptop", "Mountain bike")))
        .andExpect(jsonPath("$[0].ownerName").value("Alice"))
        .andExpect(jsonPath("$[0].availableToday").value(true));
  }

  @Test
  @DisplayName("GET /api/items?name= filters by name, ignoring case")
  void filtersByName() throws Exception {
    mvc.perform(get("/api/items").param("name", "LAPTOP"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Laptop"));
  }

  @Test
  @DisplayName("GET /api/items?maxPrice= filters by daily cost")
  void filtersByPrice() throws Exception {
    mvc.perform(get("/api/items").param("maxPrice", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Mountain bike"));
  }

  @Test
  @DisplayName("GET /api/items narrows by both filters at once")
  void filtersByBoth() throws Exception {
    mvc.perform(get("/api/items").param("name", "a").param("maxPrice", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name").value("Mountain bike"));
  }

  @Test
  @DisplayName("GET /api/items?maxPrice= answers 400 when it is not a number")
  void nonNumericPriceIs400() throws Exception {
    mvc.perform(get("/api/items").param("maxPrice", "cheap"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("\"cheap\" is not a number of credits."));
  }

  @Test
  @DisplayName("GET /api/items/{id} includes the loans booked against it")
  void includesBookedLoans() throws Exception {
    Member bob = members.getAllMembers().get(1);
    bob.addCredits(500);
    lending.lend(laptop(), bob, 2, 4);

    mvc.perform(get("/api/items/" + laptop().getItemId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.contracts", hasSize(1)))
        .andExpect(jsonPath("$.contracts[0].borrowerName").value("Bob"))
        .andExpect(jsonPath("$.contracts[0].cost").value(150));
  }

  @Test
  @DisplayName("GET /api/items/{id} answers 404 for an unknown id")
  void unknownItemIs404() throws Exception {
    mvc.perform(get("/api/items/zzz"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No item with id zzz."));
  }

  @Test
  @DisplayName("POST /api/items lists an item and pays the owner the bonus")
  void listsANewItem() throws Exception {
    String body = """
        {"ownerId": "%s", "name": "Cordless Drill", "description": "18V",
         "category": "Tools", "costPerDay": 15}
        """.formatted(alice().getMemberId());
    int creditsBefore = alice().getCredits();

    mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Cordless Drill"))
        .andExpect(jsonPath("$.ownerName").value("Alice"));

    assertThat(alice().getCredits()).isEqualTo(creditsBefore + 100);
    assertThat(members.getAllItems()).hasSize(3);
  }

  @Test
  @DisplayName("POST /api/items answers 404 when the owner does not exist")
  void unknownOwnerIs404() throws Exception {
    String body = """
        {"ownerId": "zzzzzz", "name": "Drill", "description": "18V",
         "category": "Tools", "costPerDay": 15}
        """;

    mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/items answers 400 for a negative daily cost")
  void negativeCostIs400() throws Exception {
    String body = """
        {"ownerId": "%s", "name": "Drill", "description": "18V",
         "category": "Tools", "costPerDay": -5}
        """.formatted(alice().getMemberId());

    mvc.perform(post("/api/items").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.costPerDay").value("A daily cost cannot be negative"));
  }

  @Test
  @DisplayName("PUT /api/items/{id} changes the details but not the owner")
  void updatesAnItem() throws Exception {
    String body = """
        {"name": "Gaming laptop", "description": "RTX", "category": "Electronics", "costPerDay": 70}
        """;

    mvc.perform(put("/api/items/" + laptop().getItemId())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Gaming laptop"))
        .andExpect(jsonPath("$.costPerDay").value(70))
        .andExpect(jsonPath("$.ownerName").value("Alice"));
  }

  @Test
  @DisplayName("DELETE /api/items/{id} removes an item nobody has booked")
  void deletesAnUnbookedItem() throws Exception {
    mvc.perform(delete("/api/items/" + laptop().getItemId()))
        .andExpect(status().isNoContent());

    assertThat(members.getAllItems()).hasSize(1);
  }

  @Test
  @DisplayName("DELETE /api/items/{id} answers 409 when loans are booked against it")
  void deletingABookedItemIs409() throws Exception {
    Member bob = members.getAllMembers().get(1);
    bob.addCredits(500);
    lending.lend(laptop(), bob, 2, 4);

    mvc.perform(delete("/api/items/" + laptop().getItemId()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Laptop has loans booked against it and cannot be removed."));

    assertThat(members.getAllItems()).hasSize(2);
  }
}
