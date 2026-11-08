# Local Deployment and Reviewer Guide

## 1. Deployment Status

**Deployment Type:** Local execution only  
**Live Application URL:** Not available. The application has not been deployed to a cloud host.

**GitHub Repository:** https://github.com/Fauzdarnikhil/coaching-fee-portal

The reviewer should clone the repository and run the application locally. Keep the backend running while testing.

## 2. Prerequisites

- Java 21
- MySQL Server
- Git
- A web browser
- IntelliJ IDEA or VS Code (optional)

## 3. Get the Source Code

```bash
git clone https://github.com/Fauzdarnikhil/coaching-fee-portal.git
cd coaching-fee-portal
```

## 4. Configure the Database

Create the database in MySQL:

```sql
CREATE DATABASE coaching_fee_db;
```

Set these environment variables. Do not commit actual credentials or secret values.

| Variable | Purpose |
|---|---|
| `DB_URL` | JDBC connection URL for MySQL |
| `DB_USERNAME` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET` | Secret used to sign authentication tokens |

## 5. Build and Run

### Windows PowerShell

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/coaching_fee_db"
$env:DB_USERNAME = "<your-mysql-username>"
$env:DB_PASSWORD = "<your-mysql-password>"
$env:JWT_SECRET = "<a-local-secret-value>"

.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
export DB_URL="jdbc:mysql://localhost:3306/coaching_fee_db"
export DB_USERNAME="<your-mysql-username>"
export DB_PASSWORD="<your-mysql-password>"
export JWT_SECRET="<a-local-secret-value>"

./mvnw clean package
./mvnw spring-boot:run
```

Alternatively, import the project as a Maven project in an IDE, configure the same environment variables in its run configuration, and run the Spring Boot application.

Keep the backend running during the review.

## 6. Open the Application

Open `http://localhost:8080` in a browser. If a different port is configured, use that port.

## 7. Demo Accounts

Use the seeded/demo accounts in the project. Replace these placeholders with the exact credentials present in the submitted project.

### Accounts Executive(`http://localhost:8080/accounts-login.html`)

- Work Email: `accounts@isocode.com`
- Password: `Accounts@123`

### Parent Portal (`http://localhost:8080/parent-login.html`)

I have Seeded 4 Students, so I am providing two registered mobile number to test. For these numbers the OTP will be generated at Console for testing Purpose.

- Registered mobile number 1: `9000000002`
- Registered mobile number 2: `9000000003`
- OTP: Generated at login and printed in the Spring Boot application console.

### Parent Login Procedure

1. Open the Parent Login page.
2. Enter the registered demo parent's mobile number.
3. Select **Send OTP**.
4. Open the terminal or IDE console where Spring Boot is running.
5. Find the OTP generated for that login request.
6. Enter the OTP in the login page and select **Verify OTP**.
7. After successful verification, the parent is redirected to the Parent Portal.

SMS delivery is not configured. For this local demonstration, the OTP is printed in the backend console. The reviewer must have access to that console and keep the backend running while signing in.

## 8. Suggested Reviewer Walkthrough

1. Sign in as the Accounts Executive and inspect the accounts dashboard.
2. Review student records, fee plans, instalments, payment records and the fee ledger.
3. Inspect unmatched payments. The application prevents allocation when no eligible outstanding instalment exists.
4. Open the refund calculator and review the preview. It does not execute or approve a refund.
5. Generate and inspect fee reminders. Reminder records are simulated; no SMS or WhatsApp message is sent.
6. Open Student Risk and inspect the rule-based score, contributing factors and counsellor tasks.
7. Sign out and sign in through the Parent Portal using the registered mobile number and OTP printed in the backend console.
8. Review the linked student's fee summary, instalments, payment history and academic progress.

## 9. Implementation Notes and Limitations

- **Payments:** The payment workflow is a mock/demo flow. It does not connect to a real payment gateway or transfer money.
- **OTP:** OTP is displayed in the backend console; SMS delivery is not integrated.
- **Reminders:** Reminder generation and records are implemented, but external SMS/WhatsApp delivery is not integrated.
- **Refunds:** The refund calculator provides a preview only. Actual refund approval and payment processing are not implemented.
- **Student Risk:** Risk assessment is a deterministic, explainable rule-based score, not a trained machine-learning model or statistically calibrated dropout probability.
- **Deployment:** The project is provided for local execution. No hosted/live URL is supplied.

## 10. Verification Notes

The application build, core accounts dashboard and fee workflows, mock payment flow, fee ledger, reminder generation, risk assessment and counsellor-task workflow, parent authentication, and parent-specific dashboard display were exercised during development.

The parent portal was tested with two parent accounts to confirm that each displayed its own linked student's information in normal use. A separate manual authorization test involving deliberate identifier tampering was not performed.

The unmatched-payment list and prevention of allocation when no outstanding instalment is available were checked. A successful unmatched-payment allocation was not verified.

The refund calculator preview was checked; actual refund processing was not tested because the feature is preview-only.

## 11. Troubleshooting

If the application does not start, verify:

- MySQL is running and `coaching_fee_db` exists.
- All four environment variables are configured in the same terminal or IDE run configuration used to start Spring Boot.
- Java 21 is selected.
- The backend has completed startup before opening the browser.

For parent OTP login, check the Spring Boot console immediately after selecting **Send OTP**.