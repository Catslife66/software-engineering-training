## Phase 1 — Core OOP & Backend Architecture ✅ (Completed)

This phase is complete and does not need to be rewritten unless you later want a consolidated set of notes.

Topics covered:

- OOP Principles
- SOLID
- Layered Architecture
- DTO Design
- Service / Repository Architecture
- Dependency Injection (concept)
- Authentication (concept)
- Authorization (concept)
- Transaction Thinking

## Phase 2 — Backend Workflow & Reliability Engineering ✅ (Completed)

This phase focuses on engineering thinking rather than Spring implementation.

### 2.1 Business Workflows

- Business transactions
- Critical path vs side effects
- Business invariants
- Workflow decomposition

### 2.2 Reliability Engineering

- Consistency
- Idempotency
- Retry safety
- Failure recovery
- Eventual consistency

### 2.3 Asynchronous Architecture

- Message queues
- Domain events
- Outbox Pattern
- Synchronous vs asynchronous processing

### 2.4 Authorization

- Ownership
- Business authorization
- Permission checks

### 2.5 Phase 2 Review

- Engineering mental models
- Trade-offs
- Common interview discussions

## Phase 3 — Real Spring Boot Development (Restart Here)

### Module 3.1 — Spring Boot Project & Dependency Injection ✅

Topics:

```
Spring Boot project structure
@SpringBootApplication
Beans
Dependency Injection
@Component
@Service
@Repository
@Controller
Bean lifecycle
```

### Module 3.2 — REST API ✅

Topics:

```
@RestController
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@RequestBody
@PathVariable
@RequestParam
ResponseEntity
HTTP Status Codes
```

### Module 3.3 — Validation ✅

Topics:

```
@Valid
Validation lifecycle
@NotNull
@NotBlank
@Positive
@Size
@Email
Custom validation
Cross-field validation
```

### Module 3.4 — Exception Handling ✅

Topics:

```
Custom exceptions
@RestControllerAdvice
@ExceptionHandler
Error DTO design
Consistent API error contracts
```

### Module 3.5 — Spring Data JPA ✅

Topics:

```
Repository interfaces
JpaRepository
CRUD
Optional
Query derivation
Custom repository methods
Custom @Query
```

### Module 3.6 — Hibernate & Persistence Context ✅

Topics:

```
Entity lifecycle
Transient
Managed
Detached
Removed
Persistence Context
Dirty Checking
Flush
Commit
```

### Module 3.7 — Entity Mapping ✅

Topics:

```
@Entity
@Table
@Id
@Column
Enum mapping
Entity lifecycle callbacks
@Version
```

### Module 3.8 — Entity Relationships ✅

Topics:

```
@ManyToOne
@OneToMany
Foreign Keys
Owning Side
mappedBy
Cascade
orphanRemoval
Bidirectional consistency
Helper methods
```

### Module 3.9 — Transactions & Concurrency ✅

Topics:

```
Transactions
@Transactional
Transaction boundaries
Rollback
Propagation
Read-only transactions
Concurrency
Race conditions
Optimistic Locking
@Version
ObjectOptimisticLockingFailureException
Pessimistic Locking
Business Workflows
Multi-repository transactions
Transaction design
Outbox implementation in Spring
```

### Module 3.10 — Querying ✅

Topics:

```
Derived queries
Relationship queries
JPQL
DTO Projection
Aggregations
GROUP BY
HAVING
```

### Module 3.11 — Fetching & Performance

Topics:

```
LAZY vs EAGER
LazyInitializationException
N+1 Problem
Fetch Join
EntityGraph
```

### Module 3.12 — Pagination & Sorting

Topics:

```
Pageable
Page
Slice
Sort
```

### Module 3.13 — Specifications & Dynamic Search

Topics:

```
Specification<T>
JpaSpecificationExecutor
Dynamic filtering
Combining optional filters
Search DTO design
```

### Module 3.14 — PostgreSQL Integration

Topics:

```
Datasource configuration
SQL/JPA boundary
Schema management
Migration concepts (Flyway/Liquibase overview)
```

### Module 3.15 — Spring Security

Topics:

```
Security Filter Chain
Authentication
Authorization
SecurityContext
Ownership implementation
```

### Module 3.16 — Configuration

Topics:

```
application.yml
Profiles
Environment variables
@Configuration
@Bean
```

### Module 3.17 — Testing

Topics:

```
Unit tests
Mockito
Repository tests
Integration tests
MockMvc
```

### Module 3.18 — Production Structure

Topics:

```
Package organization
Configuration separation
Environment handling
Logging
Service boundaries
Production-ready project structure
```

### Module 3.19 — Phase 3 Consolidated Review

Topics:

```
End-to-end request flow
Spring architecture review
Performance review
Persistence review
Security review
Common interview questions
BrightMove architecture review
```

---

## Teaching Rules (Frozen)

These are now part of the classroom itself.

1. The curriculum is frozen.

```
No topic changes.
No module changes.
No moving lessons around.
```

2. Every lesson follows the same structure.

```
Purpose
Core Concepts
Mental Model
Spring / Java Implementation
Request & Data Flow
BrightMove Example
Common Mistakes
Engineering Trade-offs
Summary
```

3. One lesson = one response.

```
No Socratic interruptions.
No artificial pauses.
No splitting one lesson across multiple messages.
```

4. Questions come after the lesson.

The lesson is treated like a chapter in a book.

After reading and taking notes, you ask questions.

5. BrightMove is the continuous example.

BrightMove is not a separate roadmap.

It is the single example project used throughout the entire course.
