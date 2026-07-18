<h1 align="center">🍕 Cafe Management System</h1>
<p align="center">
  A full-stack, production-grade restaurant / cafe ordering &amp; management platform —<br/>
  <b>Spring Boot 3 (Java 21) + PostgreSQL</b> backend and a <b>React 18 + TypeScript + MUI</b> frontend,<br/>
  with a premium Swiggy/Zomato/Uber-Eats style customer experience and a full back-office admin suite.
</p>

---

## Table of Contents

1. [About the Project](#about-the-project)
2. [Tech Stack](#tech-stack)
3. [Architecture &amp; Project Structure](#architecture--project-structure)
4. [Features](#features)
   - [Customer-Facing Features](#customer-facing-features)
   - [Admin / Back-Office Features](#admin--back-office-features)
   - [Kitchen &amp; Delivery Features](#kitchen--delivery-features)
   - [Platform-Wide Features](#platform-wide-features)
5. [User Roles](#user-roles)
6. [Prerequisites](#prerequisites)
7. [Setup &amp; Installation](#setup--installation)
   - [1. Clone the repository](#1-clone-the-repository)
   - [2. Set up PostgreSQL](#2-set-up-postgresql)
   - [3. Configure environment variables](#3-configure-environment-variables)
   - [4. Run the backend](#4-run-the-backend)
   - [5. Run the frontend](#5-run-the-frontend)
8. [Default Login Credentials](#default-login-credentials)
9. [API Documentation (Swagger)](#api-documentation-swagger)
10. [Running Tests](#running-tests)
11. [Browser End-to-End Tests](#browser-end-to-end-tests)
12. [Payment Gateway (Razorpay) Setup](#payment-gateway-razorpay-setup)
13. [Building for Production](#building-for-production)
14. [Troubleshooting](#troubleshooting)
15. [Project History / Legacy Notes](#project-history--legacy-notes)

---

## About the Project

**Cafe Management System** is an end-to-end food ordering and cafe/restaurant management application. It started life as a simple admin billing tool and has since been rebuilt into a modern, feature-rich platform comparable to real-world food delivery products (Domino's, Swiggy, Zomato, Uber Eats).

It supports the **entire lifecycle of a food order**:

`Browse menu → Add to cart → Apply coupon → Checkout → Pay (COD or online via Razorpay) → Kitchen prepares → Delivery partner delivers → Customer tracks & rates`

...as well as the **entire back-office lifecycle** an admin needs to run a cafe:

`Manage categories/products/stores → Manage coupons & loyalty → Track orders in a live Kitchen Display → Assign & monitor delivery partners → View sales analytics → Manage users`

The system is built with a clean layered architecture (Controller → Service → DAO → Entity) on the backend and a component-driven, hook-based architecture on the frontend, with JWT-based stateless authentication and role-based access control throughout.

---

## Tech Stack

### Backend
| Layer | Technology |
|---|---|
| Language / Runtime | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security 6 + JWT (`jjwt` 0.11.5) |
| Persistence | Spring Data JPA / Hibernate 6 |
| Database | PostgreSQL |
| Validation | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| API Docs | springdoc-openapi (Swagger UI) |
| PDF Generation | iText 5 / Apache PDFBox (bill/invoice downloads) |
| Payments | Razorpay Java SDK (`razorpay-java` 1.4.9) — real-time UPI / Cards / Net Banking / Wallets |
| Email | Spring Mail (SMTP) for notifications, password reset |
| Build Tool | Maven |
| Testing | JUnit 5 + Mockito |

### Frontend
| Layer | Technology |
|---|---|
| Framework | React 18 + TypeScript |
| Build Tool | Vite 5 |
| UI Library | MUI (Material UI) 5 |
| Animations | Framer Motion |
| Routing | React Router 6 |
| HTTP Client | Axios |
| Notifications/Toasts | Notistack |
| Auth | JWT decoding via `jwt-decode`, token stored in `localStorage` |
| File downloads | `file-saver` (bill PDFs) |

### Testing / Tooling
- **Playwright** — full browser click-through regression suite (`browser-tests/`)
- **Maven Surefire** — backend unit tests

> **Note:** An older Angular frontend still exists under `Frontend/` for historical reference only. **`Frontend-React/` is the active, maintained frontend.**

---

## Architecture & Project Structure

```
Cafe_Management_System/
├── BackEnd/
│   └── com.inn.cafe/                     # Spring Boot backend (Maven project)
│       ├── src/main/java/com/inn/cafe/
│       │   ├── rest/                     # @RestController interfaces (API contracts)
│       │   ├── restImpl/                 # Controller implementations
│       │   ├── service/                  # Service interfaces
│       │   ├── serviceImpl/              # Business logic implementations
│       │   ├── dao/                      # Spring Data JPA repositories
│       │   ├── POJO/                     # JPA entities (User, Product, Bill, Cart, ...)
│       │   ├── dto/ / wrapper/           # Request DTOs & response wrappers
│       │   ├── JWT/                      # JWT filter, token utilities
│       │   ├── config/                   # Spring Security config, CORS, Swagger, etc.
│       │   ├── exception/                # Custom exceptions + global exception handler
│       │   └── constents/                # Shared constants (roles, statuses, charges...)
│       └── src/main/resources/
│           └── application.properties    # DB, mail, JWT, Razorpay config
│
├── Frontend-React/                       # Active React frontend (Vite + TS)
│   ├── src/
│   │   ├── pages/                        # Route-level pages (Menu, Cart, Checkout, Dashboards...)
│   │   ├── components/                   # Reusable components (FoodCard, dialogs, etc.)
│   │   ├── layout/                       # App shell: Header, Sidebar, FullLayout
│   │   ├── services/                     # Axios API wrappers, one per backend module
│   │   ├── auth/                         # Token storage/decoding, role helpers
│   │   ├── shared/                       # Cross-cutting utilities (snackbar, razorpay loader, etc.)
│   │   └── api/                          # Axios instance + interceptors
│   └── public/assets/img/                # Menu & category photography
│
├── Frontend/                             # Legacy Angular frontend (reference only, not maintained)
├── browser-tests/                        # Playwright end-to-end click-through suite
├── images/                               # Screenshots for documentation
├── sql.sql                               # Legacy MySQL schema (kept for historical reference only)
└── README.md
```

**Request flow example (checkout):**

`CheckoutPage.tsx` → `cart.service.ts` (Axios) → `CartRest` → `CartRestImpl` → `CartServiceImpl` (business logic: pricing breakdown, coupons, loyalty points, Razorpay order creation) → `BillDao`/`CartDao` (JPA) → PostgreSQL.

---

## Features

### Customer-Facing Features

- **Modern storefront UI** — premium red/orange/green Swiggy/Zomato-inspired design system, framer-motion animations (card lift, image zoom, fade/slide transitions, skeleton loaders), fully responsive (desktop/tablet/mobile), dark mode toggle.
- **Signup / Login** — JWT-based auth with **real email OTP verification on signup** (a 6-digit code is emailed and must be verified before the account is created), "Forgot Password" email flow, change password.
- **Menu browsing** — real food photography per category (Pizza, Biryani, Beverages, Desserts), search with live suggestions, category filters, price/rating/veg-only filters, best-seller & new-arrival badges, spice level, prep time, calories-style metadata.
- **Recommendations** — "Recommended for You" section based on past orders.
- **Cart** — add/update/remove items, live subtotal/tax/delivery-charge/platform-fee breakdown, coupon code application with validation (expiry, usage limit, min order amount, max discount cap), free-delivery threshold.
- **Multi-step checkout** — address selection/creation, delivery instructions, payment method selection, loyalty points redemption, order review.
- **Real-time online payments (Razorpay)** — UPI, Credit/Debit Cards, Net Banking, and Wallets all through Razorpay's hosted Checkout widget; Cash on Delivery also supported. Payment success is verified server-side via HMAC-SHA256 signature verification (see [Payment Gateway section](#payment-gateway-razorpay-setup)).
- **Order tracking** — animated status timeline (Placed → Accepted → Preparing → Out for Delivery → Delivered), live delivery-partner assignment view.
- **Bill / order history** — view past orders, download PDF invoices, cancel orders, retry a failed payment, request refunds (admin-approved).
- **Loyalty points program** — earn points on every order, redeem points for discounts on future orders, view balance.
- **Coupons** — percentage or flat-amount discount codes with expiry dates, usage limits, and minimum order requirements.
- **Store locator** — browse all physical cafe/store locations with "Get Directions".
- **Notifications** — in-app notification bell (order placed, payment success/failure, refunds, delivery updates) + email notifications.

### Admin / Back-Office Features

- **Admin Dashboard** — sales analytics, order volume, revenue overview.
- **Manage Categories** — add/edit categories used to organize the menu.
- **Manage Products** — add/edit/delete menu items with price, description, category, veg/non-veg flag, spice level, best-seller/new-arrival flags, prep time, and image.
- **Manage Orders** — view all orders across customers, update order status (Placed/Accepted/Preparing/Out for Delivery/Delivered/Cancelled).
- **Manage Bills** — view bill details, download invoices, cancel bills, filter/search.
- **Manage Coupons** — create/edit discount coupons with full rule configuration.
- **Manage Stores** — add/edit physical store/location entries shown on the Store Locator.
- **Manage Users** — view all registered users, ping/notify a user, filter/search users.
- **Manage Delivery Partners** — register delivery partners, assign orders to riders, monitor delivery status.
- **Refunds** — issue refunds on successfully paid orders.
- **Change password.**

### Kitchen & Delivery Features

- **Kitchen Display Dashboard** — a real-time queue of incoming orders for kitchen staff to accept and progress through preparation stages.
- **Delivery Partner Dashboard** — riders log in with a dedicated `delivery` role to see orders assigned to them, mark orders as delivered, and toggle their availability.
- **Role-guarded routing** — delivery partners are automatically routed to their own dashboard and blocked from admin/customer pages (and vice-versa) via `RouteGuard`.

### Platform-Wide Features

- **JWT authentication & role-based authorization** (`admin`, `user`, `delivery` roles) enforced on both the API (Spring Security) and the frontend (`RouteGuard`).
- **Global exception handling** with consistent, descriptive JSON error responses.
- **Request validation** via Jakarta Bean Validation annotations on all DTOs.
- **Pagination & sorting** on list endpoints (users, products, orders).
- **Swagger / OpenAPI** interactive API documentation.
- **BCrypt password hashing.**
- **Externalized secrets** — DB credentials, JWT secret, mail credentials, and Razorpay keys are all environment-variable driven (no secrets hardcoded in source).
- **Automated Playwright regression suite** covering admin, customer, and delivery-partner flows end-to-end in a real browser.

---

## Email OTP Signup Verification

Signup is a **two-step, email-verified process** — no account is created until the user proves ownership of their email address:

1. **`POST /user/signup`** — the user submits name, email, contact number, and password. The backend does **not** create a `User` row yet. Instead it:
   - Hashes the password (BCrypt) and stores a *pending registration* in a `signup_otp` table (email, name, contact, hashed password, requested status).
   - Generates a random 6-digit OTP, hashes it (BCrypt) before storing it (the plaintext OTP is never persisted or logged), and sets a 10-minute expiry.
   - Emails the plaintext OTP to the user via `EmailUtil.sendOtpMail(...)` (real SMTP send — requires `MAIL_USERNAME`/`MAIL_PASSWORD` to be configured).
   - Responds with `"OTP sent to your email. Please verify to complete registration."`
2. **`POST /user/verifySignupOtp`** — the user submits `{ email, otp }`. The backend validates the OTP against the pending registration:
   - Wrong OTP → attempt counter incremented, up to `SIGNUP_OTP_MAX_ATTEMPTS = 5` tries before the pending registration is discarded.
   - Expired OTP (> 10 minutes old) → pending registration discarded, user must sign up again.
   - Correct OTP → the real `User` row is created (role `user`), the pending `signup_otp` row is deleted, and the account can now log in.
3. **`POST /user/resendSignupOtp`** — submits `{ email }` to regenerate and re-send a fresh OTP for an existing pending registration (e.g. if the original email didn't arrive), resetting the attempt counter.

The Signup dialog on the frontend (`SignupDialog.tsx`) reflects this as a two-step UI: fill the signup form → enter the 6-digit code emailed to you (with a "Resend OTP" option) → account created, ready to log in.

> ⚠️ Because a real OTP email is required to complete signup, `MAIL_USERNAME`/`MAIL_PASSWORD` **must** be configured (a real Gmail address + [App Password](https://myaccount.google.com/apppasswords) works well) — signup will fail with a mail error otherwise.

---

## User Roles

| Role | Description | Example Login |
|---|---|---|
| `admin` | Full back-office access: manage products/categories/orders/coupons/stores/users/delivery, view analytics, issue refunds. | `admin@cafe.com` |
| `user` | Customer role: browse menu, order, pay, track orders, manage profile/addresses. | `user@cafe.com` |
| `delivery` | Delivery-partner role: view assigned orders, mark delivered, manage availability. | registered via **Manage Delivery Partners** (admin) |

---

## Prerequisites

Make sure the following are installed before you begin:

| Requirement | Version | Notes |
|---|---|---|
| **Java (JDK)** | 21+ | Required by the Spring Boot backend (`java.version=21` in `pom.xml`) |
| **Maven** | 3.9+ | Or use the included `mvnw`/`mvnw.cmd` wrapper |
| **Node.js** | 18+ (LTS recommended) | Required by the Vite/React frontend |
| **npm** | 9+ | Ships with Node.js |
| **PostgreSQL** | 14+ | The application's database (schema auto-created on first run) |

---

## Setup & Installation

### 1. Clone the repository

```powershell
git clone https://github.com/Rahulshah1256/Cafe_Management_System.git
cd Cafe_Management_System
```

### 2. Set up PostgreSQL

Create an empty database named `cafesystem` (the app auto-creates/updates all tables via Hibernate `ddl-auto=update` — **no manual schema import needed**; `sql.sql` in the repo root is a legacy MySQL script kept only for historical reference and is not used by the current Postgres-backed app).

```sql
CREATE DATABASE cafesystem;
```

Note the username/password you'll connect with — you'll supply them as environment variables in the next step.

### 3. Configure environment variables

The backend reads all secrets from environment variables (with safe local-dev fallback defaults baked into `application.properties`). Set the following before starting the backend, or accept the defaults for a quick local trial:

| Variable | Purpose | Local default |
|---|---|---|
| `DB_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/cafesystem` |
| `DB_USERNAME` | Postgres username | `postgres` |
| `DB_PASSWORD` | Postgres password | `cafe_pg_pass` |
| `JWT_SECRET` | Secret key used to sign JWTs | dev-only placeholder (⚠️ **change for production**) |
| `MAIL_USERNAME` | SMTP username — **required for signup to work**, since a real OTP email is sent on registration (also used for password-reset/notifications) | *(empty — signup/email features fail until set)* |
| `MAIL_PASSWORD` | SMTP password / app password | *(empty)* |
| `MAIL_HOST` / `MAIL_PORT` | SMTP server | `smtp.gmail.com` / `587` |
| `RAZORPAY_KEY_ID` | Razorpay API Key ID | randomly-generated placeholder (⚠️ **not a real account — see [Payment Gateway](#payment-gateway-razorpay-setup)**) |
| `RAZORPAY_KEY_SECRET` | Razorpay API Key Secret | randomly-generated placeholder |

Example (PowerShell):

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/cafesystem"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_postgres_password"
$env:JWT_SECRET = "a-long-random-production-secret"
$env:MAIL_USERNAME = "you@gmail.com"
$env:MAIL_PASSWORD = "your-gmail-app-password"
$env:RAZORPAY_KEY_ID = "rzp_test_xxxxxxxxxxxxxx"
$env:RAZORPAY_KEY_SECRET = "your_razorpay_key_secret"
```

Example (bash/Linux/macOS):

```bash
export DB_URL="jdbc:postgresql://localhost:5432/cafesystem"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_postgres_password"
export JWT_SECRET="a-long-random-production-secret"
export MAIL_USERNAME="you@gmail.com"
export MAIL_PASSWORD="your-gmail-app-password"
export RAZORPAY_KEY_ID="rzp_test_xxxxxxxxxxxxxx"
export RAZORPAY_KEY_SECRET="your_razorpay_key_secret"
```

### 4. Run the backend

```powershell
cd BackEnd/com.inn.cafe
mvn spring-boot:run
```

The API starts on **http://localhost:8081**. On first boot, Hibernate automatically creates all required tables in the `cafesystem` database.

### 5. Run the frontend

In a **new terminal**:

```powershell
cd Frontend-React
npm install     # first time only
npm run dev
```

Open **http://localhost:4200** in your browser. The frontend is pre-configured to call the backend at `http://localhost:8081`.

> Both servers must be running simultaneously for the app to work — the frontend is a pure SPA that talks to the backend over REST.

---

## Default Login Credentials

If you're starting from a fresh database, sign up a new account from the homepage (Signup → choose role where applicable). If seed/test data is already present in your database, the following accounts are commonly used throughout this project's testing:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@cafe.com` | `admin@123` |
| Customer | `user@cafe.com` | `user@123` |

> Delivery-partner accounts are created by an admin via **Manage Delivery Partners**, not through public signup.

---

## API Documentation (Swagger)

Once the backend is running, interactive API documentation is available at:

```
http://localhost:8081/swagger-ui/index.html
```

This lists every REST endpoint (`/user`, `/product`, `/category`, `/cart`, `/bill`, `/payment`, `/coupon`, `/loyalty`, `/delivery`, `/store`, `/notification`, `/dashboard`, `/address`, ...) with request/response schemas, generated directly from the backend's DTOs and validation annotations.

---

## Running Tests

### Backend unit tests

```powershell
cd BackEnd/com.inn.cafe
mvn test
```

Runs the full JUnit 5 + Mockito suite covering services like cart/checkout logic, payment retry/verification, coupons, loyalty points, and authorization rules.

### Frontend build check

```powershell
cd Frontend-React
npm run build
```

Runs the TypeScript compiler (`tsc -b`) followed by a production Vite build — this is the fastest way to catch type errors across the whole frontend.

---

## Browser End-to-End Tests

A Playwright-based click-through suite lives in `browser-tests/` and exercises admin, customer, and delivery-partner flows against **live, running** dev servers (frontend on `:4200`, backend on `:8081`).

```powershell
cd browser-tests
npm install     # first time only
node clickthrough.js
```

Make sure both the backend and frontend are already running before executing the suite. It prints a `PASS`/`FAIL` line per scenario and a final summary.

---

## Payment Gateway (Razorpay) Setup

Online payments (UPI, Credit/Debit Cards, Net Banking, and Wallets) are integrated via the **real Razorpay Orders API + hosted Checkout widget** — this is a genuine, production-shaped integration, not a simulator:

1. Backend creates a real order via the Razorpay Orders API (`RazorpayService.createOrder`).
2. Frontend opens the Razorpay Checkout widget against that order (`src/shared/razorpay.ts`), where the customer completes payment using any supported method.
3. On success, the frontend sends the signed `order_id` / `payment_id` / `signature` back to `POST /payment/verify`, where the backend cryptographically verifies the HMAC-SHA256 signature before marking the order as paid.

**By default, this repository ships with randomly-generated placeholder credentials** (`RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET`) so the project runs out-of-the-box — but these are **not a real Razorpay account** and cannot process real charges. Attempting an online payment with the defaults will surface a clear, graceful error ("Payment gateway is not available right now… please try Cash on Delivery"), while **Cash on Delivery works fully without any configuration.**

To accept real payments:

1. Create a free account at [dashboard.razorpay.com](https://dashboard.razorpay.com/) and generate API keys (**Settings → API Keys**).
2. Set `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` as environment variables before starting the backend (see [Configure environment variables](#3-configure-environment-variables)).
3. Restart the backend. No code changes are required — online payments will work immediately with your real keys.

---

## Building for Production

**Backend** — produces an executable JAR:

```powershell
cd BackEnd/com.inn.cafe
mvn clean package
java -jar target/com.inn.cafe-0.0.1-SNAPSHOT.jar
```

**Frontend** — produces a static build in `Frontend-React/dist/`, ready to be served by any static file host or reverse proxy (e.g. Nginx) in front of the backend API:

```powershell
cd Frontend-React
npm run build
npm run preview   # optional local preview of the production build
```

Remember to set all environment variables (DB, JWT, mail, Razorpay) in your production environment — never commit real secrets to source control.

---

## Troubleshooting

| Symptom | Likely Cause / Fix |
|---|---|
| Backend fails to start with a DB connection error | Confirm PostgreSQL is running and `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` are correct; confirm the `cafesystem` database exists. |
| Frontend shows CORS errors | Confirm the backend is running on port `8081` and the frontend on `4200` — CORS is pre-configured for this pairing. |
| Login returns `400 Bad Request` | The login endpoint expects `{ "email": "...", "password": "..." }` (not `username`). |
| Email notifications / password reset don't send | `MAIL_USERNAME`/`MAIL_PASSWORD` are unset by default — configure a real SMTP account (e.g. a Gmail App Password) to enable email. |
| Signup fails / never receives OTP email | Signup now **requires** working SMTP — set `MAIL_USERNAME`/`MAIL_PASSWORD` (Gmail App Password recommended). Check spam folder. OTP expires after 10 minutes and allows 5 attempts; use "Resend OTP" if needed. |
| Online payments fail with "Payment gateway is not available" | Expected with default placeholder keys — see [Payment Gateway Setup](#payment-gateway-razorpay-setup) to use real Razorpay credentials. Cash on Delivery is unaffected. |
| `mvn`/`java` not recognized, or wrong Java version | Ensure `JAVA_HOME` points at a JDK **21+** installation and its `bin` folder is on your `PATH`. |

---

## Project History / Legacy Notes

- This project was originally built with an **Angular frontend + MySQL backend on Java 8/Spring Boot 2**. It has since been fully migrated to **React + TypeScript** and **PostgreSQL + Java 21/Spring Boot 3**, with substantial feature and architecture upgrades (see feature list above).
- The legacy Angular app remains under `Frontend/` purely for historical reference and is **not maintained or run** as part of this project anymore.
- `sql.sql` is a legacy MySQL Workbench export from the original schema and is **not used** by the current Postgres-backed application (Hibernate manages the schema automatically).

---

<p align="center">Built with ❤️ — a complete, real-world food ordering &amp; cafe management platform.</p>
