# SE2030--Web-based-Blood-Donation-System

# BloodConnect

BloodConnect is a Spring Boot web application for managing blood donors, blood requests, screened blood inventory, hospital requisitions, deliveries, and staff access. It connects public users and operational teams through one SQL Server-backed system.

The project is designed around the complete blood-service workflow:

```text
Donor registration
      -> Blood collection and inventory
      -> QA screening
      -> Approved stock
      -> Recipient or hospital request
      -> Manager approval
      -> Delivery scheduling
      -> Dispatch and delivery tracking
```

## What This Project Provides

### Public features

- Landing page with donor, request, and fulfilled-request statistics
- Donor registration with availability time slots
- Donor search by name, blood group, and city
- Donor profile viewing and updating
- Donation history recording
- Blood availability search for approved units
- Public blood-request board with filtering and status updates
- Contact form for public inquiries

### Staff portals

The staff login redirects each member to the portal available for their role. Access is controlled through role permissions and individual member permissions.

| Role | Main responsibility | Portal |
| --- | --- | --- |
| Recipient / Patient | View availability, submit and track personal blood requests | `/portal/recipient` |
| Hospital Coordinator | Submit hospital requisitions and track dispatches | `/portal/hospital` |
| Quality Assurance Officer | Review inventory and record infectious-marker screening | `/portal/qa` |
| Delivery Coordination Officer | Manage vehicles, couriers, schedules, and delivery status | `/portal/delivery` |
| Blood Bank Manager | Monitor stock, approve requests, and review reports | `/portal/manager` |
| IT Operations Supervisor | Manage users, permissions, and audit activity | `/portal/it` |

An IT Administrator can access all portals. The application also supports changing permissions for a whole role or for an individual member.

## Main Workflow

1. A donor registers through `/register` and adds their blood group, contact details, city, availability, and optional donation history.
2. Blood units are recorded in inventory with their group, component, branch, expiry date, and screening status.
3. QA staff record HIV, Hepatitis B, Hepatitis C, Syphilis, and Malaria results.
4. Units that pass screening become `Approved - Available` and appear in availability searches.
5. A recipient submits a patient request, or a hospital coordinator submits a hospital requisition.
6. The Blood Bank Manager reviews and approves eligible requests.
7. Delivery staff assign a courier, vehicle, schedule, and delivery status.
8. Actions are recorded in the audit log for operational traceability.

## Technology Stack

- Java 17
- Spring Boot 3.2.5
- Spring MVC
- Thymeleaf server-side templates
- Spring Data JPA / Hibernate
- Microsoft SQL Server
- Microsoft SQL Server JDBC Driver
- BCrypt password hashing
- HTML, CSS, and JavaScript for the browser UI
- Maven
- JUnit 5 and Spring Boot Test

## Project Structure

```text
blood-donation-spring/
├── pom.xml                              Maven dependencies and build configuration
├── run.bat                              Windows startup helper
├── database/
│   └── qa-blood-units.sql               SQL Server database/table preparation script
├── data/                                Supporting project data
├── src/
│   ├── main/
│   │   ├── java/com/blooddonation/spring/
│   │   │   ├── config/                  Interceptors and exception handling
│   │   │   ├── controller/              Public, authentication, admin, and portal routes
│   │   │   ├── model/                   JPA entities, roles, and permission definitions
│   │   │   ├── repository/               Spring Data repository interfaces
│   │   │   └── service/                  Business logic, authentication, and seeding
│   │   │   └── BloodDonationApplication.java
│   │   └── resources/
│   │       ├── application.properties   Port, SQL Server, JPA, and application settings
│   │       ├── templates/                Thymeleaf pages and portal views
│   │       └── static/                   CSS and JavaScript assets
│   └── test/                             Unit tests for models and services
└── target/                               Maven build output (generated)
```

## Important Routes

### Public routes

| URL | Purpose |
| --- | --- |
| `/` | Home dashboard |
| `/availability` | View approved blood units |
| `/search` | Search registered donors |
| `/register` | Register a donor |
| `/donor/{id}` | View a donor profile |
| `/requests` | View and submit public requests |
| `/contact` | Send a contact message |
| `/login` | Staff login |
| `/logout` | End the current session |

### Admin routes

The `/admin/**` area provides operational administration, including the dashboard, donor management, inventory, QA screening, requests, deliveries, and contact messages. It is protected by the admin interceptor and the current authenticated session.

## Database and Initial Data

The default configuration connects to a local SQL Server instance using Windows integrated authentication:

```properties
Database: Blood_Connect
Host: localhost
Port: 1433
Authentication: Windows integrated security
```

The database script at `database/qa-blood-units.sql` creates the `Blood_Connect` database and the `blood_units` table with useful indexes. Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`, so the remaining entity tables are created or updated when the application starts.

When the database is empty, the application seeds demonstration data automatically:

- Sample donors and donation history
- Sample pending and delivered requests
- Sample contact message
- Screened and pending blood units
- Delivery vehicles and couriers
- Permission configurations
- One demo staff user for each role

### Demo staff accounts

All seeded staff accounts use the initial password `pass123`:

| Username | Role |
| --- | --- |
| `recipient` | Recipient / Patient |
| `hospital` | Hospital Coordinator |
| `qa` | Quality Assurance Officer |
| `delivery` | Delivery Coordination Officer |
| `manager` | Blood Bank Manager |
| `itadmin` | IT Operations Supervisor |

These credentials are for local development only. Change or remove them before using the system in a real environment.

## Prerequisites

For the default setup, install or configure:

1. JDK 17 or a compatible newer JDK
2. Maven 3.9+ or the Maven installation bundled with IntelliJ IDEA
3. Microsoft SQL Server running locally on TCP port `1433`
4. A SQL Server database login or Windows account with permission to create and update `Blood_Connect`
5. Windows, because the default SQL Server authentication setup uses the Microsoft native authentication library

The Maven build copies the SQL Server authentication DLL into `target/native`. The application loads that library automatically on Windows before Spring Boot starts.

## Running the Application

### Option 1: Windows helper script

From the project root, double-click `run.bat`, or run:

```bat
run.bat
```

The script looks for a usable JDK and Maven installation, then starts the application on:

```text
http://localhost:8089
```

### Option 2: Maven commands

```bat
mvn clean package
mvn spring-boot:run
```

If the native SQL Server authentication library has not been prepared yet, run:

```bat
mvn process-resources
mvn spring-boot:run
```

Then open `http://localhost:8089` in a browser.

## Configuration

The main settings are in `src/main/resources/application.properties`:

- `server.port=8089` controls the HTTP port.
- `spring.datasource.url` controls the SQL Server connection.
- `spring.jpa.hibernate.ddl-auto=update` keeps entity tables aligned during development.
- `spring.thymeleaf.cache=false` enables template changes without restarting during development.
- `spring.jpa.show-sql=true` prints SQL statements to the console.

For another environment, update the datasource configuration and move credentials/secrets to environment variables or an external configuration source. Do not commit production passwords, connection strings, or demo credentials.

## Testing

Run the test suite with:

```bat
mvn test
```

Current tests cover model behavior and authentication/permission behavior, including:

- Stored role permissions
- Inactive-user access restrictions
- Portal access based on assigned permissions
- IT Administrator access
- User deletion behavior
- Authentication rejection for disabled accounts

The application also has generated Maven reports under `target/surefire-reports` after a test run.

## Security and Access Design

- Passwords are stored as BCrypt hashes.
- Login state is stored in the HTTP session.
- `/portal/**` is protected by `PortalAuthInterceptor`.
- `/admin/**` is protected by `AdminInterceptor`.
- Portal actions perform role and permission checks in `PortalController`.
- IT administrators can activate/deactivate users and customize permissions.
- Important actions are written to audit logs by `AuthService`.

This is a development-oriented application. Before production deployment, add environment-based secrets, CSRF protection, stronger validation, production session settings, HTTPS, database least-privilege access, and a complete integration/security test suite.

## Development Notes

- Business rules mainly live in `BloodDonationService` and `AuthService`.
- Public page navigation is implemented in `WebController`.
- Authentication is implemented in `AuthController`.
- Staff operations are split between `AdminController` and `PortalController`.
- Thymeleaf fragments and shared navigation are in `src/main/resources/templates/layout.html`.
- Static frontend assets are in `src/main/resources/static/css` and `src/main/resources/static/js`.
- `target/` is generated output and should not be edited manually.

## Project Summary

BloodConnect is a role-based blood donation and distribution platform. It gives donors and the public a simple way to register, search, and request blood while giving hospitals, QA teams, managers, delivery staff, and IT administrators the operational tools needed to move a blood unit from collection through screening and delivery.
