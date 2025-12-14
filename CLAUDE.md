# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Tourism Information Management System - A full-stack web application for managing tourism products, user accounts, and orders. The system supports three user roles: Administrators, Travel Advisors, and Customers.

**Tech Stack:**
- Backend: Spring Boot 3.3.4 + MyBatis + MySQL
- Frontend: Vue 2.6 + Element UI + Vuex
- Authentication: JWT tokens

## Build and Run Commands

### Backend (Spring Boot)

```bash
# Navigate to backend directory
cd tourism_back_end

# Run application (development)
mvn spring-boot:run

# Build JAR package
mvn clean package

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Skip tests during build
mvn clean package -DskipTests
```

Backend runs on **http://localhost:8080**

### Frontend (Vue.js)

```bash
# Navigate to frontend directory
cd tourism_front_end

# Install dependencies
npm install

# Run development server
npm run serve

# Build for production
npm run build

# Lint and fix files
npm run lint
```

Frontend runs on **http://localhost:7070**

### Database Setup

MySQL database required:
- Database name: `tourism`
- Default connection: `localhost:3306`
- Credentials: root/123456 (configured in [application.yml](tourism_back_end/src/main/resources/application.yml))

MyBatis mapper XML files are located in `tourism_back_end/src/main/resources/mapper/`

## Architecture

### Backend Architecture (Spring Boot + MyBatis)

**Package Structure:**
- `com.example.tourism_rear`
  - `controller/` - REST API endpoints (UserController, CulturalProductController, OrderController)
  - `service/` + `service/impl/` - Business logic layer
  - `mapper/` - MyBatis data access interfaces
  - `config/` - Spring configuration (WebConfig, CorsConfig)
  - `interceptor/` - JWT authentication interceptor (LoginCheckInterceptor)
  - `pack/utils/` - DTOs and utility classes (Jwt, Result, Login, UserInfo, etc.)
  - `pack/pagination/` - PageBean wrapper for paginated responses

**Key Patterns:**
1. **Three-Tier Layered Architecture**: Controller → Service → Mapper
2. **JWT Authentication**: All endpoints except `/login`, `/register`, `/indexView` require valid Bearer token
3. **Interceptor-Based Validation**: LoginCheckInterceptor validates JWT on each request
4. **MyBatis Hybrid Mapping**: XML files for complex queries + annotations (@Select, @Insert, @Update, @Delete) for simple operations
5. **Pagination**: PageHelper library with try-with-resources pattern
6. **Transaction Management**: @Transactional(rollbackFor = Exception.class) for write operations
7. **Standard Response Format**: Result object with code, message, data fields

**Authentication Flow:**
- Login returns JWT with claims: uid, username, authority (role)
- Token stored in sessionStorage on frontend
- Token sent in Authorization header as "Bearer {token}"
- Token expiration: 24 hours
- Signing key: "SunWukong" (in [Jwt.java](tourism_back_end/src/main/java/com/example/tourism_rear/pack/utils/Jwt.java))

**Role System:**
- 1 = Administrator (highest privilege)
- 2 = Travel Advisor
- 3 = Customer (lowest privilege)

### Frontend Architecture (Vue.js + Vuex)

**Directory Structure:**
- `src/components/` - Reusable Vue components
- `src/views/` - Page-level components
  - `views/UserPanel/` - Protected dashboard area with role-based access
- `src/router/index.js` - Route definitions with navigation guards
- `src/store/index.js` - Vuex store for authentication state
- `src/axios.js` - API client factory with retry logic

**Key Patterns:**
1. **Route Guards**: beforeEach hook validates JWT and checks user role against route meta.requiredRole
2. **Centralized API Client**: Two axios instances (apiRear for backend, apiFront for frontend) with automatic token injection
3. **Vuex State Management**: Stores decoded JWT token and user object
4. **Element UI Components**: el-form, el-table, el-dialog, el-card for consistent UI
5. **Nested Routing**: /userPanel parent route with child routes for different dashboard sections
6. **Role-Based Rendering**: Conditionally shows menu items and features based on user.authority

**API Integration:**
- Import via `import { apiRear } from "@/axios"`
- Request interceptor adds Authorization header automatically
- Retry mechanism: Up to 3 retries on connection errors
- Timeout: 10 seconds

**Protected Routes:**
All routes under `/userPanel` require authentication. Access determined by role comparison:
- `/userManagement` - Admin only (role == 1)
- `/culturalProducts/manage` - Advisors and above (role <= 2)
- `/home`, `/userInfo`, `/culturalProducts`, `/billPayment`, `/completeBill` - All authenticated users (role >= 3)

## Common Development Workflows

### Adding a New API Endpoint

1. Create DTO class in `pack/utils/` if needed
2. Add mapper interface method in `mapper/` package
3. Add XML mapping in `src/main/resources/mapper/*.xml` (for complex queries) or use annotations
4. Implement service interface in `service/` and implementation in `service/impl/`
5. Add controller endpoint in `controller/` package
6. Update LoginCheckInterceptor exclude patterns if endpoint should be public
7. Test via Postman or frontend integration

### Adding a New Frontend View

1. Create .vue file in `src/views/` or `src/views/UserPanel/`
2. Add route definition in `src/router/index.js` with appropriate meta (requiresAuth, requiredRole)
3. Add navigation menu item in `UserPanel/Main.vue` if applicable
4. Import API client: `import { apiRear } from "@/axios"`
5. Use Element UI components for consistency

### Modifying Database Schema

1. Update MySQL database schema
2. Update corresponding Mapper interface in `mapper/` package
3. Update XML mapper file in `src/main/resources/mapper/`
4. Update DTO classes in `pack/utils/`
5. Update service implementations if business logic changes

## Testing

### Backend Testing

Tests are located in `tourism_back_end/src/test/java/`

Run all tests:
```bash
cd tourism_back_end
mvn test
```

Run specific test:
```bash
mvn test -Dtest=ClassName#methodName
```

### Frontend Testing

Currently no automated tests configured. Manual testing via browser on http://localhost:7070

## Configuration Files

### Backend Configuration

**[application.yml](tourism_back_end/src/main/resources/application.yml)**
- Database connection settings
- Server port (8080)
- MyBatis logging configuration

**[pom.xml](tourism_back_end/pom.xml)**
- Dependencies: Spring Boot, MyBatis, PageHelper, JJWT, MySQL Connector, Lombok, FastJSON
- Java version: 17

### Frontend Configuration

**[package.json](tourism_front_end/package.json)**
- Scripts: serve, build, lint
- Dependencies: Vue 2.6, Element UI, axios, vue-router, vuex, v-charts, echarts

**[vue.config.js](tourism_front_end/vue.config.js)**
- Webpack dev server configuration
- Port 7070
- Path alias: `@` → `src/`

## Important Notes

### Security Considerations

- JWT signing key is hardcoded in Jwt.java (production should use environment variable)
- Database credentials in application.yml (use environment variables in production)
- Passwords stored as plain text in database (should be hashed with bcrypt)
- FastJSON 1.2.83 has known vulnerabilities (consider upgrading to 2.x or switching to Jackson)

### CORS Configuration

Backend allows requests from `http://localhost:7070` via CorsConfig.java and WebConfig.java. Update for production deployment.

### Database Connection

Default connection uses localhost. Update [application.yml](tourism_back_end/src/main/resources/application.yml) for different environments.

### Token Expiration

JWT tokens expire after 24 hours (86400 seconds). Users must re-authenticate after expiration.

## Project Naming Convention

The project was renamed from "medicine system" to "tourism system". Some references may still show "medicine" in:
- Frontend package.json (name: "medicine_system")
- Comments or legacy code

Backend artifact ID is `tourism_rear` (intentionally "rear" not "rear-end" or "backend").
