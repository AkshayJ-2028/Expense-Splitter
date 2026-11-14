# Expense Splitter (Backend)

A group expense-sharing API, like Splitwise. Friends create groups, add shared expenses, see who owes whom, and settle up. An AI feature turns a sentence such as "Dinner 1200 with Rahul" into a draft expense.

**Live app:** https://expense-splitter-ui-three.vercel.app
**Frontend repo:** https://github.com/AkshayJ-2028/expense-splitter-ui

> The backend runs on a free Render plan. It sleeps when idle, so the first request can take up to a minute.

## Features
- Register and log in with JWT authentication (BCrypt password hashing)
- Create groups and add members by email (only the group creator can add or remove members)
- Add expenses with three split types: **equal**, **percent** and **exact amount**
- Balances per member and a **settle-up plan with the fewest payments** (greedy algorithm)
- Record payments, and balances update automatically
- **AI expense entry:** type a sentence, get a draft expense (Gemini API)
  - The AI never saves anything: the user reviews the draft first
  - AI output is validated on the server, requests are rate limited, and a backup model is used if the first one is busy
- Money is handled with `BigDecimal` (never floating point)

## Tech stack
Java 25, Spring Boot 4, Spring Security (JWT), Spring Data JPA, PostgreSQL (Neon), H2 (local), Maven, Docker, Render

## API overview
| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Groups | `POST /api/groups`, `GET /api/groups`, `GET /api/groups/{id}` |
| Members | `POST /api/groups/{id}/members`, `DELETE /api/groups/{id}/members/{userId}` |
| Expenses | `POST /api/groups/{id}/expenses` |
| Balances | `GET /api/groups/{id}/balances` |
| Settle up | `POST /api/groups/{id}/settlements`, `GET /api/groups/{id}/settlements` |
| AI | `POST /api/groups/{id}/ai/parse-expense` |

## Run locally
1. Install Java 25.
2. Clone the repo and run:
