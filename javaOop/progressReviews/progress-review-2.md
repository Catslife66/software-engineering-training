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

## Phase 3 — Real Spring Boot Development

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

### Module 3.11 — Fetching & Performance ✅

Topics:

```
LAZY vs EAGER
LazyInitializationException
N+1 Problem
Fetch Join
EntityGraph
```

### Module 3.12 — Pagination & Sorting ✅

Topics:

```
Pageable
Page
Slice
Sort
```

### Module 3.13 — Specifications & Dynamic Search ✅

Topics:

```
Specification<T>
JpaSpecificationExecutor
Dynamic filtering
Combining optional filters
Search DTO design
```

### Module 3.14 — PostgreSQL Integration ✅

Topics:

```
3.14A — How Spring Boot reaches PostgreSQL
        DataSource
        JDBC driver
        Hibernate
        connection settings

3.14B — application.yml
        datasource URL
        username/password
        Hibernate configuration

3.14C — Schema management
        ddl-auto
        create/update/validate
        why production is different

3.14D — Database migrations
        Flyway/Liquibase concept
        versioned schema changes

3.14E — SQL/JPA boundary
        what Hibernate manages
        what PostgreSQL manages
        where native SQL/database features fit

3.14F — Small implementation/practice
        configure BrightMove
        trace startup
        reason about common failures
```

### Module 3.15 — Spring Security

Topics:

```
A. Security Mental Model
   What Spring Security actually does
   Authentication vs Authorization
   Request security pipeline

B. SecurityFilterChain
   Filters
   protected vs public endpoints
   request authorization rules

C. Authentication
   credentials
   Authentication object
   AuthenticationManager
   AuthenticationProvider

D. User Loading
   UserDetails
   UserDetailsService
   database-backed users

E. Password Security
   PasswordEncoder
   BCrypt
   registration vs login

F. SecurityContext
   authenticated principal
   SecurityContextHolder
   accessing the current user

G. Authorization
   roles
   authorities
   request-level authorization
   method-level authorization

H. Ownership Authorization
   "Can this user modify THIS Property?"
   identity vs role vs ownership

I. JWT / Session Integration
   how authentication persists between requests
   where JWT fits into Spring Security

J. Error Handling
   401 Unauthorized
   403 Forbidden
   authentication failures

K. BrightMove Integration
   registration
   login
   authenticated requests
   protected Property operations
   ownership checks

L. Security Review
   complete request trace
   common mistakes
   final canonical notes
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
