# Coaching Institute Fee Tracking System

A web-based fee management and parent portal application for a coaching
institute. The system helps institute staff manage students, fee plans,
instalments, payments, fee records, reminders, refunds, and student risk
indicators. Parents can securely sign in to view their child's fee and
academic information.

## Features

### Accounts Executive

-   **Dashboard:** Central place to access the accounts workflows.
-   **Student management:** View and manage student records.
-   **Fee plans and instalments:** Track student fee plans and scheduled
    instalments.
-   **Mock payment workflow:** Demonstrates payment initiation and
    successful mock payment handling.
-   **Payment allocation:** Allocate received payments against eligible
    fee instalments.
-   **Fee ledger:** View fee and payment allocation information.
-   **Unmatched payments:** Identify successful payments that still have
    an unallocated balance. Allocation validation prevents invalid
    allocations.
-   **Reminders:** Generate and view fee reminders for upcoming,
    due-today, and overdue instalments.
-   **Refund calculator:** Preview an estimated refund using the
    configured demonstration policy.
-   **Student risk assessment:** View explainable, rule-based risk
    scores and contributing factors.
-   **Counsellor tasks:** Track follow-up tasks associated with medium-
    and high-risk students.
-   **Responsive navigation:** Sidebar navigation adapts to desktop and
    mobile screen sizes.

### Parent Portal

-   Parent authentication.
-   Parent dashboard showing linked student information.
-   Fee plan, instalment, paid amount, and outstanding balance details.
-   Payment history, including allocated and unallocated amounts.
-   Academic progress information, including attendance and test
    results.
-   Parent views are scoped to the students linked to the authenticated
    parent account.

## Technology Stack

-   **Backend:** Java 21, Spring Boot
-   **Security:** Spring Security and JWT-based authentication
-   **Database:** MySQL
-   **Persistence:** Spring Data JPA / Hibernate
-   **Frontend:** HTML, CSS, JavaScript
-   **Build tool:** Maven (Maven Wrapper included)

> Check `pom.xml` for the exact Spring Boot and dependency versions used
> by this project.

## Prerequisites

Install the following before running the application:

-   Java Development Kit (JDK) 21
-   MySQL Server
-   Git
-   A modern web browser

You can use the included Maven Wrapper, so a separate Maven installation
is not required.

## Getting Started

### 1. Clone the repository

``` bash
git clone https://github.com/Fauzdarnikhil/coaching-fee-portal.git
cd coaching-fee-portal
```

### 2. Create the database

Create a MySQL database:

``` sql
CREATE DATABASE coaching_fee_db;
```

The application uses the database URL below by default:

``` text
jdbc:mysql://localhost:3306/coaching_fee_db
```

If your MySQL database uses a different host, port, or database name,
set `DB_URL` accordingly.

### 3. Configure environment variables

The application reads its database credentials and JWT signing secret
from environment variables.

**PowerShell (Windows):**

``` powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/coaching_fee_db"
$env:DB_USERNAME = "your_mysql_username"
$env:DB_PASSWORD = "your_mysql_password"
$env:JWT_SECRET = "replace_with_a_long_random_secret"
```

**Command Prompt (Windows):**

``` cmd
set DB_URL=jdbc:mysql://localhost:3306/coaching_fee_db
set DB_USERNAME=your_mysql_username
set DB_PASSWORD=your_mysql_password
set JWT_SECRET=replace_with_a_long_random_secret
```

**macOS / Linux:**

``` bash
export DB_URL="jdbc:mysql://localhost:3306/coaching_fee_db"
export DB_USERNAME="your_mysql_username"
export DB_PASSWORD="your_mysql_password"
export JWT_SECRET="replace_with_a_long_random_secret"
```

Use a strong, private JWT secret. These examples are placeholders. Do
not commit real passwords or secrets to Git.

The variables above must be set in the same terminal session used to run
the application. If you open a new terminal, set them again.

### 4. Build the application

**Windows PowerShell:**

``` powershell
.\mvnw.cmd clean package
```

**macOS / Linux:**

``` bash
./mvnw clean package
```

### 5. Run the application

**Windows PowerShell:**

``` powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**

``` bash
./mvnw spring-boot:run
```

Once the application starts, open:

``` text
http://localhost:8080
```

Use the login pages and accounts configured in your local/demo database.
Demo usernames and passwords are intentionally not listed here; add only
credentials that are safe to share with evaluators.

## Configuration

The main configuration file is:

``` text
src/main/resources/application.properties
```

The application uses these environment-backed settings:

  Variable        Purpose
  --------------- ---------------------------------
  `DB_URL`        JDBC URL for the MySQL database
  `DB_USERNAME`   MySQL username
  `DB_PASSWORD`   MySQL password
  `JWT_SECRET`    Secret used to sign JWT tokens

The project currently uses the following JPA schema setting in its
application configuration:

``` properties
spring.jpa.hibernate.ddl-auto=update
```

For a production deployment, use a deliberate database migration
strategy and review the schema-management configuration before
deployment.

## Project Structure

The project follows a typical Spring Boot layout:

``` text
coaching-fee-portal/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/iscode/coaching_fee_portal/
│       │       ├── controller/
│       │       ├── service/
│       │       ├── repository/
│       │       ├── entity/
│       │       ├── dto/
│       │       └── config/
│       └── resources/
│           ├── application.properties
│           └── static/
│               ├── index.html
│               ├── css/
│               └── js/
├── pom.xml
└── mvnw / mvnw.cmd
```

The package and subdirectory names above describe the main architectural
areas; consult the repository for the complete current file tree.

## Important Implementation Notes

-   **Payments:** The payment journey is a mock/demo workflow. It does
    not integrate with a live payment gateway.
-   **Reminders:** The application generates and stores reminder
    records. Actual SMS or WhatsApp delivery is not integrated.
-   **Refunds:** The refund calculator provides an estimate based on a
    demonstration policy. It does not approve or execute a refund.
-   **Student risk:** Risk scoring is rule-based and explainable. It is
    not a trained machine-learning model or a guaranteed prediction of
    student dropout.
-   **Academic data:** Attendance and test information shown in the
    parent portal depends on the records available in the database.

## Verification

The project has previously been built successfully using:

``` powershell
.\mvnw.cmd clean package
```

The main workflows have been exercised during development, including
student and fee management, the mock payment flow, payment allocation,
parent authentication, parent fee views, payment history, academic
progress, reminder generation, refund estimation, and student
risk/counsellor-task views.

Some scenarios remain unverified, including a successful allocation of a
genuinely unmatched payment. Negative authorization testing involving
manual ID tampering was not completed. Do not treat these scenarios as
verified.

## Security and Deployment Checklist

Before deploying beyond a local/demo environment:

-   Use strong, unique database credentials and JWT secrets.
-   Keep secrets in environment variables or a secrets manager; never
    commit them.
-   Use HTTPS.
-   Review Spring Security rules and authorization for every role and
    API.
-   Test that parents can access only their linked students' records.
-   Review CORS, session/token handling, error responses, and logging.
-   Replace mock payment handling with a verified payment-provider
    integration if real payments are required.
-   Integrate and test a real notification provider if SMS/WhatsApp
    delivery is required.
-   Use database migrations and production backups.
-   Disable verbose SQL and security trace logging in production.

## Repository

GitHub: <https://github.com/Fauzdarnikhil/coaching-fee-portal>

## License

No license is specified in this README. Add a license file if you intend
to publish or distribute the project under specific terms.
