# NexusPortal - Enterprise User Governance & Access System

A modern, production-grade Java Web Application featuring a **Dark Aurora & Glassmorphic UI**, **Session-Based Security**, **JSP**, **jQuery**, **Bootstrap 5**, **AJAX**, **Jersey JAX-RS Web Services**, and **MySQL Database**.

Designed specifically for technical evaluations and high-stakes interviews, showcasing enterprise architecture, prepared statements for SQL injection immunity, session fixation defense, and a responsive SaaS sidebar dashboard.

---

## 🚀 Key Features

- **🔐 Session-Based Security**: Complete authentication lifecycle with `HttpSession`, session fixation defense, inactivity countdown timer, and automatic invalidation on logout.
- **🛡️ Security Filters**: 
  - `AuthenticationFilter`: Intercepts protected JSP pages and REST endpoints, redirecting browsers to `/login.jsp` and returning `401 Unauthorized` for AJAX requests.
  - `NoCacheFilter`: Prevents browser history caching (`no-store, no-cache, must-revalidate`), stopping unauthorized back-button access after sign out.
- **⚡ Asynchronous CRUD with AJAX**: Add, Update, Toggle Status, and Delete users without full page reloads.
- **📊 Interactive Data Grid**:
  - Instant live search with debounced input.
  - Multi-criteria filtering by **Role** (`ADMIN`, `MANAGER`, `USER`) and **Status** (`ACTIVE`, `INACTIVE`).
  - Dynamic column sorting (`ID`, `Name`, `Email`, `Role`, `Status`, `Date`).
  - Client & Server-side pagination with configurable page sizes.
- **📈 Dashboard KPI Metrics**: Live count cards displaying Total Users, Active Accounts, Inactive Accounts, and Administrator totals.
- **🔒 Password Security**: Industry-standard **BCrypt** salted hashing (work factor 10). Plaintext passwords are never stored in the database or serialized into JSON responses.
- **📋 Audit Activity Logs**: Tracks authentication and CRUD events (`LOGIN`, `LOGOUT`, `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, `STATUS_CHANGED`) with timestamps and client IP addresses.
- **🔄 Smart Database Resilience**: Seamlessly connects to your primary MySQL database; includes an auto-initializing embedded MySQL-compatible engine for zero-configuration testing.

---

## 🛠️ Technology Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| **View / Presentation** | JSP (JavaServer Pages) | 2.3 | Server-rendered modular view templates |
| **Frontend Styling** | Bootstrap CSS | 5.3.3 | Responsive modern design, modals, badges, cards |
| **Icons** | Bootstrap Icons | 1.11.3 | High-DPI interface icons |
| **Client Scripting** | jQuery & AJAX | 3.7.1 | DOM manipulation, asynchronous REST calls, event handling |
| **REST Web Service** | Jersey (JAX-RS) | 2.39.1 | RESTful web services (`/api/users`, `/api/auth`) |
| **JSON Serialization** | Jackson Databind | 2.15.2 | POJO to JSON marshaling and unmarshaling |
| **Connection Pool** | HikariCP | 5.1.0 | High-performance JDBC connection pooling |
| **Database** | MySQL | 8.x / 5.7+ | Primary relational database persistence |
| **Security** | jBCrypt | 0.4 | Adaptive salted password hashing |
| **Server Runtime** | Embedded Apache Tomcat | 9.0.89 | 1-Click standalone runner (`mvn exec:java`) |
| **Build Tool** | Apache Maven | 3.8+ | Dependency management & WAR packaging |

---

## 🔑 Demo Credentials

For quick evaluation during interviews or presentations, use the pre-seeded accounts:

| Role | Username | Password | Permissions |
|---|---|---|---|
| **Administrator** | `admin` | `Admin@123` | Full control (Add, Edit, Toggle, Delete users, view all metrics) |
| **Manager** | `john_doe` | `Manager@123` | Add, Edit, Toggle user status |
| **Standard User** | `jane_smith` | `User@123` | View user directory and profile |

> **Tip**: The login page includes convenient **one-click demo pill buttons** to automatically fill in credentials.

---

## ⚡ Quick Start Guide

### Option 1: One-Click Runner (Windows)
Double-click [`run.bat`](file:///c:/Dhanush/run.bat) in the project root. It will compile the project and start the embedded server automatically on port `8080`.

### Option 2: Maven Command Line
```bash
# 1. Compile and download dependencies
mvn clean compile

# 2. Start the embedded web server
mvn exec:java
```

Once started, open your web browser at:
- **Application URL**: `http://localhost:8080/`
- **Login Page**: `http://localhost:8080/login.jsp`
- **Home Dashboard**: `http://localhost:8080/home.jsp`
- **REST API Base**: `http://localhost:8080/api/users`

---

## 🗄️ Database Setup (MySQL)

By default, the application is pre-configured to look for MySQL running locally on port `3306`:
- **Database Name**: `usermanagement_db`
- **User**: `root`
- **Password**: `root` (Configurable in `src/main/resources/db.properties`)

### To initialize MySQL manually:
1. Open MySQL Workbench or your MySQL CLI:
   ```bash
   mysql -u root -p < init-db.sql
   ```
2. Check `src/main/resources/db.properties` and verify your username/password.
3. If your MySQL server is currently offline or unreachable, the application **automatically falls back** to an embedded MySQL-compatible engine so the application will run and remain fully functional without crashing.

---

## 📦 Deploying as WAR to Standalone Tomcat

To build a standalone `.war` file for deployment to an external Apache Tomcat server:

```bash
mvn clean package -DskipTests
```
The resulting artifact `target/user-management.war` can be copied directly to your Tomcat `webapps/` directory:
```bash
cp target/user-management.war /path/to/tomcat/webapps/ROOT.war
```

---

## 📂 Project Directory Structure

```text
c:\Dhanush\
├── pom.xml                               # Maven project descriptor & dependencies
├── run.bat                               # 1-Click execution script
├── init-db.sql                           # Standalone MySQL schema and seed data
├── README.md                             # Quickstart and overview guide
├── IMPLEMENTATION.md                     # Deep technical architecture & interview guide
├── src\
│   └── main\
│       ├── java\com\usermanagement\
│       │   ├── Main.java                 # Standalone embedded Tomcat runner
│       │   ├── config\
│       │   │   └── JerseyApplication.java # JAX-RS ResourceConfig registration
│       │   ├── model\
│       │   │   ├── User.java             # User entity (POJO with JSON annotations)
│       │   │   ├── ActivityLog.java      # Audit logging entity
│       │   │   └── ApiResponse.java      # Standard JSON response wrapper
│       │   ├── dao\
│       │   │   ├── UserDAO.java          # User data access interface
│       │   │   ├── UserDAOImpl.java      # JDBC PreparedStatement implementation
│       │   │   └── ActivityLogDAO.java   # Audit logging persistence
│       │   ├── service\
│       │   │   ├── UserService.java      # Input validation & business logic
│       │   │   └── AuthService.java      # Session authentication & fixation defense
│       │   ├── filter\
│       │   │   ├── AuthenticationFilter.java # Protected route & API security filter
│       │   │   └── NoCacheFilter.java    # HTTP Cache-Control header enforcer
│       │   ├── util\
│       │   │   ├── DBConnectionManager.java # HikariCP pool manager with fallback
│       │   │   ├── DBInitializer.java    # Automatic schema script runner
│       │   │   └── PasswordUtil.java     # BCrypt password hashing & salt generation
│       │   └── web\
│       │       ├── AuthResource.java     # REST endpoints for login/logout/me
│       │       └── UserResource.java     # REST endpoints for User CRUD & metrics
│       ├── resources\
│       │   ├── db.properties             # Database connection pool configuration
│       │   └── schema.sql                # SQL tables, indices, and sample accounts
│       └── webapp\
│           ├── WEB-INF\
│           │   └── web.xml               # Servlet, Filter, and Session deployment descriptor
│           ├── css\
│           │   └── style.css             # Modern custom UI styling
│           ├── js\
│           │   ├── app.js                # Session countdown, toasts, Ajax interceptor
│           │   └── users.js              # jQuery & Ajax User Grid CRUD controller
│           ├── includes\
│           │   ├── header.jsp            # Common HTML head and Bootstrap CSS
│           │   ├── navbar.jsp            # Top navigation bar with user profile dropdown
│           │   └── footer.jsp            # Common scripts and toast containers
│           ├── login.jsp                 # Modern session-secured login page
│           └── home.jsp                  # Main user management dashboard & modals
```

---

## 📑 In-Depth Implementation & Interview Documentation

For detailed explanation of:
- Database queries & PreparedStatement internals
- Session management, fixation defense, and filter flowcharts
- REST API contracts with JSON request/response formats
- Common interview questions and answers

👉 Read [`IMPLEMENTATION.md`](file:///c:/Dhanush/IMPLEMENTATION.md).
