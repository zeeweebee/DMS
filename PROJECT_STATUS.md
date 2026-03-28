# DMS (Dealer Management System) — Project Status

**Date:** March 27, 2026  
**Stack:** Spring Boot 3 (Java) · Angular 21 · MySQL 8

---

## Overall Status

| Layer    | Status      |
|----------|-------------|
| Backend  | ✅ Complete  |
| Frontend | ✅ Complete  |
| Database | ✅ Schema defined |

---

## Backend

### Tech Stack
- Spring Boot 3, Spring Security 6, Spring Data JPA
- JWT authentication (stateless)
- MySQL 8 via Hibernate
- Lombok, BCrypt password encoding

### Modules & Endpoints

#### Auth — `/api/auth`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/login` | Public | Authenticate and receive JWT + role |
| POST | `/register` | Public | Register a new user with role |

#### Dealers — `/api/dealers`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/` | ADMIN | Paginated list with keyword/status filters |
| GET | `/{id}` | ADMIN | Get dealer by ID |
| POST | `/` | ADMIN | Create dealer (auto-generates dealerCode) |
| PUT | `/{id}` | ADMIN | Update dealer |
| DELETE | `/{id}` | ADMIN | Delete dealer |

#### Vehicle Models — `/api/models`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/` | ALL roles | Paginated list with keyword/status filters |
| GET | `/active` | ALL roles | List of ACTIVE models only |
| GET | `/{id}` | ALL roles | Get model by ID |
| POST | `/` | ADMIN | Create model (validates fuelType, transmission) |
| PUT | `/{id}` | ADMIN | Partial update model |
| DELETE | `/{id}` | ADMIN | Delete model |

#### Vehicle Stock — `/api/stock`
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/` | ADMIN, DEALER | Paginated list; DEALER scoped to own stock |
| GET | `/{vin}` | ADMIN, DEALER | Get stock by VIN |
| POST | `/` | DEALER | Add stock (dealer derived from JWT) |
| POST | `/admin/dealer/{dealerId}` | ADMIN | Add stock for a specific dealer |
| PATCH | `/{vin}/book` | ADMIN, DEALER | Transition AVAILABLE → BOOKED |
| PATCH | `/{vin}/sell` | ADMIN, DEALER | Transition BOOKED → SOLD |
| POST | `/transfer` | ADMIN, DEALER | Transfer AVAILABLE stock to another dealer |
| DELETE | `/{vin}` | ADMIN | Delete stock record |

### Security
- JWT-based stateless authentication
- Role-based access control via `@PreAuthorize` and `SecurityFilterChain`
- Roles: `ADMIN`, `DEALER`, `EMPLOYEE`
- CORS configured via `application.properties` (`cors.allowed-origins`)
- BCrypt password hashing

### Data Model
- `User` — username, password_hash, role, dealer_id (nullable)
- `Dealer` — dealerName, dealerCode (unique), address, cityId, stateId, phone, email, status
- `VehicleModel` — modelName, variant, fuelType (PETROL/DIESEL/EV), transmission (MANUAL/AUTOMATIC), exShowroomPrice, status
- `VehicleStock` — VIN (PK), model (FK), dealer (FK), color, manufactureDate, stockStatus (AVAILABLE/BOOKED/SOLD)
- `BaseEntity` — createdAt, updatedAt (inherited by all entities)

### Cross-cutting Concerns
- `ApiResponse<T>` — unified response wrapper (success, message, data, timestamp)
- `PagedResponse<T>` — pagination wrapper (content, page, pageSize, totalCount, totalPages)
- `GlobalExceptionHandler` — handles `ResourceNotFoundException`, `AccessDeniedException`, `InvalidStockStatusException`, `IllegalArgumentException`
- JPA Specifications for dynamic filtering on all list endpoints

---

## Frontend

### Tech Stack
- Angular 21 (standalone components)
- Angular Router with lazy-free route guards
- HttpClient with JWT interceptor

### Components & Routes

| Route | Component | Access | Description |
|-------|-----------|--------|-------------|
| `/login` | LoginComponent | Public | Login form, stores JWT + role |
| `/hello` | HelloComponent | Authenticated | Dashboard landing page |
| `/dealers` | DealerListComponent | ADMIN | Paginated dealer list |
| `/dealers/add` | DealerFormComponent | ADMIN | Create dealer |
| `/dealers/edit/:id` | DealerFormComponent | ADMIN | Edit dealer |
| `/models` | ModelListComponent | ALL roles | Vehicle model catalog |
| `/models/add` | ModelFormComponent | ADMIN | Create model |
| `/models/edit/:id` | ModelFormComponent | ADMIN | Edit model |
| `/stock` | StockListComponent | ADMIN, DEALER | Stock list with status filter, book/sell/transfer/delete actions |
| `/stock/add` | StockFormComponent | ADMIN, DEALER | Add stock (ADMIN picks dealer; DEALER uses own account) |

### Services
- `AuthService` — login, logout, token/role storage, `isAuthenticated()`, `hasAnyRole()`
- `DealerService` — CRUD calls to `/api/dealers`
- `ModelService` — CRUD + `getActive()` calls to `/api/models`
- `StockService` — full stock operations including book, sell, transfer, delete

### Guards & Interceptors
- `AuthGuard` — redirects unauthenticated users to `/login`
- `RoleGuard` — redirects unauthorized roles to `/hello`
- `authInterceptor` — attaches `Authorization: Bearer <token>` to all outgoing requests

---

## Configuration

| Setting | Value |
|---------|-------|
| Backend port | `8080` |
| Frontend port | `4200` (default Angular dev server) |
| Database | MySQL `DMS` schema |
| JWT expiry | 24 hours (configurable via `JWT_EXPIRATION` env var) |
| CORS origin | `http://localhost:4200` (configurable via `CORS_ALLOWED_ORIGINS` env var) |

---

## Known Gaps / Pending

- No unit or integration tests beyond the default `DemoApplicationTests` stub
- `EMPLOYEE` role is defined but has no accessible module (blocked at service level)
- Frontend stock list uses client-side status filtering instead of server-side query params
- Frontend services do not unwrap the `ApiResponse<T>` envelope — raw response handling may need alignment
- No pagination UI controls on frontend list views (backend supports full pagination)
- No city/state lookup — `cityId` and `stateId` on Dealer are raw IDs with no reference table integration
