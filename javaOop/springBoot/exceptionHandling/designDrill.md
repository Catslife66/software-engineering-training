# Design Drill (Software Engineer Level)

Let’s design the complete request flow for this endpoint:

```
POST /viewings
```

Possible failures:

1. Request email is invalid.
2. Property does not exist.
3. Property exists but belongs to another agent.
4. Viewing slot was booked by another user at the same time.
5. Database unexpectedly crashes.

For each failure, tell me:

- Which layer detects it?
- Which exception is thrown? (Use reasonable names if needed.)
- Which HTTP status is returned?
- Why?

## Request flow

```
Client
  |
  | HTTP request
  v
Controller
  |
  | @RequestBody
  | JSON → RequestDTO
  |
  | @Valid
  | structural validation
  |
  | if invalid:
  |   MethodArgumentNotValidException
  |   ↓
  |   GlobalExceptionHandler
  |   ↓
  |   400 Bad Request
  |
  v
Service
  |
  | business validation
  | - resource exists?
  |     → service throws PropertyNotFoundException
  |     → handler returns 404
  | - ownership allowed?
  |     → service throws AccessDeniedException
  |     → handler returns 403
  | - Slot conflict / optimistic lock / duplicate booking?
  |     → service throws BookingConflictException or OptimisticLockException
  |     → handler returns 409
  |
  | @Transactional
  | business workflow starts
  |     → business validation
  |     → reserve slot / create viewing / save outbox message
  |     → repository writes
  |     → commit
  v
Repository
  |
  | Spring Data JPA
  v
Hibernate
  |
  | Persistence Context
  | Dirty Checking
  | Flush
  v
Database
  |
  | commit or rollback
  | - DataAccessException / unexpected Exception
  |     → rollback
  |     → global catch-all handler
  |     → 500 Internal Server Error
  v
Service returns DTO
  |
  v
Controller returns HTTP response
```

## Mental Model

```
Controller = API boundary
DTO = request/response shape
@Valid = structural validation
Service = business rules + transaction boundary
Repository = database access
Hibernate = ORM engine
Database = persistent state
@ControllerAdvice = error translator
```
