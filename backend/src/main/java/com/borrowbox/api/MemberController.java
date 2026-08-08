package com.borrowbox.api;

import com.borrowbox.api.dto.MemberRequest;
import com.borrowbox.api.dto.MemberResponse;
import com.borrowbox.model.Member;
import com.borrowbox.service.MemberService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Members: signing up, looking up, editing and removing.
 */
@RestController
@RequestMapping("/api/members")
public class MemberController {

  private final MemberService members;

  public MemberController(MemberService members) {
    this.members = members;
  }

  @GetMapping
  public List<MemberResponse> list() {
    return members.getAllMembers().stream().map(MemberResponse::from).toList();
  }

  @GetMapping("/{id}")
  public MemberResponse get(@PathVariable String id) {
    return MemberResponse.from(members.requireMemberById(id));
  }

  /**
   * Signs up a new member and points at where to find them.
   */
  @PostMapping
  public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request) {
    Member member = members.register(request.name(), request.email(), request.mobile());
    return ResponseEntity
        .created(URI.create("/api/members/" + member.getMemberId()))
        .body(MemberResponse.from(member));
  }

  /**
   * Replaces a member's details.
   */
  @PutMapping("/{id}")
  public MemberResponse update(@PathVariable String id, @Valid @RequestBody MemberRequest request) {
    Member member = members.requireMemberById(id);
    members.changeMemberInformation(id, request.name(), request.email(), request.mobile());
    return MemberResponse.from(member);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    members.requireMemberById(id);
    members.deleteMember(id);
    return ResponseEntity.noContent().build();
  }
}
