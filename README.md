# Banking Customer Enquiry Routing

Full-stack banking-support routing demo. The Java 17 / Spring Boot backend persists state in H2 and publishes post-commit STOMP events; the React/Vite frontend visualises agents, capacity, queues, conversations, and simulation activity.

```text
banking-agent/
├── backend/     Spring Boot REST, routing, WebSocket, simulation, datasets
└── frontend/    React + TypeScript + Vite client
```

## Run locally

Start the backend in one terminal:

```bash
cd backend
mvn spring-boot:run
```

Start the frontend in another:

```bash
cd frontend
cp .env.example .env # optional; these are the defaults
npm install
npm run dev
```

Open `http://localhost:5173`. Swagger UI remains at `http://localhost:8080/swagger-ui/index.html`; H2 is at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:routing`).

The frontend uses `VITE_API_BASE_URL` (default `http://localhost:8080`) for REST and `VITE_WS_URL` (default `http://localhost:8080/ws`) for STOMP. Backend CORS permits `http://localhost:5173` by default; override it with `FRONTEND_ORIGIN`.

## Single-EC2 deployment

The included `docker-compose.yml` is a low-cost personal-project deployment: PostgreSQL, Spring Boot, and an Nginx-served frontend run on one host. Nginx proxies `/api` and `/ws` to the backend, so production builds use same-origin requests and do not need public backend or database ports.

1. Copy the example environment file and replace the password and host:

   ```bash
   cp .env.example .env
   ```

2. Build and start the stack:

   ```bash
   docker compose up -d --build
   ```

3. Verify it locally or on the EC2 host:

   ```bash
   curl -f http://localhost/actuator/health
   ```

Only expose HTTP port `80` from the host. PostgreSQL and Spring Boot are internal Docker services. The `postgres-data` named volume persists database data across container restarts. The production profile uses PostgreSQL and `ddl-auto: update`; before treating the deployment as production-grade, replace that setting with versioned migrations such as Flyway.

For an EC2 deployment, use an Ubuntu instance, attach an Elastic IP, allow inbound `80` (and later `443`) from the internet, and limit SSH (`22`) to your own IP. Do not expose `5432` or `8080`.

## User workflows

- **Routing Dashboard** (`/dashboard`) loads agents and enquiry state from REST, then uses `/topic/routing` for live updates. It includes simulator controls, capacity timers, pending FIFO queue, and a bounded routing feed.
- **Customer** (`/customer`) creates a manual enquiry, observes its assignment at `/enquiries/{id}`, and chats using persisted history plus live STOMP updates.
- **Agent Console** (`/agent`) simulates an agent login, changes availability, receives assignment events, chats with customers, closes enquiries, and displays FAQ suggestions.

## Architecture and routing

Feature packages separate agents, enquiries, conversation, skills, assignments, routing and WebSocket integration. The category-to-skill map is cached as immutable lookup data. Mutable agent availability/capacity is never cached.

An enquiry is stored with its initial `ConversationMessage`, then routed transactionally. Candidate agents must be `ONLINE`, support the requested language, have remaining capacity, and satisfy containment `|required ∩ agentSkills| / |required| >= 0.80`. Candidates rank by lowest utilization (`active/maxCapacity`), then never-assigned/oldest `lastAssignedAt`, then UUID. The winning agent is pessimistically row-locked and rechecked before capacity is claimed; if a concurrent request consumed its last slot, routing tries the next candidate. This makes the `activeEnquiryCount <= maxCapacity` invariant durable and portable to PostgreSQL.

Pending enquiries stay FIFO by `createdAt`; retry occurs every 5 seconds and whenever an agent comes online or capacity grows. Closing is idempotent and releases capacity once. Scheduled jobs retry pending work, close inactive conversations, and reroute enquiries assigned to long-offline agents.

## REST

- `POST /api/agents`, `GET /api/agents`, `GET /api/agents/{id}`
- `PATCH /api/agents/{id}/status`, `PATCH /api/agents/{id}/capacity`, `POST /api/agents/{id}/heartbeat`
- `POST /api/enquiries`, `GET /api/enquiries`, `GET /api/enquiries/pending`, `GET /api/enquiries/{id}`
- `POST /api/enquiries/{id}/messages`, `GET /api/enquiries/{id}/messages`, `POST /api/enquiries/{id}/close`
- `POST /api/routing/retry-pending`

Example create enquiry:

```json
{"customerId":"CUSTOMER-123","category":"CARD_PAYMENT_DISPUTE","preferredLanguage":"ENGLISH","message":"I do not recognise a card payment."}
```

## STOMP

Connect to `/ws`, send application messages to `/app/enquiries/{enquiryId}/messages`, and subscribe to `/topic/routing`, `/topic/enquiries/{enquiryId}`, or `/topic/agents/{agentId}/assignments`. Chat and routing events are published only after their persisted state commits. REST conversation history reconstructs state after reconnect.

## Evolution

The JPA/domain services are database-independent: H2 can be replaced by PostgreSQL. Spring Cache is limited to immutable mappings, so its `simple` provider can become Redis without changing routing logic. The built-in broker can later become a scalable broker; local deployment can later move to AWS.

## Limitations

No authentication, authorization, external broker, Redis, Kafka, containers, cloud infrastructure, or microservices are included in this version.

## Realistic simulation data

The real [BANKING77 dataset](https://huggingface.co/datasets/PolyAI/banking77) is bundled under `backend/src/main/resources/sample-data/banking77/` as its official PolyAI train/test CSV files. It is loaded once from the classpath; the running application never calls Hugging Face. To refresh it, run `backend/scripts/download-banking77.sh`.

BANKING77 fine-grained intents are centrally mapped in `simulation/banking77/Banking77IntentMapping` to broader routing categories (card payment dispute/fraud/services, ATM query/dispute, transfer, account, identity, fees, loan, mortgage, and general banking). The existing category-to-required-skill mapping remains the only routing input.

Use `POST /api/simulation/enquiries` for one real sample, or `POST /api/simulation/enquiries/batch?count=20` for 1–500. `POST /api/simulation/start` enables randomized traffic and `/stop` stops only future arrivals; in-flight handling continues. `GET /api/simulation/status` and `/dashboard` expose runtime state; `/reset` removes simulation-created enquiries, messages, histories, and timer state only.

Handling timers are per-enquiry, not per-agent. Configured category ranges live in `backend/src/main/resources/application.yml`; assignment creates `SIMULATION_HANDLING_STARTED` with a completion timestamp, and expiry closes through `EnquiryService`, so normal capacity release, pending retry, audit history, and post-commit WebSocket events remain canonical. Reassignment is guarded by the persisted agent/assignment timestamp token, preventing stale timers from closing a reassigned enquiry.

The Banking FAQ Kaggle source is [here](https://www.kaggle.com/datasets/rudrakumargupta/banking-faq-dataset-for-chatbot-training). Kaggle authentication is not required by startup. Place any downloaded CSV under `backend/src/main/resources/sample-data/knowledge-base/`; the current lightweight knowledge service exposes `GET /api/knowledge-base`, `/search?q=card`, `/category/{category}`, and `/api/enquiries/{id}/knowledge` using category and customer-message keyword matching. This is intentionally a simple seam for future AI/RAG work, with no embeddings or vector database.
