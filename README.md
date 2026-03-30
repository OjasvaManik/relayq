# RelayQ

A multi-tenant job queue service built with Java and Spring Boot. Organisations submit jobs to named queues with webhook
URLs — RelayQ handles scheduling, delivery, retries, and observability.

---

## Tech Stack

- **Java 21** + **Spring Boot 4**
- **PostgreSQL 16** — persistent job and tenant storage
- **Redis 7** — session management, OTP, and reset tokens
- **Spring Scheduler** — background job polling
- **Spring Security** + **JWT** — stateless authentication
- **Flyway** — database migrations
- **Docker Compose** — local infrastructure

---

## How It Works

RelayQ is a webhook-based job queue. Tenants register, create queues (each with a webhook URL), and submit jobs with a
payload, priority, and optional scheduled time. A background poller picks up eligible jobs every 5 seconds and POSTs the
payload to the queue's webhook URL. The tenant's own server handles the actual execution — RelayQ just ensures reliable,
ordered, retried delivery.

```
Client → POST /api/v1/jobs → DB (PENDING)
                                    ↓
                           Poller wakes every 5s
                                    ↓
                     Finds next eligible PENDING job
                     (priority DESC, created_at ASC)
                                    ↓
                     POSTs payload to webhook URL
                                    ↓
                Success → DONE   Failure → retry with exponential backoff → FAILED
```

---

## Features

- **Multi-tenancy** — complete data isolation between organisations
- **Priority queuing** — higher priority jobs are always processed first
- **Scheduled jobs** — submit a job with a future `scheduledAt` timestamp
- **Async processing** — jobs processed concurrently on a thread pool
- **Exponential backoff** — failed jobs retry at 10s, 20s, 40s intervals
- **Job logs** — every attempt logged with timestamp and error detail
- **Role-based access** — ADMIN and MEMBER roles with method-level security
- **Session invalidation** — JWT sessions stored in Redis, invalidated on logout
- **OTP flow** — email-based OTP for password reset

---

## Getting Started

### Prerequisites

- Java 21
- Docker

### Setup

```bash
git clone https://github.com/ojasvamanik/relayq
cd relayq
```

Generate a jwt secret:

```bash
openssl rand -base64 64
```

Create a `secrets.yaml` file in the same folder as `application.yaml`:

```yaml
DB_USER: relayq-user
DB_PASS: relayq-pass
DB_NAME: relayq-db
MAIL_USERNAME: your@email.com
MAIL_PASSWORD: your-app-password
JWT_SECRET: your-base64-encoded-secret
```

Start infrastructure:

```bash
docker compose up -d
```

Run the application:

```bash
./gradlew bootRun
```

Flyway will automatically run all migrations on startup.

---

## API Reference

All endpoints except `/api/v1/auth/**` require:

```
Authorization: Bearer <jwt>
```

### Authentication

| Method | Endpoint                       | Description                         |
|--------|--------------------------------|-------------------------------------|
| POST   | `/api/v1/auth/register`        | Create organisation + admin account |
| POST   | `/api/v1/auth/login`           | Login, returns JWT                  |
| POST   | `/api/v1/auth/logout`          | Invalidate session                  |
| POST   | `/api/v1/auth/forgot-password` | Send OTP to email                   |
| POST   | `/api/v1/auth/verify-otp`      | Verify OTP, returns reset token     |
| POST   | `/api/v1/auth/reset-password`  | Reset password using reset token    |

### Users

| Method | Endpoint                    | Auth  | Description                   |
|--------|-----------------------------|-------|-------------------------------|
| POST   | `/api/v1/users`             | Admin | Invite member to organisation |
| GET    | `/api/v1/users`             | Admin | List all users in tenant      |
| GET    | `/api/v1/users/{id}`        | Admin | Get specific user             |
| PATCH  | `/api/v1/users/{id}`        | Admin | Update name or role           |
| DELETE | `/api/v1/users/{id}`        | Admin | Remove user                   |
| GET    | `/api/v1/users/me`          | Any   | Get own profile               |
| PATCH  | `/api/v1/users/me/password` | Any   | Change own password           |

### Queues

| Method | Endpoint              | Auth  | Description                       |
|--------|-----------------------|-------|-----------------------------------|
| POST   | `/api/v1/queues`      | Admin | Create queue with webhook URL     |
| GET    | `/api/v1/queues`      | Any   | List all queues in tenant         |
| GET    | `/api/v1/queues/{id}` | Any   | Get specific queue                |
| PATCH  | `/api/v1/queues/{id}` | Admin | Update webhook URL or max retries |
| DELETE | `/api/v1/queues/{id}` | Admin | Delete queue                      |

### Jobs

| Method | Endpoint                       | Auth | Description                         |
|--------|--------------------------------|------|-------------------------------------|
| POST   | `/api/v1/jobs`                 | Any  | Submit job to a queue               |
| GET    | `/api/v1/jobs`                 | Any  | List jobs (Admin: all, Member: own) |
| GET    | `/api/v1/jobs/{id}`            | Any  | Get job details and status          |
| DELETE | `/api/v1/jobs/{id}`            | Any  | Cancel a PENDING job                |
| PATCH  | `/api/v1/jobs/{id}/reschedule` | Any  | Reschedule a PENDING or FAILED job  |

### Job Logs

| Method | Endpoint                 | Auth | Description            |
|--------|--------------------------|------|------------------------|
| GET    | `/api/v1/jobs/{id}/logs` | Any  | Get all logs for a job |

---

## Example Requests

**Register an organisation:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"orgName": "Acme Corp", "name": "Alice", "email": "alice@acme.com"}'
```

**Submit a job:**

```bash
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "queueId": "3d974bb8-5642-457f-85e4-1bc13936a79f",
    "payload": {"to": "alice@acme.com", "subject": "Hello"},
    "priority": 5
  }'
```

**Submit a scheduled job:**

```bash
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "queueId": "3d974bb8-5642-457f-85e4-1bc13936a79f",
    "payload": {"to": "alice@acme.com", "subject": "Reminder"},
    "priority": 3,
    "scheduledAt": "2026-04-01T09:00:00"
  }'
```

---

## Design Decisions

### Webhook-based execution

RelayQ does not execute jobs itself. Instead, each queue has a `webhook_url` — when a job is ready, RelayQ POSTs the
payload to that URL and the tenant's server handles execution. This makes RelayQ a pure orchestration layer with no
coupling to business logic. Tenants can handle any job type without any changes on RelayQ's side.

### `SELECT FOR UPDATE SKIP LOCKED`

The poller uses a pessimistic write lock with `SKIP LOCKED` on the job query. This ensures that if multiple poller
instances ever run simultaneously, they never pick up the same job — each one skips rows locked by another. Combined
with immediately marking the job as `PROCESSING` before the async thread takes over, double-processing is effectively
impossible.

### Async processing with transaction separation

The poller (`JobPoller`) and processor (`JobProcessor`) are intentionally split into two Spring beans. `@Transactional`
does not propagate across thread boundaries — so if both the lock acquisition and the webhook call lived in the same
`@Async` method, the lock would be released before the job status was committed. Separating them means the poller
acquires the lock, marks the job `PROCESSING`, and commits — then hands off to the async thread which opens its own
transaction for the webhook call and status update.

### Redis for sessions instead of stateless JWT

JWT is stateless by default — there is no way to invalidate a token before it expires. Storing session tokens in Redis
with a TTL matching the JWT expiry allows explicit logout, admin-forced session termination, and future support for max
concurrent sessions. The tradeoff is one extra Redis lookup per request, which is negligible.

### Redis for OTP and reset tokens

OTPs and reset tokens are inherently temporary. Storing them in Redis with TTL means expiry is handled automatically —
no cleanup jobs, no `expires_at` columns, no stale rows. PostgreSQL is reserved for data that actually needs to persist.

### Exponential backoff on retries

Failed jobs are rescheduled with a delay of `2^retryCount * 10` seconds. A job that fails three times waits 20s, then
40s, then 80s before each retry. This prevents a flapping webhook from hammering RelayQ's poller and avoids thundering
herd on recovery.

### Tenant isolation via JWT principal

`tenantId` is embedded in the `UserPrincipal` loaded from the JWT. Every query that touches tenant-scoped data (queues,
jobs, users) pulls `tenantId` from the principal — never from the request body. This makes it structurally impossible
for a user to query or modify another tenant's data, even if they craft a malicious request.

### HASH indexes for UUID foreign keys

All foreign key columns used in equality lookups (`tenant_id`, `queue_id`, `job_id`, `created_by`) use PostgreSQL HASH
indexes instead of the default B-TREE. HASH indexes are faster for pure equality comparisons and more space efficient.
B-TREE is reserved for the composite `(status, scheduled_at)` index on jobs, which requires range comparison support for
the poller query.

---

## Database Schema

```
tenants
  └──< users
  └──< queues
          └──< jobs
                  └──< job_logs
```

Jobs carry `tenant_id` directly (denormalised from `queue → tenant`) for fast tenant-scoped queries without joins.
