// Тонкая обёртка над fetch: подставляет Basic-заголовок и разбирает ошибки (RFC 7807).

const SESSION_KEY = 'auth';
let authHeader = sessionStorage.getItem(SESSION_KEY);
let onUnauthorized = () => {};

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler;
}

export function hasCredentials() {
  return Boolean(authHeader);
}

export function setCredentials(username, password) {
  const bytes = new TextEncoder().encode(`${username}:${password}`);
  authHeader = 'Basic ' + btoa(String.fromCharCode(...bytes));
}

export function saveSession() {
  sessionStorage.setItem(SESSION_KEY, authHeader);
}

export function clearCredentials() {
  authHeader = null;
  sessionStorage.removeItem(SESSION_KEY);
}

function errorMessage(problem, status) {
  if (problem.errors) return Object.values(problem.errors).join('; ');
  return problem.detail || `Ошибка ${status}`;
}

async function request(method, url, body, { auth = true } = {}) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (auth && authHeader) headers.Authorization = authHeader;

  const response = await fetch(url, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.ok) return response.status === 204 ? null : response.json();

  const problem = await response.json().catch(() => ({}));
  if (response.status === 401 && auth) onUnauthorized();
  throw new ApiError(response.status, errorMessage(problem, response.status));
}

export const register = (username, password) =>
  request('POST', '/api/auth/register', { username, password }, { auth: false });
export const me = () => request('GET', '/api/auth/me');

export const listNotes = () => request('GET', '/api/notes');
export const createNote = (note) => request('POST', '/api/notes', note);
export const updateNote = (id, note) => request('PUT', `/api/notes/${id}`, note);
export const deleteNote = (id) => request('DELETE', `/api/notes/${id}`);
