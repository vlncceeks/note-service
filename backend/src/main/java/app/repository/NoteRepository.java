package app.repository;

import java.util.List;
import java.util.Optional;

import app.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
  List<Note> findByUserIdOrderByIdDesc(Long userId);

  Optional<Note> findByIdAndUserId(Long id, Long userId);
}
