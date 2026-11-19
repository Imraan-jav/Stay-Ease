# PG / Hostel Management System

Backend-focused project: **Java 17, Spring Boot 3, PostgreSQL, MongoDB, JWT, Docker, Render**, with a small vanilla-JS frontend and a Postman collection.

## What it does
**Admin (owner):** add rooms (beds are auto-created), onboard tenants, allocate / transfer / check out beds, generate and track rent invoices, record payments, manage complaints, post notices, view dashboard and audit log.
**Tenant:** see room and rent, view invoices and pay (mock), raise complaints and track status, read notices.

## Design decisions (good interview talking points)
| Concern | Approach |
|---|---|
| Double allocation of a bed | DB **partial unique indexes** (`WHERE active`) on `allocations(bed_id)` and `allocations(tenant_id)`; violations map to HTTP 409. Safe under concurrent requests. |
| Monthly rent | `BillingScheduler` cron on the 1st + **catch-up on startup** (free hosts sleep). Generation is idempotent via `UNIQUE(allocation_id, month_year)`. |
| Mid-month joiners | First invoice is **prorated by days stayed**. |
| Late fees | Daily job marks invoices OVERDUE and applies per-day fee, capped at 25% of the amount. |
| Checkout rule | Blocked while the tenant has unpaid invoices. |
| Security | Stateless JWT, BCrypt, `@PreAuthorize` roles ADMIN/TENANT, tenants can only pay their own invoices. |
| Why MongoDB | Append-only audit logs and notices are schema-flexible documents. If Mongo is down the core app still works (audit is best-effort). |
| Schema management | Flyway migration, Hibernate `ddl-auto=validate`. |

## Run locally
```bash
docker compose up --build        # app on http://localhost:8080
```
Or without Docker: start PostgreSQL (db `pgdb`) and MongoDB, then `mvn spring-boot:run`.

Login: `admin@pg.com / Admin@123` (change via `ADMIN_EMAIL`, `ADMIN_PASSWORD`).
Swagger UI: `http://localhost:8080/swagger-ui.html`. Import `postman_collection.json` (run "Login as admin" first; it stores the token).

Suggested demo flow: Rooms (3 demo rooms are seeded) -> Tenants -> Allocations -> Invoices -> log in as the tenant -> pay / raise a complaint.

## Deploy on Render
1. Push to GitHub. Create a free **MongoDB Atlas** cluster and copy its connection string.
2. Render -> New -> Blueprint -> pick the repo (`render.yaml` creates the web service and PostgreSQL).
3. Set `MONGODB_URI` and `ADMIN_PASSWORD` in the service environment. Check Render's current free-tier limits (database expiry, sleeping instances).

## Environment variables
`DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD MONGODB_URI JWT_SECRET ADMIN_EMAIL ADMIN_PASSWORD SEED_DEMO PORT`

## API overview
| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/login` |
| Rooms/beds (admin) | `POST/GET /api/rooms`, `GET /api/beds?status=` |
| Tenants (admin) | `POST/GET /api/tenants`, `GET /api/me` |
| Allocations (admin) | `POST/GET /api/allocations`, `POST /{id}/checkout`, `POST /{id}/transfer` |
| Invoices | `GET /api/invoices`, `GET /mine`, `POST /generate?month=YYYY-MM`, `POST /{id}/pay` |
| Complaints | `POST /api/complaints`, `GET /mine`, `GET /` (admin), `PATCH /{id}/status` |
| Notices / reports | `GET/POST /api/notices`, `GET /api/reports/dashboard`, `GET /api/audit` |

## Ideas to extend
PDF receipts, multi-property support, Testcontainers integration tests, GitHub Actions CI, refresh tokens, rate limiting login.

## Resume bullets
- Built a PG management platform (Spring Boot, PostgreSQL, MongoDB) with JWT role-based access, bed allocation/transfer, prorated monthly invoicing and late-fee automation.
- Prevented double bed allocation under concurrency using PostgreSQL partial unique indexes; designed Flyway-managed schema.
- Stored audit trails and notices in MongoDB; containerised with Docker and deployed on Render; documented with Swagger and Postman.
