# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Beverage Buddy is a demo Vaadin web application written in Kotlin, using Ktorm for database access. It manages beverage reviews and categories with a server-side rendered UI.

## Build & Run Commands

```bash
./gradlew build                              # Build and run tests
./gradlew run                                # Run the app (http://localhost:8080)
./gradlew test                               # Run all tests
./gradlew test --tests "*CategoryTest"       # Run a single test class
./gradlew build -Pvaadin.productionMode      # Production build
```

Requires JDK 21. Gradle 9.4.0 via wrapper.

## Architecture

Three-tier server-side app: Vaadin UI → Ktorm entities → H2 in-memory database.

**Backend** (`backend/`): Ktorm entity classes (`Category`, `Review`) using the ActiveEntity pattern with companion object DAOs. `ReviewWithCategory` is a join DTO. `RestService` exposes `/rest/categories` and `/rest/reviews` via Javalin servlet.

**UI** (`ui/`): Vaadin Flow views built with Karibu DSL. `MainLayout` is the router layout. `ReviewsList` (route `/`) uses a VirtualList. `CategoriesList` (route `/categories`) uses a Grid. Both use `Toolbar` for search/create and editor dialogs for CRUD.

**Database**: H2 in-memory, initialized in `Bootstrap.kt` with HikariCP pooling. Schema managed by Flyway migrations in `src/main/resources/db/migration/`. Demo data populated on startup via `DemoData`.

## Key Libraries

- **Vaadin 25.x + Vaadin Boot** — server-side UI framework with embedded Jetty
- **Karibu DSL** — Kotlin DSL for building Vaadin components (`ui { verticalLayout { ... } }`)
- **Ktorm** — SQL framework with active record entities; use `db { }` blocks for database operations
- **Karibu Testing** — browserless Vaadin UI testing (no browser/Selenium needed)
- **Flyway** — database migrations
- **Javalin** — lightweight REST endpoints (mounted as servlet, cannot upgrade to 6+ until Javalin 7 due to Jetty version)

## Testing

Tests use JUnit Jupiter + Karibu Testing for browserless UI testing. `AbstractAppTest` is the base class that sets up/tears down the mock Vaadin environment and resets the database before each test. Karibu helpers like `_get<T>()`, `_click()`, `_expectOne<T>()` are used to interact with UI components without a browser.

## Code Patterns

- Entities extend `ActiveEntity<T>` with `save()`, `delete()`, and static finder methods on companion objects
- UI components use `KComposite` with Karibu DSL builders
- Bean validation annotations (`@NotBlank`, `@Min`, `@Max`, `@PastOrPresent`) on entity fields
- DataProviders connect Grids/VirtualLists to Ktorm queries with lazy loading
