# Repository Guidelines

## Project Structure & Module Organization
This repository contains two main modules:
- `tourism_back_end`: Spring Boot API (`src/main/java/com/example/tourism_rear`) with `controller`, `service`, `service/impl`, `mapper`, `config`, and `interceptor` packages.
- `tourism_front_end`: Vue 2 app with source in `src/` (`views`, `components`, `router`, `store`, `utils`) and static files in `src/assets`.

Backend MyBatis XML mappings live in `tourism_back_end/src/main/resources/com/example/tourism_rear/mapper`, and SQL scripts/migrations are in `tourism_back_end/src/main/resources/sql`.

## Build, Test, and Development Commands
Backend (`tourism_back_end`):
- `./mvnw spring-boot:run` or `mvnw.cmd spring-boot:run`: run API locally (default port `8080`).
- `./mvnw test` or `mvnw.cmd test`: run JUnit tests.
- `./mvnw clean package`: build artifact.

Frontend (`tourism_front_end`):
- `npm install`: install dependencies.
- `npm run serve`: start Vue dev server (configured for port `7070`).
- `npm run build`: create production bundle in `dist/`.
- `npm run lint`: run ESLint checks.

## Coding Style & Naming Conventions
- Java: 4-space indentation, `PascalCase` classes, `camelCase` methods/fields.
- Vue/JS: 2-space indentation, `PascalCase` component/view filenames (for example `UserHome.vue`).
- Keep MyBatis mapper interface and XML names aligned (for example `UserMapper.java` and `UserMapper.xml`).
- Prefer clear, module-scoped names (`OrderController`, `OrderService`, `OrderMapper`).

## Testing Guidelines
- Backend tests use Spring Boot + JUnit 5 under `tourism_back_end/src/test/java` with `*Tests.java` naming.
- Add or update tests for backend logic changes, especially service and controller behavior.
- Frontend has no dedicated automated test suite yet; run `npm run lint` and manually verify changed views/flows.

## Commit & Pull Request Guidelines
- Existing history favors short, descriptive commit messages (often Chinese summaries). Keep one logical change per commit.
- Recommended format: `<module>: <what changed>` (example: `front_end: adjust checkout status display`).
- PRs should include: scope summary, affected module(s), verification steps, related SQL/script changes, and UI screenshots for frontend updates.

## Security & Configuration Tips
- Never commit secrets. Ignored files include `config/application-secrets.yml` and `tourism_back_end/config/application-secrets.yml`.
- Start from `config/application-secrets.example.yml`, then set local values via secrets file or env vars (`DB_*`, `MAIL_*`, `JWT_SECRET`, `QWEN_API_KEY`).
