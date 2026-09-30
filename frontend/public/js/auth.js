// Форма входа и регистрации.
import * as api from './api.js';

const form = document.getElementById('auth-form');
const errorEl = document.getElementById('auth-error');
const submitBtn = document.getElementById('auth-submit');
const tabs = document.querySelectorAll('.tab');
let mode = 'login';

function setMode(newMode) {
  mode = newMode;
  tabs.forEach((tab) => tab.classList.toggle('active', tab.dataset.mode === mode));
  submitBtn.textContent = mode === 'login' ? 'Войти' : 'Зарегистрироваться';
  form.elements.password.autocomplete = mode === 'login' ? 'current-password' : 'new-password';
  errorEl.textContent = '';
}

export function initAuth(onSuccess) {
  tabs.forEach((tab) => tab.addEventListener('click', () => setMode(tab.dataset.mode)));

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    errorEl.textContent = '';
    const username = form.elements.username.value.trim();
    const password = form.elements.password.value;

    try {
      if (mode === 'register') await api.register(username, password);
      api.setCredentials(username, password);
      const user = await api.me();
      api.saveSession();
      form.reset();
      onSuccess(user);
    } catch (err) {
      api.clearCredentials();
      errorEl.textContent = err.status === 401 ? 'Неверный логин или пароль' : err.message;
    }
  });
}
