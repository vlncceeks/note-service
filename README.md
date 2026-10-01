# Notes (простое CRUD-приложение)

Запуск: `docker compose up --build`, затем http://localhost:3000

Стек: Spring Boot 3 (Java 22) + PostgreSQL + статический HTML/JS за nginx.
Конфигурация - переменные окружения (см. .env, application.properties).

API (Basic Auth, кроме регистрации):
- POST /api/auth/register  {username,password}
- GET  /api/auth/me
- GET/POST /api/notes, GET/PUT/DELETE /api/notes/{id}
