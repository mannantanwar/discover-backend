# My Learning Notes — Discover Backend

> This file is mine, not the assistant's — it doesn't get auto-updated. Add to it whenever something clicks (or finally clicks after three tries). Doesn't need to be formal — write it however makes sense to you. Unlike `Discover_Progress_Log.md` (which tracks project status), this tracks *my own* understanding.

---

## How to use this

Whenever you learn something — a concept, a gotcha, a "why didn't I know this sooner" — drop a short entry under the right section (or add a new section if nothing fits). A few words is fine. The point is having your own words to look back on, not a textbook.

Suggested entry format, but not mandatory:
```
### <topic>
What it is, in my own words:
Why it matters / when I'd use it:
Where I used it in this project:
```

---

## External chats / resources

- [ChatGPT conversation](https://chatgpt.com/share/6a64a5fd-0e88-83ee-ad09-e3ffdd7cd817) — side learning and doubts asked outside this project's main working sessions.
- [ChatGPT conversation](https://chatgpt.com/share/6a651c3c-9c94-83ee-bc23-2ff024f371bf) — more side learning/doubts. --> contains the jwtstructure adn bean doubts 

---

## Java & Spring Boot

*(e.g. constructor injection, `@Service`/`@Repository`/`@Entity`, Optional, Lombok annotations...)*

## JPA / Hibernate

*(e.g. `@ManyToOne` vs `@ManyToMany`, `@CreationTimestamp`, `ddl-auto: validate`, JSONB mapping...)*

## Databases / SQL

*(e.g. indexes, BIGSERIAL, migrations, Postgres vs standard SQL...)*

## Docker

Commands actually used on this project so far, and why each one:

### Checking Docker itself is alive
- `docker --version` — confirms the Docker CLI is installed and on PATH. Doesn't tell you if the engine is running, just that the command exists.
- `docker info` — confirms the actual Docker *daemon/engine* is reachable, not just the CLI. If Docker Desktop isn't running, this fails even though `docker --version` succeeds — this distinction is what caught our "Docker Desktop installed but not open" issue early on.

### Starting/stopping our services (`docker-compose.yml`)
- `docker compose up -d` — reads `docker-compose.yml` and starts everything defined in it (our Postgres+PostGIS container). `-d` = detached, runs in the background instead of blocking the terminal.
- `docker compose ps` — lists the containers belonging to *this* project's compose file specifically (as opposed to `docker ps`, which lists everything running on the whole machine). Used to confirm our container was actually `Up` and check its port mapping.
- `docker compose down` — stops and removes the containers (but not the data — see volumes below).
- `docker compose down -v` — same, but **also deletes the named volumes** — the actual "wipe the database" command. Not used yet on purpose; know it exists, be careful with it.
- `docker compose logs postgres` — prints everything Postgres has logged since it started, same as watching its console output directly. Used to confirm `initdb` actually ran fresh (vs. reusing old data) while debugging the auth issue.

### Talking to the database directly, without the Java app
- `docker compose exec postgres psql -U user -d discover` — opens a real interactive SQL shell **inside** the running container, logged in as our `user` role. This is how we ran ad-hoc queries (`\dt`, `SELECT PostGIS_Version();`, etc.) without going through the app at all.
- `docker compose exec -T postgres psql -U user -d discover -c "..."` — same idea, but runs one SQL command and exits, instead of opening an interactive session. The `-T` disables the pseudo-terminal, needed when running it as a single scripted command rather than something a human types into.

### Inspecting what's actually running/stored (used heavily while debugging the port 5432 conflict)
- `docker ps` — lists *every* running container on the machine, regardless of project. Different from `docker compose ps`, which is scoped to one compose file.
- `docker ps -a` — same, but includes stopped containers too.
- `docker inspect <container> --format '...'` — dumps detailed configuration about a specific container (we used it to check exactly which environment variables — `POSTGRES_USER`, `POSTGRES_PASSWORD` — the container actually launched with, to rule out a config mismatch).
- `docker volume ls` — lists Docker's named volumes (the persistent storage areas, separate from any one container's lifecycle).
- `docker volume inspect <name>` — shows where a volume actually lives and which project/compose file owns it.

### One-off throwaway containers, for testing
- `docker run --rm -e PGPASSWORD=root postgis/postgis:16-3.4 psql -h ... -U user -d discover -c "SELECT 1;"` — spins up a brand-new, temporary container just to run one command, then deletes itself (`--rm`). Used this repeatedly while diagnosing the auth bug, to test the exact same connection from different network paths (inside Docker's network vs. across the Windows/WSL2 relay) without needing a real client installed anywhere.

## Git / GitHub

*(e.g. local vs remote, staging vs committing, .gitignore...)*

## System design principles

*(e.g. Separation of Concerns, Dependency Inversion, YAGNI, when an interface is/isn't worth it...)*

## Debugging war stories

*(The annoying ones worth remembering — e.g. the port 5432 conflict, the wrong `User` import from Spring Security...)*

## Questions / things to revisit later

*(Anything you were told but didn't fully click yet — come back to these.)*
