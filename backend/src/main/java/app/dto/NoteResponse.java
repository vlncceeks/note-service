package app.dto;

import app.entity.Note;

public record NoteResponse(Long id, String title, String content) {
  public static NoteResponse from(Note note) {
    return new NoteResponse(note.getId(), note.getTitle(), note.getContent());
  }
}
