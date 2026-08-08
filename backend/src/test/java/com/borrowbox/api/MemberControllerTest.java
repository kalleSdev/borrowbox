package com.borrowbox.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * The members endpoints, driven the way a client would drive them.
 *
 * <p>The member list is a singleton the whole application shares, so each test
 * gets a fresh context rather than inheriting whatever the last one left behind.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

  private static final String ADA = """
      {"name": "Ada", "email": "ada@example.com", "mobile": "0700000099"}
      """;

  @Autowired
  private MockMvc mvc;

  @Autowired
  private MemberService members;

  private String firstSeededMemberId() {
    return members.getAllMembers().get(0).getMemberId();
  }

  @Test
  @DisplayName("GET /api/members lists the seeded members")
  void listsMembers() throws Exception {
    mvc.perform(get("/api/members"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(3)))
        .andExpect(jsonPath("$[0].name").value("Alice"))
        .andExpect(jsonPath("$[0].credits").value(530));
  }

  @Test
  @DisplayName("GET /api/members/{id} returns one member")
  void returnsOneMember() throws Exception {
    mvc.perform(get("/api/members/" + firstSeededMemberId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Alice"))
        .andExpect(jsonPath("$.ownedItemCount").value(2));
  }

  @Test
  @DisplayName("GET /api/members/{id} answers 404 for an unknown id")
  void unknownMemberIs404() throws Exception {
    mvc.perform(get("/api/members/zzzzzz"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Not found"))
        .andExpect(jsonPath("$.detail").value("No member with id zzzzzz."));
  }

  @Test
  @DisplayName("POST /api/members signs up a member and says where to find them")
  void signsUpAMember() throws Exception {
    mvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(ADA))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.name").value("Ada"))
        .andExpect(jsonPath("$.credits").value(0))
        .andExpect(jsonPath("$.id").isNotEmpty());

    assertThat(members.getAllMembers()).hasSize(4);
  }

  @Test
  @DisplayName("POST /api/members answers 409 when the email is taken")
  void duplicateEmailIs409() throws Exception {
    mvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(ADA));

    mvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(ADA))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Already registered"));
  }

  @Test
  @DisplayName("POST /api/members answers 400 and names the bad fields")
  void invalidBodyIs400() throws Exception {
    String body = """
        {"name": "", "email": "not-an-email", "mobile": "0700000001"}
        """;

    mvc.perform(post("/api/members").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.name").value("A name is required"))
        .andExpect(jsonPath("$.errors.email").value("That does not look like an email address"));
  }

  @Test
  @DisplayName("PUT /api/members/{id} changes the details")
  void updatesAMember() throws Exception {
    String body = """
        {"name": "Alice B", "email": "alice.b@example.com", "mobile": "0700000009"}
        """;

    mvc.perform(put("/api/members/" + firstSeededMemberId())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Alice B"))
        .andExpect(jsonPath("$.email").value("alice.b@example.com"));
  }

  @Test
  @DisplayName("PUT /api/members/{id} answers 409 when the email belongs to someone else")
  void updateToATakenEmailIs409() throws Exception {
    String body = """
        {"name": "Alice", "email": "bob@example.com", "mobile": "0700000001"}
        """;

    mvc.perform(put("/api/members/" + firstSeededMemberId())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("DELETE /api/members/{id} removes the member")
  void deletesAMember() throws Exception {
    mvc.perform(delete("/api/members/" + firstSeededMemberId()))
        .andExpect(status().isNoContent());

    assertThat(members.getAllMembers()).hasSize(2);
  }

  @Test
  @DisplayName("DELETE /api/members/{id} answers 404 for an unknown id")
  void deleteUnknownIs404() throws Exception {
    mvc.perform(delete("/api/members/zzzzzz"))
        .andExpect(status().isNotFound());
  }
}
