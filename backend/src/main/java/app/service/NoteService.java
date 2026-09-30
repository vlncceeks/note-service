package app.service;

import java.util.List;

import app.dto.NoteRequest;
import app.dto.NoteResponse;
import app.entity.Note;
import app.exception.NotFoundException;
import app.repository.NoteRepository;
import app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteService {
  private static final Logger log = LoggerFactory.getLogger(NoteService.class);

  private final NoteRepository noteRepository;
  private final UserRepository userRepository;

  public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
    this.noteRepository = noteRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public NoteResponse find(Long id, Long userId) {
    return NoteResponse.from(getOwned(id, userId));
  }

  @Transactional(readOnly = true)
  public List<NoteResponse> findAll(Long userId) {
    return noteRepository.findByUserIdOrderByIdDesc(userId).stream()
        .map(NoteResponse::from)
        .toList();
  }

  @Transactional
  public NoteResponse create(NoteRequest request, Long userId) {
    Note note = new Note();
    note.setUser(userRepository.getReferenceById(userId));
    note.setTitle(request.title());
    note.setContent(request.content());
    noteRepository.save(note);
    log.info("Note created: id={}, userId={}", note.getId(), userId);
    return NoteResponse.from(note);
  }

  @Transactional
  public NoteResponse update(Long id, Long userId, NoteRequest request) {
    Note note = getOwned(id, userId);
    note.setTitle(request.title());
    note.setContent(request.content());
    log.info("Note updated: id={}, userId={}", id, userId);
    return NoteResponse.from(note);
  }

  @Transactional
  public void delete(Long id, Long userId) {
    noteRepository.delete(getOwned(id, userId));
    log.info("Note deleted: id={}, userId={}", id, userId);
  }

  private Note getOwned(Long id, Long userId) {
    return noteRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new NotFoundException("Note " + id + " not found"));
  }
}
