// Список заметок и форма создания/редактирования.
import * as api from './api.js';

const form = document.getElementById('note-form');
const formTitle = document.getElementById('form-title');
const submitBtn = document.getElementById('note-submit');
const cancelBtn = document.getElementById('note-cancel');
const errorEl = document.getElementById('note-error');
const listEl = document.getElementById('notes-list');
const emptyEl = document.getElementById('empty');
let editingId = null;

function resetForm() {
  editingId = null;
  form.reset();
  formTitle.textContent = 'Новая заметка';
  submitBtn.textContent = 'Добавить';
  cancelBtn.classList.add('hidden');
  errorEl.textContent = '';
}

function startEdit(note) {
  editingId = note.id;
  form.elements.title.value = note.title;
  form.elements.content.value = note.content;
  formTitle.textContent = 'Редактирование заметки';
  submitBtn.textContent = 'Сохранить';
  cancelBtn.classList.remove('hidden');
  errorEl.textContent = '';
  form.scrollIntoView({ behavior: 'smooth' });
  form.elements.title.focus();
}

function button(text, className, onClick) {
  const btn = document.createElement('button');
  btn.type = 'button';
  btn.className = `btn btn-sm ${className}`;
  btn.textContent = text;
  btn.addEventListener('click', onClick);
  return btn;
}

function noteCard(note) {
  const card = document.createElement('article');
  card.className = 'card note';

  const title = document.createElement('h3');
  title.textContent = note.title;
  const content = document.createElement('p');
  content.textContent = note.content;

  const actions = document.createElement('div');
  actions.className = 'actions';
  actions.append(
    button('Изменить', 'btn-ghost', () => startEdit(note)),
    button('Удалить', 'btn-danger', () => removeNote(note)),
  );

  card.append(title, content, actions);
  return card;
}

async function removeNote(note) {
  if (!confirm(`Удалить заметку «${note.title}»?`)) return;
  try {
    await api.deleteNote(note.id);
    if (editingId === note.id) resetForm();
    await loadNotes();
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

export async function loadNotes() {
  try {
    const notes = await api.listNotes();
    listEl.replaceChildren(...notes.map(noteCard));
    emptyEl.classList.toggle('hidden', notes.length > 0);
  } catch (err) {
    errorEl.textContent = err.message;
  }
}

export function clearNotes() {
  listEl.replaceChildren();
  resetForm();
}

export function initNotes() {
  cancelBtn.addEventListener('click', resetForm);

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const note = {
      title: form.elements.title.value.trim(),
      content: form.elements.content.value,
    };
    try {
      if (editingId) await api.updateNote(editingId, note);
      else await api.createNote(note);
      resetForm();
      await loadNotes();
    } catch (err) {
      errorEl.textContent = err.message;
    }
  });
}
