package com.borrowbox.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.borrowbox.model.Item;
import com.borrowbox.model.Member;
import com.borrowbox.model.MemberList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Booking a loan over HTTP, advancing the clock, and reading the activity feed.
 * These three are tested together because that is how they are used: book
 * something, push the calendar, watch what the day announced.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LendingApiTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private MemberList members;

  private Member alice;
  private Member bob;
  private Item laptop;

  @BeforeEach
  void setUp() {
    alice = members.getAllMembers().get(0);
    bob = members.getAllMembers().get(1);
    laptop = alice.getOwnedItems().get(0);
    bob.addCredits(1000);
  }

  private ResultActions book(String itemId, String borrowerId, int startDay, int endDay) throws Exception {
    String body = """
        {"itemId": "%s", "borrowerId": "%s", "startDay": %d, "endDay": %d}
        """.formatted(itemId, borrowerId, startDay, endDay);
    return mvc.perform(post("/api/contracts").contentType(MediaType.APPLICATION_JSON).content(body));
  }

  @Test
  @DisplayName("POST /api/contracts books an item and moves the credits")
  void booksAnItem() throws Exception {
    int aliceBefore = alice.getCredits();
    int bobBefore = bob.getCredits();

    book(laptop.getItemId(), bob.getMemberId(), 2, 4)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.itemName").value("Laptop"))
        .andExpect(jsonPath("$.lenderName").value("Alice"))
        .andExpect(jsonPath("$.borrowerName").value("Bob"))
        .andExpect(jsonPath("$.durationInDays").value(3))
        .andExpect(jsonPath("$.cost").value(150));

    assertThat(bob.getCredits()).isEqualTo(bobBefore - 150);
    assertThat(alice.getCredits()).isEqualTo(aliceBefore + 150);
  }

  @Test
  @DisplayName("POST /api/contracts answers 422 when the item is already booked")
  void doubleBookingIs422() throws Exception {
    book(laptop.getItemId(), bob.getMemberId(), 2, 6).andExpect(status().isCreated());

    book(laptop.getItemId(), bob.getMemberId(), 6, 8)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.title").value("Not allowed"))
        .andExpect(jsonPath("$.detail").value("The item is already booked for part of that period."));
  }

  @Test
  @DisplayName("POST /api/contracts answers 422 when the borrower cannot pay")
  void tooExpensiveIs422() throws Exception {
    Member sid = members.getAllMembers().get(2);

    book(laptop.getItemId(), sid.getMemberId(), 2, 6)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.detail").value("The borrower has 100 credits but this loan costs 250."));
  }

  @Test
  @DisplayName("POST /api/contracts answers 422 when borrowing your own item")
  void borrowingYourOwnItemIs422() throws Exception {
    book(laptop.getItemId(), alice.getMemberId(), 2, 4)
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.detail").value("A member cannot borrow their own item."));
  }

  @Test
  @DisplayName("POST /api/contracts answers 404 for an unknown item")
  void unknownItemIs404() throws Exception {
    book("zzz", bob.getMemberId(), 2, 4).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /api/contracts can be narrowed to one member")
  void listsContractsForOneMember() throws Exception {
    book(laptop.getItemId(), bob.getMemberId(), 2, 4).andExpect(status().isCreated());
    Member sid = members.getAllMembers().get(2);

    mvc.perform(get("/api/contracts")).andExpect(jsonPath("$", hasSize(1)));
    mvc.perform(get("/api/contracts").param("memberId", bob.getMemberId()))
        .andExpect(jsonPath("$", hasSize(1)));
    mvc.perform(get("/api/contracts").param("memberId", sid.getMemberId()))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  @DisplayName("GET /api/clock reports the current day")
  void reportsTheCurrentDay() throws Exception {
    mvc.perform(get("/api/clock"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentDay").value(0))
        .andExpect(jsonPath("$.events", hasSize(0)));
  }

  @Test
  @DisplayName("POST /api/clock/advance reports what the new day brought")
  void advancingReportsTheDaysEvents() throws Exception {
    book(laptop.getItemId(), bob.getMemberId(), 1, 2).andExpect(status().isCreated());

    mvc.perform(post("/api/clock/advance"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentDay").value(1))
        .andExpect(jsonPath("$.events", hasSize(2)))
        .andExpect(jsonPath("$.events[0].type").value("DAY_ADVANCED"))
        .andExpect(jsonPath("$.events[1].type").value("LOAN_STARTED"))
        .andExpect(jsonPath("$.events[1].description").value("Bob picks up Laptop from Alice."));

    mvc.perform(post("/api/clock/advance"))
        .andExpect(jsonPath("$.currentDay").value(2))
        .andExpect(jsonPath("$.events[1].type").value("LOAN_ENDED"));
  }

  @Test
  @DisplayName("GET /api/events returns the feed newest first")
  void returnsTheFeedNewestFirst() throws Exception {
    book(laptop.getItemId(), bob.getMemberId(), 1, 2).andExpect(status().isCreated());
    mvc.perform(post("/api/clock/advance"));

    mvc.perform(get("/api/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].type").value("LOAN_STARTED"))
        .andExpect(jsonPath("$[2].type").value("LOAN_AGREED"));
  }

  @Test
  @DisplayName("GET /api/events honours a limit and rejects a silly one")
  void honoursTheLimit() throws Exception {
    book(laptop.getItemId(), bob.getMemberId(), 1, 2).andExpect(status().isCreated());
    mvc.perform(post("/api/clock/advance"));

    mvc.perform(get("/api/events").param("limit", "1")).andExpect(jsonPath("$", hasSize(1)));
    mvc.perform(get("/api/events").param("limit", "0")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/events").param("limit", "9999")).andExpect(status().isBadRequest());
  }
}
