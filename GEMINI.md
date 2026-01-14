# GEMINI.md - Project Context

This document provides a comprehensive overview of the `graduation_project` to be used as a context for AI-driven development.

## Project Overview

This is a full-stack web application for a tourism platform. It consists of a Java-based backend and a Vue.js frontend. The application appears to manage cultural products, user accounts, orders, and travel promotions. The project is structured as a monorepo with two main directories: `tourism_back_end` and `tourism_front_end`.

### Backend (`tourism_back_end`)

*   **Framework:** Spring Boot 3.3.4
*   **Language:** Java 17
*   **Build Tool:** Apache Maven
*   **Database:** MySQL
*   **Core Dependencies:**
    *   `spring-boot-starter-web`: For building RESTful APIs.
    *   `mybatis-spring-boot-starter`: For database persistence.
    *   `mysql-connector-j`: MySQL JDBC driver.
    *   `pagehelper-spring-boot-starter`: For pagination.
    *   `jjwt`: For creating and verifying JSON Web Tokens (JWT) for authentication.
*   **Architecture:** The backend follows a classic layered architecture with Controllers, Services, and Mappers (DAO). It exposes a REST API for the frontend to consume.

### Frontend (`tourism_front_end`)

*   **Framework:** Vue.js 2
*   **Build Tool:** Vue CLI
*   **Package Manager:** npm
*   **Core Dependencies:**
    *   `vue-router`: For client-side routing.
    *   `vuex`: For state management.
    *   `axios`: For making HTTP requests to the backend.
    *   `element-ui`: A UI component library.
    *   `echarts` / `v-charts`: For data visualization and charts.
*   **Architecture:** A single-page application (SPA) that communicates with the backend API. It implements role-based access control (RBAC) on the client-side based on a JWT. The project name in `package.json` is `medicine_system`, which might be a remnant from a template and doesn't match the tourism theme.

## Building and Running

### Backend

1.  **Prerequisites:**
    *   JDK 17
    *   Apache Maven
    *   A running MySQL instance.
2.  **Configuration:**
    *   Update the database connection details in `tourism_back_end/src/main/resources/application.yml`. The current configuration is:
        *   **URL:** `jdbc:mysql://localhost:3306/tourism`
        *   **Username:** `root`
        *   **Password:** `123456`
3.  **Running the application:**
    *   Navigate to the `tourism_back_end` directory.
    *   Run the command:
        ```bash
        mvn spring-boot:run
        ```
    *   The backend server will start on port `8080`.

### Frontend

1.  **Prerequisites:**
    *   Node.js and npm
2.  **Installation:**
    *   Navigate to the `tourism_front_end` directory.
    *   Run the command to install dependencies:
        ```bash
        npm install
        ```
3.  **Running the development server:**
    *   Run the command:
        ```bash
        npm run serve
        ```
    *   The frontend development server will start, typically on a port like `8081` (the exact port will be shown in the console output), and will proxy API requests to the backend.

## Development Conventions

*   **API:** The backend provides a RESTful API. API endpoints are defined in the `controller` package.
*   **Authentication:** The application uses JWT for authentication. The frontend sends the token in the `Authorization` header as a Bearer token.
*   **Routing:** Frontend routing is managed by `vue-router`. Route definitions and navigation guards are in `tourism_front_end/src/router/index.js`.
*   **State Management:** Frontend global state is managed by `vuex`. The store is configured in `tourism_front_end/src/store/index.js`.
*   **User Roles:** The application has a role-based access system with the following roles (as inferred from the frontend router):
    *   `1`: Administrator
    *   `2`: Travel Consultant
    *   `3`: Customer
*   **Code Style:** No strict linting or formatting rules are immediately apparent, but the code is generally well-structured.
