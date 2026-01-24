# Repository Guidelines

This repository contains a Spring Boot + MyBatis backend and a Vue 2 frontend for a tourism management system. Use the module sections below to stay consistent with the existing layout and tooling.

## Project Structure & Module Organization
- `tourism_back_end/`: Spring Boot app.
  - `src/main/java/com/example/tourism_rear/`: `controller/`, `service/`, `service/impl/`, `mapper/`, `config/`, `interceptor/`, `pack/`, and `TourismRearApplication.java`.
  - `src/main/resources/`: `application.yml` and MyBatis XMLs in `com/example/tourism_rear/mapper/`.
  - `src/test/java/...`: backend tests.
- `tourism_front_end/`: Vue 2 app.
  - `src/`: `views/`, `components/`, `router/`, `store/`, `assets/`, `main.js`.
  - `public/`: static assets and index template.

## Build, Test, and Development Commands
Backend (run from `tourism_back_end/`):
- `.\mvnw.cmd spring-boot:run` — start the API on port 8080.
- `.\mvnw.cmd test` — run JUnit tests.
- `.\mvnw.cmd clean package` — build the jar.

Frontend (run from `tourism_front_end/`):
- `npm install` — install dependencies.
- `npm run serve` — start the dev server.
- `npm run build` — production build.
- `npm run lint` — ESLint checks.

## Coding Style & Naming Conventions
- Java uses 4-space indentation and standard Spring naming: `*Controller`, `*Service`, `*ServiceImpl`, `*Mapper`, with matching `*Mapper.xml`.
- Package base is `com.example.tourism_rear`; keep new classes in the existing subpackages.
- Vue files follow PascalCase (e.g., `IndexView.vue`, `HelloWorld.vue`) with 2-space indentation.
- Linting: `vue-cli-service lint` with `plugin:vue/essential` and `eslint:recommended`.

## Testing Guidelines
- Backend tests are JUnit 5 under `tourism_back_end/src/test/java` (e.g., `*Tests` classes).
- No frontend test runner is configured; add tests only if you also add tooling.

## Commit & Pull Request Guidelines
- Git history shows short, descriptive Chinese messages (e.g., “首页更新”), not a strict convention.
- Keep commit messages brief and scoped to the change (module or feature).
- PRs should include: summary, affected module(s), testing performed, and UI screenshots for frontend changes.

## Configuration & Security Notes
- Backend DB settings live in `tourism_back_end/src/main/resources/application.yml` (MySQL `tourism`, default `root/123456`).
- Frontend API base URLs are in `tourism_front_end/src/axios.js` (`http://localhost:8080`).
- Use local overrides for credentials and avoid committing real secrets.
