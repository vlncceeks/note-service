
package app.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import app.dto.NoteRequest;
import app.dto.NoteResponse;
import app.entity.Note;
import app.exception.NotFoundException;
import app.repository.NoteRepository;
import app.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NoteService {

  private static final Logger log =
      LoggerFactory.getLogger(NoteService.class);

  private final NoteRepository noteRepository;
  private final UserRepository userRepository;
  private final MeterRegistry meterRegistry;

  private final int createFailureEvery;
  private final int updateFailureEvery;
  private final int deleteFailureEvery;

  private final AtomicInteger createCounter = new AtomicInteger();
  private final AtomicInteger updateCounter = new AtomicInteger();
  private final AtomicInteger deleteCounter = new AtomicInteger();

  public NoteService(
      NoteRepository noteRepository,
      UserRepository userRepository,
      MeterRegistry meterRegistry,
      @Value("${demo.failure.create-every:0}") int createFailureEvery,
      @Value("${demo.failure.update-every:0}") int updateFailureEvery,
      @Value("${demo.failure.delete-every:0}") int deleteFailureEvery
  ) {
    this.noteRepository = noteRepository;
    this.userRepository = userRepository;
    this.meterRegistry = meterRegistry;
    this.createFailureEvery = createFailureEvery;
    this.updateFailureEvery = updateFailureEvery;
    this.deleteFailureEvery = deleteFailureEvery;
  }

  private boolean shouldFail(AtomicInteger counter, int every) {
    return every > 0
        && counter.incrementAndGet() % every == 0;
  }

  @Transactional(readOnly = true)
  public NoteResponse find(Long id, Long userId) {
    return NoteResponse.from(getOwned(id, userId));
  }

  @Transactional(readOnly = true)
  public List<NoteResponse> findAll(Long userId) {
    meterRegistry.counter("notes.findAll").increment();

    return noteRepository.findByUserIdOrderByIdDesc(userId)
        .stream()
        .map(NoteResponse::from)
        .toList();
  }

  @Transactional
  public NoteResponse create(NoteRequest request, Long userId) {

    if (shouldFail(createCounter, createFailureEvery)) {
      meterRegistry.counter("notes.create.failures").increment();
      log.warn("Artificial create failure: userId={}", userId);

      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Artificial create failure"
      );
    }

    Note note = new Note();
    note.setUser(userRepository.getReferenceById(userId));
    note.setTitle(request.title());
    note.setContent(request.content());

    noteRepository.save(note);

    meterRegistry.counter("notes.created").increment();

    log.info("Note created: id={}, userId={}", note.getId(), userId);

    return NoteResponse.from(note);
  }

  @Transactional
  public NoteResponse update(
      Long id,
      Long userId,
      NoteRequest request
  ) {

    if (shouldFail(updateCounter, updateFailureEvery)) {
      meterRegistry.counter("notes.update.failures").increment();
      log.warn("Artificial update failure: id={}, userId={}", id, userId);

      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Artificial update failure"
      );
    }

    Note note = getOwned(id, userId);
    note.setTitle(request.title());
    note.setContent(request.content());

    meterRegistry.counter("notes.updated").increment();

    log.info("Note updated: id={}, userId={}", id, userId);

    return NoteResponse.from(note);
  }

  @Transactional
  public void delete(Long id, Long userId) {

    if (shouldFail(deleteCounter, deleteFailureEvery)) {
      meterRegistry.counter("notes.delete.failures").increment();
      log.warn("Artificial delete failure: id={}, userId={}", id, userId);

      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "Artificial delete failure"
      );
    }

    noteRepository.delete(getOwned(id, userId));

    meterRegistry.counter("notes.deleted").increment();

    log.info("Note deleted: id={}, userId={}", id, userId);
  }

  private Note getOwned(Long id, Long userId) {
    return noteRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> {
          log.warn(
              "Note not found: noteId={}, userId={}",
              id,
              userId
          );
          return new NotFoundException("Note " + id + " not found");
        });
  }
}
