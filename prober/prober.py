
import os
import time
import secrets
import logging

import requests
from prometheus_client import Counter, Gauge, Histogram, start_http_server

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("prober")

BASE_URL = os.getenv(
    "BASE_URL",
    "http://note-service-backend:8080"
).rstrip("/")
INTERVAL = int(os.getenv("PROBE_INTERVAL", "30"))
TIMEOUT = 5

runs = Counter("prober_runs", "Total user scenarios executed")
failures = Counter(
    "prober_failures",
    "Failed user scenarios",
    ["step"]
)
duration = Histogram(
    "prober_duration_seconds",
    "Duration of the complete user scenario"
)
last_success = Gauge(
    "prober_last_success",
    "Whether the last complete scenario succeeded (1 or 0)"
)
step_success = Gauge(
    "prober_step_success",
    "Whether the last step succeeded (1 or 0)",
    ["step"]
)
scenarios_total = Counter(
    "prober_scenarios_total",
    "Total completed user scenarios"
)

scenarios_success_total = Counter(
    "prober_scenarios_success_total",
    "Successful user scenarios"
)

session = requests.Session()
username = "probe_" + secrets.token_hex(8)
password = secrets.token_hex(16)


def check(condition, message):
    if not condition:
        raise RuntimeError(message)


def request(method, path, **kwargs):
    response = session.request(
        method,
        BASE_URL + path,
        timeout=TIMEOUT,
        **kwargs
    )
    return response


def register_user():
    response = request(
        "POST",
        "/api/auth/register",
        json={"username": username, "password": password}
    )

    # If registration succeeded earlier but its response was lost,
    # try authenticating with the same generated credentials.
    if response.status_code == 201:
        log.info("Test user registered")
        return

    auth_response = request(
        "GET",
        "/api/auth/me",
        auth=(username, password)
    )
    check(
        auth_response.status_code == 200,
        f"Registration failed: HTTP {response.status_code}"
    )


def run_step(name, action):
    try:
        action()
        step_success.labels(step=name).set(1)
    except Exception:
        step_success.labels(step=name).set(0)
        failures.labels(step=name).inc()
        raise


def scenario():
    session.auth = (username, password)

    # Verify authentication.
    def check_auth():
        response = request("GET", "/api/auth/me")
        check(response.status_code == 200, "Authentication failed")
        check(
            response.json().get("username") == username,
            "Unexpected authenticated user"
        )

    run_step("auth", check_auth)

    note = {
        "title": "Prober test note",
        "content": "Created by automated reliability check"
    }
    created = {}

    def create_note():
        response = request("POST", "/api/notes", json=note)
        check(response.status_code == 201, "Create note failed")
        data = response.json()
        check(data.get("id") is not None, "Created note has no ID")
        check(data.get("title") == note["title"], "Wrong note title")
        created["id"] = data["id"]

    run_step("create", create_note)
    note_id = created["id"]

    def get_note():
        response = request("GET", f"/api/notes/{note_id}")
        check(response.status_code == 200, "Get note failed")
        check(response.json().get("content") == note["content"],
              "Wrong note content")

    run_step("get", get_note)

    def list_notes():
        response = request("GET", "/api/notes")
        check(response.status_code == 200, "List notes failed")
        check(
            any(item.get("id") == note_id for item in response.json()),
            "Created note not found in list"
        )

    run_step("list", list_notes)

    updated = {
        "title": "Prober updated note",
        "content": "Updated by automated reliability check"
    }

    def update_note():
        response = request(
            "PUT", f"/api/notes/{note_id}", json=updated
        )
        check(response.status_code == 200, "Update note failed")
        check(response.json().get("title") == updated["title"],
              "Updated title does not match")

    run_step("update", update_note)

    def delete_note():
        response = request("DELETE", f"/api/notes/{note_id}")
        check(response.status_code == 204, "Delete note failed")

        check(
            request("GET", f"/api/notes/{note_id}").status_code == 404,
            "Deleted note is still accessible"
        )

    run_step("delete", delete_note)


def main():
    start_http_server(8000)

    # Register once per prober process. Retry if the backend isn't ready.
    while True:
        try:
            register_user()
            break
        except Exception as exc:
            log.warning("Registration/auth setup failed: %s", exc)
            time.sleep(5)

    while True:
        started = time.monotonic()
        runs.inc()

        try:
            scenario()
            scenarios_success_total.inc()
            last_success.set(1)
            log.info("Scenario succeeded")
        except Exception:
            last_success.set(0)
            log.exception("Scenario failed")
        finally:
            scenarios_total.inc()

        duration.observe(time.monotonic() - started)
        time.sleep(INTERVAL)


if __name__ == "__main__":
    main()
