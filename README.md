# Expense Splitter (Backend)

A group expense-sharing REST API, like Splitwise. Friends create groups, add shared expenses, see who owes whom, and settle up. An AI feature turns a sentence such as *"Dinner 1200 with Rahul"* into a draft expense.

**Live app:** https://expense-splitter-ui-three.vercel.app
**Frontend repo:** https://github.com/AkshayJ-2028/expense-splitter-ui

> The API runs on a free Render plan and sleeps when idle, so the first request can take up to a minute.

## Features

- **Authentication:** register and log in with JWT, passwords hashed with BCrypt
- **Groups:** create groups and add members by email. Only the group creator can add or remove members, or delete the group.
- **Delete a group:** removes the group together with its expenses, splits, payments and members in a single transaction, so no orphaned data is left behind
- **Three split types:** equal, percent and exact amount
- **Balances and settle-up plan:** a balance per member, plus a plan that clears every debt with the **fewest payments** (greedy algorithm)
- **Payments:** record a payment and the balances update automatically
- **AI expense entry (Gemini API):** type a sentence, get a draft expense
  - The AI never saves anything. The user reviews the draft first.
  - The AI output is validated on the server before it is returned
  - Requests are rate limited, and a backup model is used if the first one is busy
- **Accurate money handling:** amounts use `BigDecimal`, never floating point

## Tech stack

Java 25, Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL (Neon), H2 (local), Maven, JUnit, Docker, Render

## API overview

All endpoints except `/api/auth/*` need the header `Authorization: Bearer <token>`.

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Groups | `POST /api/groups`, `GET /api/groups`, `GET /api/groups/{id}`, `DELETE /api/groups/{id}` |
| Members | `POST /api/groups/{id}/members`, `DELETE /api/groups/{id}/members/{userId}` |
| Expenses | `POST /api/groups/{id}/expenses`, `GET /api/groups/{id}/expenses`, `DELETE /api/groups/{id}/expenses/{expenseId}` |
| Balances | `GET /api/groups/{id}/balances` (member balances and the settle-up plan) |
| Payments | `POST /api/groups/{id}/payments` |
| AI | `POST /api/groups/{id}/ai/parse-expense` |

## How the AI feature works

1. The frontend sends the sentence the user typed, for example *"Dinner 1200, I paid, split with Rahul"*.
2. The backend sends it to the Gemini API together with the group's member names.
3. The backend checks the structured answer: amount, who paid, split type, and that every name is a real group member.
4. The validated draft goes back to the frontend, which fills the expense form. Nothing is saved until the user confirms.

## Run locally

You need Java 25. The app uses a local H2 database file by default, so no database setup is needed.

```
git clone https://github.com/AkshayJ-2028/Expense-Splitter.git
cd Expense-Splitter
.\mvnw spring-boot:run
```

The API starts on http://localhost:8080. On macOS or Linux use `./mvnw` instead of `.\mvnw`.

**Optional, for the AI feature:** set `GEMINI_API_KEY` before starting.

```
:: Command Prompt
set GEMINI_API_KEY=your-key
```

```
# PowerShell
$env:GEMINI_API_KEY = "your-key"
```

## Run the tests

```
.\mvnw test
```

## Environment variables (production)

| Name | Purpose |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | PostgreSQL connection (Neon) |
| `JWT_SECRET` | Secret used to sign login tokens |
| `GEMINI_API_KEY` | Key for the AI feature |

Secrets live in environment variables and are never committed to the repo.

## Deployment

The app is built into a Docker image from the `Dockerfile` and deployed on Render, with the database on Neon (PostgreSQL).

To try the image locally:

```
docker build -t expense-splitter .
docker run -p 8080:8080 -e JWT_SECRET=change-me expense-splitter
```

## Author

Akshay Kumar Jha · https://github.com/AkshayJ-2028
