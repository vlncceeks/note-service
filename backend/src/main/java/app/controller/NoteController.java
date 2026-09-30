package app.controller;

import java.net.URI;
import java.util.List;

import app.dto.NoteRequest;
import app.dto.NoteResponse;
import app.entity.User;
import app.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
  private final NoteService noteService;

  public NoteController(NoteService noteService) {
    this.noteService = noteService;
  }

  @GetMapping
  public ResponseEntity<List<NoteResponse>> list(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok().body(noteService.findAll(user.getId()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<NoteResponse> get(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return ResponseEntity.ok().body(noteService.find(id, user.getId()));
  }

  @PostMapping
  public ResponseEntity<NoteResponse> create(
      @Valid @RequestBody NoteRequest request, @AuthenticationPrincipal User user) {
    NoteResponse note = noteService.create(request, user.getId());
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(note.id()).toUri();
    return ResponseEntity.created(location).body(note);
  }

  @PutMapping("/{id}")
  public ResponseEntity<NoteResponse> update(
      @PathVariable Long id, @Valid @RequestBody NoteRequest request, @AuthenticationPrincipal User user) {
    return ResponseEntity.ok().body(noteService.update(id, user.getId(), request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
    noteService.delete(id, user.getId());
    return ResponseEntity.noContent().build();
  }
}
