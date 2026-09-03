# PostgreSQL Integration

Until now, we have mostly worked above the database layer:

```
Controller
    ↓
Service
    ↓
Repository
    ↓
Spring Data JPA
    ↓
Hibernate
```

In this module, we open the lower part of the stack:

```
Spring Data JPA
      ↓
Hibernate
      ↓
JDBC
      ↓
PostgreSQL JDBC Driver
      ↓
DataSource / Connection Pool
      ↓
PostgreSQL
```

The main engineering questions are:

- How does Spring Boot know where PostgreSQL is?
- How does Hibernate actually communicate with the database?
- What is a `DataSource`?
- Why do we need a PostgreSQL JDBC driver?
- Who creates and changes database tables?
- Why is `ddl-auto:update` not a production migration strategy?
- What problem does Flyway solve?
- Where does JPA stop and PostgreSQL begin?
- How do we diagnose database problems by layer?

## Core Concepts

### The Complete Persistence Stack

A typical BrightMove repository call:

```
propertyRepository.findById(id);
```

travels through several layers:

```
Your Java Code
    ↓
Spring Data JPA
    ↓
JPA
    ↓
Hibernate
    ↓
JDBC
    ↓
PostgreSQL JDBC Driver
    ↓
DataSource / Connection
    ↓
PostgreSQL
```

Each layer has a different responsibility.

| Technology             | Main responsibility                                              |
| ---------------------- | ---------------------------------------------------------------- |
| Spring Data JPA        | Repository abstraction and implementation                        |
| JPA                    | Standard persistence/ORM specification                           |
| Hibernate              | JPA implementation, ORM, entity tracking, SQL generation         |
| JDBC                   | Standard Java relational database API                            |
| PostgreSQL JDBC Driver | PostgreSQL-specific JDBC implementation                          |
| DataSource             | Provides database connections                                    |
| Connection Pool        | Reuses database connections efficiently                          |
| Flyway                 | Versioned schema migration                                       |
| PostgreSQL             | Stores data, executes SQL, enforces constraints and transactions |

### JDBC

JDBC stands for **Java Database Connectivity**. It is the standard Java API for working with relational databases.

Without Hibernate or Spring Data JPA, Java code might work at a lower level:

```
Connection connection = ...;
PreparedStatement statement = connection.prepareStatement(
        "SELECT * FROM properties WHERE id = ?"
);
statement.setObject(1, id);
ResultSet result = statement.executeQuery();
```

Hibernate uses JDBC underneath its ORM abstraction.

### PostgreSQL JDBC Driver

JDBC defines the Java database API, but a database-specific implementation is still needed.

```text
JDBC API
    ↓
PostgreSQL JDBC Driver
    ↓
PostgreSQL
```

A Maven project commonly includes:

```
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

The driver answers: **How does Java communicate with PostgreSQL specifically?**

### DataSource

A `DataSource` represents a source of database connections.

```
DataSource
≠ one permanent connection

DataSource
= mechanism for obtaining database connections
```

Conceptually:

```
Application
    ↓
DataSource
    ↓
Connection
    ↓
PostgreSQL
```

### Connection Pool

Creating a new physical database connection for every query is expensive. Applications normally reuse a pool of connections.

```
Database work needed
    ↓
Borrow connection
    ↓
Execute work
    ↓
Return connection to pool
```

Spring Boot commonly uses HikariCP as its connection pool implementation.

### `application.yml`

Spring Boot needs configuration that tells it where PostgreSQL is and how to authenticate.

```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/brightmove
    username: brightmove_user
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate

    show-sql: true
```

The JDBC URL:

```
jdbc:postgresql://localhost:5432/brightmove
```

means:

```
jdbc       → use JDBC
postgresql → database type
localhost  → database host
5432       → PostgreSQL port
brightmove → database name
```

The database username and password belong to the backend application's database identity, not to a BrightMove end user.

### Spring Boot Auto-Configuration

Spring Boot combines the PostgreSQL JDBC dependency with `spring.datasource` configuration to auto-configure the normal database infrastructure.

```
application.yml
      ↓
Spring Boot reads datasource properties
      ↓
finds PostgreSQL driver
      ↓
creates/configures DataSource
      ↓
connection pool configured
      ↓
Hibernate can use PostgreSQL
```

### Schema vs Entity Model

The Java entity model and PostgreSQL schema are related, but they are not the same thing.

```
@Entity
public class Property {
    @Id
    private UUID id;
    private String title;
    private BigDecimal price;
}
```

```
properties
├── id
├── title
└── price
```

Entity mapping answers: **How should Java state map to relational state?**

Schema management answers: **How does PostgreSQL reach and maintain that structure?**

### DDL

DDL means **Data Definition Language**.

```
CREATE TABLE ...
ALTER TABLE ...
DROP TABLE ...
```

These statements modify database structure.

### Hibernate Schema Generation and `ddl-auto`

Spring Boot exposes Hibernate schema behavior through:

```
spring:
  jpa:
    hibernate:
      ddl-auto: ...
```

Common values:

```
none
validate
update
create
create-drop
```

`none`

Do not automatically manage the schema.

`validate`

Check whether the existing database schema is compatible with the entity mappings, but do not modify it.

```
Entity mappings
      ↓
compare
      ↕
Database schema
      ↓
compatible?
  ├── yes → continue
  └── no  → startup failure
```

**Validate checks. It does not fix.**

`update`

Try to change the schema so it matches the current entity model. Convenient in development, but not a robust production migration strategy because Hibernate cannot reliably infer business intent for existing data.

`create`

Create/recreate the schema at startup. Useful only for disposable environments.

`create-drop`

Create the schema at startup and drop it at shutdown. Useful only for temporary environments.

### Development vs Production

Local development may sometimes use `update` or `create-drop`. Production-oriented systems normally move toward:

```
ddl-auto: validate
```

or:

```
ddl-auto: none
```

combined with explicit migrations.

### Why Automatic `update` Is Not Enough

Suppose BrightMove adds:

```
@Column(nullable = false)
private PropertyStatus status;
```

but 50,000 rows already exist.

A safe migration requires reasoning:

```
1. Add status as nullable
2. Backfill existing rows with AVAILABLE
3. Make status NOT NULL
```

Hibernate cannot infer that business decision from the field declaration alone.

### Database Migrations

A migration describes a controlled transition:

```
Database V1
    ↓
Migration V2
    ↓
Database V2
```

Migrations record **how** the database evolves, not only what the final structure should be.

### Flyway

Flyway manages versioned database migrations.

```
Spring Boot
   │
   ├── Flyway → schema evolution
   └── Hibernate → ORM/persistence
          │
          ▼
      PostgreSQL
```

Typical migration directory:

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__create_initial_schema.sql
        ├── V2__add_property_status.sql
        └── V3__add_viewing_index.sql
```

Example:

```
ALTER TABLE properties
ADD COLUMN status VARCHAR(50);

UPDATE properties
SET status = 'AVAILABLE'
WHERE status IS NULL;

ALTER TABLE properties
ALTER COLUMN status SET NOT NULL;
```

This implements:

```
add structure
    ↓
migrate existing data
    ↓
tighten constraint
```

**Flyway History**:

Flyway records which migrations have already been applied.

| Version | Description         | Status  |
| ------- | ------------------- | ------- |
| 1       | create schema       | success |
| 2       | add property status | success |
| 3       | add viewing index   | success |

On startup, Flyway compares migration files with schema history and runs only unapplied versions.

**Migration Ordering**:

Versions establish deterministic order:

```
V1 → V2 → V3
```

**Do Not Rewrite Applied Migrations**

Once a migration has been applied in a shared environment, treat it as historical record.

```
V3 introduced something
V4 modifies it
V5 later changes it again
```

Flyway also uses checksums to detect changes to already-applied migration files.

### Schema Migration vs Data Migration

Schema migration:

```
ALTER TABLE properties
ADD COLUMN status VARCHAR(50);
```

Data migration:

```
UPDATE properties
SET status = 'AVAILABLE';
```

Real production migrations often require both.

### Flyway + Hibernate `validate`

A clean responsibility split is:

```
Flyway
   ↓
create/evolve schema intentionally
   ↓
PostgreSQL
   ↑
validate
   ↑
Hibernate entity mappings
```

Startup becomes:

```
1. Configure DataSource
2. Flyway applies missing migrations
3. Hibernate reads entity mappings
4. Hibernate validates schema
5. Application becomes ready
```

### SQL/JPA Boundary

JPA/Hibernate abstracts persistence, but PostgreSQL remains an engineering layer.

```
Java objects
      ↓
Hibernate
      ↓
SQL
      ↓
PostgreSQL
      ↓
tables / indexes / constraints / locks
```

### What Hibernate Handles

```
entity mapping
persistence context
dirty checking
lazy loading
SQL generation
entity lifecycle
optimistic locking strategy
```

### What PostgreSQL Handles

```
actual data storage
SQL execution
query planning
indexes
constraints
foreign keys
database transactions
locking
data integrity
```

### Constraints as Final Integrity Protection

Application checks can improve user experience:

```
if (userRepository.existsByEmail(email)) {
    throw new EmailAlreadyExistsException(email);
}
```

But the database must provide the final guarantee:

```
UNIQUE (email)
```

```
Application check
→ friendly early handling

PostgreSQL constraint
→ final integrity guarantee
```

### Foreign Keys

Java:

```
@ManyToOne
@JoinColumn(name = "agent_id")
private Agent agent;
```

PostgreSQL:

```
properties.agent_id
        ↓
FOREIGN KEY
        ↓
agents.id
```

### Indexes

A repository method can be functionally correct but slow at scale.

```
findByAgentId(agentId);
```

may benefit from:

```
CREATE INDEX idx_properties_agent_id
ON properties(agent_id);
```

So:

```
Correct ORM mapping
≠
efficient database design
```

### Three Query Levels

**Spring Data abstraction**

```
findByCity("Edinburgh");
```

**JPQL**

```
SELECT p
FROM Property p
WHERE p.city = :city
```

**Native SQL**

```
SELECT *
FROM properties
WHERE city = 'Edinburgh';
```

Use the simplest abstraction that expresses the requirement cleanly.

```
Simple CRUD/query
→ Spring Data

Fixed complex entity query
→ JPQL

Dynamic filters
→ Specification

Database-specific or measured performance need
→ consider native SQL
```

## Mental Model

The main persistence model is:

```
Application Code
      ↓
Spring Data JPA
      ↓
Hibernate / JPA
      ↓
JDBC
      ↓
DataSource / Connection Pool
      ↓
PostgreSQL JDBC Driver
      ↓
PostgreSQL
```

Schema evolution follows a connected path:

```
Migration files
      ↓
Flyway
      ↓
PostgreSQL schema
      ↑
Hibernate validates
      ↑
Entity mappings
```

Responsibility map:

```
application.yml
→ connection configuration

DataSource
→ connection access

Connection Pool
→ reusable connections

Spring Data JPA
→ repository abstraction

Hibernate
→ ORM and entity lifecycle

Flyway
→ database version history and schema evolution

PostgreSQL
→ actual data, SQL execution, constraints, indexes, transactions
```

The central idea is:

> **JPA abstracts PostgreSQL, but does not replace PostgreSQL.**

## Spring / Java Implementation

### Maven Dependencies

A BrightMove persistence setup conceptually needs:

```
Spring Data JPA
PostgreSQL JDBC Driver
Flyway
```

PostgreSQL example:

```
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Exact Flyway dependency details may depend on the Spring Boot/Flyway version in use.

### `application.yml`

```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/brightmove
    username: brightmove_user
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate

    show-sql: true
```

`show-sql: true` can help while learning by exposing generated SQL.

### Flyway Directory

```text
src/main/resources/
└── db/
    └── migration/
        ├── V1__create_agents.sql
        ├── V2__create_properties.sql
        └── V3__add_property_status.sql
```

### Initial Schema Migration

```
CREATE TABLE agents (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE properties (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    agent_id UUID NOT NULL,

    CONSTRAINT fk_properties_agent
        FOREIGN KEY (agent_id)
        REFERENCES agents(id)
);
```

### Entity Mapping

```
@Entity
@Table(name = "properties")
public class Property {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "agent_id",
        nullable = false
    )
    private Agent agent;
}
```

The Java model and PostgreSQL schema should agree.

## Request & Data Flow

Suppose BrightMove handles:

```
GET /properties/{id}
```

Service:

```
@Transactional(readOnly = true)
public PropertyResponse getProperty(UUID id) {

    Property property = propertyRepository.findById(id)
            .orElseThrow(
                () -> new PropertyNotFoundException(id)
            );

    return propertyMapper.toResponse(property);
}
```

Complete flow:

```
HTTP request
    ↓
Controller
    ↓
Service
    ↓
PropertyRepository.findById(id)
    ↓
Spring Data JPA
    ↓
Hibernate
    ↓
SQL generated
    ↓
JDBC
    ↓
DataSource / pooled connection
    ↓
PostgreSQL JDBC Driver
    ↓
PostgreSQL executes query
    ↓
database result
    ↓
Hibernate maps row
    ↓
Property becomes managed
    ↓
Mapper
    ↓
Response DTO
    ↓
HTTP response
```

If the mapper accesses a lazy Agent:

```
property.getAgent().getName();
```

and the persistence context is still active, Hibernate may execute another SQL query to load the Agent.

## BrightMove Example

### Project Structure

```
brightmove/
│
├── src/main/java/com/brightmove/
│   ├── property/
│   │   ├── Property.java
│   │   ├── PropertyRepository.java
│   │   ├── PropertyService.java
│   │   └── PropertyController.java
│   │
│   └── agent/
│       └── Agent.java
│
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       ├── V1__create_agents.sql
│       ├── V2__create_properties.sql
│       └── V3__add_property_status.sql
│
└── pom.xml
```

### Startup Flow

```
Spring Boot starts
        ↓
reads application.yml
        ↓
configures DataSource
        ↓
database connectivity established
        ↓
Flyway checks migration history
        ↓
missing migrations applied
        ↓
Hibernate reads entity mappings
        ↓
Hibernate validates schema
        ↓
application beans created
        ↓
BrightMove ready
```

### Example Schema Change

Entity change:

```
@Enumerated(EnumType.STRING)
@Column(nullable = false)
private PropertyStatus status;
```

Migration:

```
ALTER TABLE properties
ADD COLUMN status VARCHAR(50);

UPDATE properties
SET status = 'AVAILABLE'
WHERE status IS NULL;

ALTER TABLE properties
ALTER COLUMN status SET NOT NULL;
```

Then Hibernate validates that the resulting schema matches its mapping.

## Common Mistakes

**Thinking Hibernate Communicates Directly With PostgreSQL**

Useful high-level model:

```
Hibernate → PostgreSQL
```

More precise model:

```
Hibernate
→ JDBC
→ PostgreSQL Driver
→ PostgreSQL
```

---

**Thinking `DataSource` Is One Connection**

```
DataSource
→ source/provider of connections
```

not one permanent connection.

---

**Hardcoding Production Credentials**

Prefer environment-based configuration such as:

```
password: ${DB_PASSWORD}
```

---

**Assuming `@Entity` Creates the Table**

`@Entity` describes persistence mapping. Schema creation/evolution is separate.

---

**Using `ddl-auto:update` as a Production Migration System**

Automatic update cannot reliably infer business-aware transformations of existing data.

---

**Editing Applied Migrations**

Create a new migration instead of rewriting history.

---

**Thinking Flyway Reads Entity Classes**

Flyway executes migration files. It does not invent migrations from entity changes.

---

**Suppressing Hibernate Validation Failure**

If entity mappings and schema disagree, startup failure is useful. It is safer than failing later during live traffic.

---

**Assuming Correct Repository Code Means Good Performance**

A logically correct query may still need proper PostgreSQL indexing or query tuning.

---

**Relying Only on Application Checks for Integrity**

Critical invariants should often be backed by database constraints such as `UNIQUE`, `FOREIGN KEY`, and `NOT NULL`.

## Engineering Trade-offs

**ORM Convenience vs Database Visibility**

Spring Data JPA and Hibernate reduce persistence boilerplate, but abstraction can hide:

```
generated SQL
query count
index usage
locking
database constraints
```

A backend engineer should use the abstraction while still understanding the database beneath it.

---

**Portability vs PostgreSQL Features**

JPQL and JPA reduce database-specific coupling. Native SQL and PostgreSQL-specific features provide more control.

The decision should be based on engineering value rather than hypothetical portability alone.

---

**Automatic Schema Management vs Explicit Migrations**

Automatic schema update is convenient during experimentation.

Flyway requires more explicit work but provides:

```
version history
controlled schema evolution
repeatable deployments
explicit data migration
```

---

**Startup Failure vs Runtime Failure**

With `ddl-auto: validate`, an incompatible schema can stop the application during startup.

```
fail during deployment
>
fail unpredictably during customer requests
```

---

**Application Checks vs Database Constraints**

```
Application
→ meaningful early error

PostgreSQL
→ final integrity guarantee
```

They are complementary rather than redundant.

## Summary

The complete persistence stack is:

```
Application
    ↓
Spring Data JPA
    ↓
JPA / Hibernate
    ↓
JDBC
    ↓
DataSource / Connection Pool
    ↓
PostgreSQL JDBC Driver
    ↓
PostgreSQL
```

Important responsibilities:

```
Spring Data JPA
→ repository abstraction

Hibernate
→ ORM, entity lifecycle, SQL generation

JDBC
→ Java relational database API

PostgreSQL JDBC Driver
→ PostgreSQL-specific JDBC implementation

DataSource
→ provides database connections

Connection Pool
→ reuses connections efficiently

Flyway
→ versioned database migrations

PostgreSQL
→ executes SQL and protects actual stored data
```

Configuration:

```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/brightmove
    username: brightmove_user
    password: ${DB_PASSWORD}
```

Schema strategy:

```
Development experiment
→ update/create may sometimes be convenient

Controlled environments
→ Flyway migrations

Hibernate
→ validate schema
```

Migration mental model:

```
change domain model
    ↓
decide database transformation
    ↓
decide meaning of existing data
    ↓
write versioned migration
    ↓
Flyway applies it
    ↓
Hibernate validates
    ↓
application starts
```

The central SQL/JPA boundary is:

```
Hibernate
→ maps objects and generates persistence operations

PostgreSQL
→ executes SQL, uses indexes, enforces constraints,
  manages transactions, and stores the real data
```

A useful troubleshooting rule is:

```
Repository/query abstraction problem?
→ Spring Data JPA

Entity lifecycle / LAZY / persistence context?
→ Hibernate/JPA

Schema version problem?
→ Flyway

Connection problem?
→ DataSource / JDBC / network

Constraint / index / query execution problem?
→ PostgreSQL
```

The most important engineering principle from this module is:

**JPA abstracts the relational database, but it does not remove the database as a correctness, performance, or operational concern.**
