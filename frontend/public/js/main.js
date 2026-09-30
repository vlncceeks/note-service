// Точка входа: переключение между экраном входа и экраном заметок.
import * as api from './api.js';
import { initAuth } from './auth.js';
import { clearNotes, initNotes, loadNotes } from './notes.js';

const authView = document.getElementById('auth-view');
const notesView = document.getElementById('notes-view');
const userBox = document.getElementById('user-box');
const usernameEl = document.getElementById('username');

function showApp(user) {
  usernameEl.textContent = user.username;
  authView.classList.add('hidden');
  notesView.classList.remove('hidden');
  userBox.classList.remove('hidden');
  loadNotes();
}

function showAuth() {
  api.clearCredentials();
  clearNotes();
  notesView.classList.add('hidden');
  userBox.classList.add('hidden');
  authView.classList.remove('hidden');
}

api.setUnauthorizedHandler(showAuth);
initAuth(showApp);
initNotes();
document.getElementById('logout-btn').addEventListener('click', showAuth);

// Восстанавливаем сессию после перезагрузки страницы
if (api.hasCredentials()) {
  api.me().then(showApp).catch(showAuth);
}
