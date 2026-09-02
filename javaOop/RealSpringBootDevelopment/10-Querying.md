# Querying

Spring Data JPA gives us simple repository operations such as:

```java
findById(...)
findAll()
findByCity(...)
```

But real applications eventually need more expressive queries.

BrightMove may need to answer questions such as:

```
Find all properties owned by one agent.
Find all pending viewings for properties in Edinburgh.
Return only property title and agent name.
Count properties by city.
Return cities with more than 20 available properties.
```

This module covers the main query tools we use before moving into performance and search-specific concerns:

- derived queries
- relationship queries
- JPQL
- joins
- DTO projections
- aggregation
- `GROUP BY`
- `HAVING`

## Mental Model — Start From the Information Needed

Before choosing syntax, ask:

> What information does the application actually need?

There are several common query shapes.

```
Need entities matching conditions
→ filtering query

Need related data
→ relationship query / join

Need only a few fields
→ projection

Need totals/counts/averages
→ aggregation

Need aggregation per category
→ GROUP BY

Need to filter aggregated groups
→ HAVING
```

The query technology should follow the information requirement.

## Derived Queries

We introduced query derivation in Module 3.5.

Suppose:

```java
@Entity
public class Property {

    private String city;

    private BigDecimal price;

    private PropertyStatus status;
}
```

Spring Data can derive:

```java
List<Property> findByCity(String city);
```

Conceptually:

```sql
WHERE city = ?
```

Or:

```java
List<Property> findByCityAndStatus(
        String city,
        PropertyStatus status
);
```

Conceptually:

```sql
WHERE city = ?
AND status = ?
```

Spring parses the method name and generates the query.

## Relationship Queries

Derived queries can navigate entity relationships.

Suppose:

```java
class Property {

    @ManyToOne
    private Agent agent;
}
```

and:

```java
class Agent {

    private String email;
}
```

Then:

```java
List<Property> findByAgentEmail(String email);
```

follows:

```text
Property
   ↓
agent
   ↓
email
```

Spring Data derives the relationship query automatically.

Likewise, if:

```java
class Viewing {

    @ManyToOne
    private Property property;

    private ViewingStatus status;
}
```

we can write:

```java
List<Viewing> findByPropertyId(UUID propertyId);
```

or:

```java
List<Viewing> findByPropertyAgentIdAndStatus(
        UUID agentId,
        ViewingStatus status
);
```

The method path is:

```text
Viewing
  ↓
property
  ↓
agent
  ↓
id
```

plus:

```text
Viewing.status
```

A useful rule is:

> The repository entity is the starting point; the method name follows fields from there.

## When Derived Queries Stop Being Helpful

Derived queries are excellent when the requirement is simple:

```java
findByCity(...)
findByStatus(...)
findByAgentId(...)
```

But they can become unreadable:

```java
findByCityAndStatusAndBedroomsGreaterThanAndPriceLessThanOrderByPriceAsc(...)
```

At that point, explicit JPQL may communicate intent better.

For many optional filters, Specifications will eventually be a better solution.

So:

```text
Simple fixed query
→ derived method

Complex fixed query
→ JPQL

Many optional combinations
→ Specification
```

## JPQL

JPQL stands for:

> Jakarta Persistence Query Language

It looks similar to SQL, but it queries **entities and fields**, not tables and columns.

SQL:

```sql
SELECT *
FROM properties
WHERE city = 'Edinburgh';
```

JPQL:

```java
@Query("""
    SELECT p
    FROM Property p
    WHERE p.city = :city
""")
List<Property> findPropertiesByCity(
        @Param("city") String city
);
```

Notice:

```
SQL
properties
city

JPQL
Property
p.city
```

JPQL speaks the object model.

## Named Parameters

Consider:

```java
@Query("""
    SELECT p
    FROM Property p
    WHERE p.city = :city
      AND p.status = :status
""")
List<Property> findAvailableProperties(
        @Param("city") String city,
        @Param("status") PropertyStatus status
);
```

The query parameters:

```
:city
:status
```

are bound from the Java method arguments.

Using named parameters usually makes longer queries easier to read than positional parameters.

## JPQL Joins

Because our entities have relationships, JPQL can navigate them.

Suppose:

```
Property
   ↓
Agent
```

We can write:

```java
@Query("""
    SELECT p
    FROM Property p
    JOIN p.agent a
    WHERE a.email = :email
""")
List<Property> findByAgentEmail(
        @Param("email") String email
);
```

Notice that we don't manually write:

```
p.agent_id = a.id
```

JPQL already knows the relationship from the entity mapping.

The relationship itself defines how the join works.

### INNER JOIN

A normal:

```java
JOIN
```

is effectively an inner join.

Example:

```java
SELECT p
FROM Property p
JOIN p.agent a
WHERE a.active = true
```

This returns properties whose joined agent satisfies the condition.

If the relationship is absent, the row typically does not participate in the result.

### LEFT JOIN

Sometimes we want the parent even when no matching child/relationship exists.

Example:

```java
@Query("""
    SELECT p
    FROM Property p
    LEFT JOIN p.viewings v
""")
List<Property> findPropertiesIncludingThoseWithoutViewings();
```

Conceptually:

```text
Property with viewings
→ included

Property with no viewings
→ also included
```

This mirrors the relational `LEFT JOIN` concept you already know from SQL.

### JOIN Is Not JOIN FETCH

This distinction belongs partly to the next module, but we need to establish it here.

```java
JOIN p.agent
```

means:

> Use the relationship in this query.

It does **not necessarily mean**:

> Initialize `property.agent` in the returned entity.

That is what:

```java
JOIN FETCH p.agent
```

is for.

We'll study fetch joins properly in **Fetching & Performance**.

For this module:

```text
JOIN
→ query relationship

JOIN FETCH
→ query + explicitly fetch relationship
```

## Returning Entities vs Returning Data

Suppose an endpoint only needs:

```
Property title
Property city
Agent name
```

We could load complete `Property` and `Agent` entities and then map them.

But this may retrieve much more data than necessary.

Instead, JPQL can return a projection containing exactly the required fields.

This is called a **DTO projection**.

## DTO Projection

Suppose:

```java
public record PropertySummary(
        UUID id,
        String title,
        String city,
        String agentName
) {}
```

JPQL can directly construct it:

```java
@Query("""
    SELECT new com.brightmove.dto.PropertySummary(
        p.id,
        p.title,
        p.city,
        a.name
    )
    FROM Property p
    JOIN p.agent a
""")
List<PropertySummary> findPropertySummaries();
```

Now Hibernate returns:

```
PropertySummary
```

rather than complete managed `Property` entities.

This is useful for read-heavy endpoints that need a specific data shape.

## Entity Query vs DTO Projection

Compare:

**Entity query**

```java
List<Property> findByCity(String city);
```

Useful when:

- business logic needs the entity
- we intend to modify it
- we need domain behavior
- mapping later is appropriate

**DTO projection**

```java
List<PropertySummary> findPropertySummaries();
```

Useful when:

- read-only endpoint
- only specific fields needed
- no entity modification required

The engineering question is:

> Do I need an entity, or do I only need data?

Don't load full entities automatically when a compact read model is sufficient.

## Interface-Based Projections

Spring Data also supports projection interfaces.

For example:

```java
public interface PropertySummaryProjection {

    UUID getId();

    String getTitle();

    String getCity();
}
```

A repository query can return:

```java
List<PropertySummaryProjection>
```

Spring maps selected values into the projection.

Both class/record projections and interface projections are useful.

Records provide a clear concrete DTO and are often easy to understand.

## Aggregation

Sometimes the answer isn't a collection of entities.

Suppose business asks:

> How many properties are available?

JPQL:

```java
@Query("""
    SELECT COUNT(p)
    FROM Property p
    WHERE p.status = :status
""")
long countByStatus(
        @Param("status") PropertyStatus status
);
```

`COUNT` is an aggregate function.

Other familiar aggregates include:

```
COUNT
SUM
AVG
MIN
MAX
```

Exactly as in SQL.

---

### `COUNT`

Example:

```java
@Query("""
    SELECT COUNT(v)
    FROM Viewing v
    WHERE v.status = :status
""")
long countViewingsByStatus(
        @Param("status") ViewingStatus status
);
```

The query returns one scalar value:

```
27
```

not entities.

---

### `AVG`

Suppose we want the average property price:

```java
@Query("""
    SELECT AVG(p.price)
    FROM Property p
""")
BigDecimal findAveragePrice();
```

Exact aggregate return types should be checked carefully because JPA type rules vary by expression.

The main conceptual point is that aggregation converts many rows/entities into summarized information.

```
many Property records
        ↓
AVG
        ↓
one average
```

### `GROUP BY`

Now suppose we want:

> Number of properties in each city.

We don't want one total count.

We want a count per group.

Conceptually:

```
Edinburgh → 42
Glasgow   → 31
Falkirk   → 12
```

JPQL:

```java
@Query("""
    SELECT p.city, COUNT(p)
    FROM Property p
    GROUP BY p.city
""")
List<Object[]> countPropertiesByCity();
```

Each result contains something like:

```
["Edinburgh", 42]
```

However, `Object[]` isn't a particularly nice API.

A projection is cleaner.

### Aggregation Projection

Create:

```java
public record CityPropertyCount(
        String city,
        long propertyCount
) {}
```

Then:

```java
@Query("""
    SELECT new com.brightmove.dto.CityPropertyCount(
        p.city,
        COUNT(p)
    )
    FROM Property p
    GROUP BY p.city
""")
List<CityPropertyCount> countPropertiesByCity();
```

Now the result is strongly typed:

```
CityPropertyCount("Edinburgh", 42)
CityPropertyCount("Glasgow", 31)
```

This is much better than scattering `Object[]` index access through the service layer.

---

### Mental Model for `GROUP BY`

Without grouping:

```text
Property A → Edinburgh
Property B → Edinburgh
Property C → Glasgow
Property D → Edinburgh
```

`GROUP BY city` reorganizes conceptually into:

```text
Edinburgh
├── A
├── B
└── D

Glasgow
└── C
```

Then aggregate functions operate on each group:

```text
Edinburgh → COUNT = 3
Glasgow   → COUNT = 1
```

This is the same mental model as SQL.

---

### `WHERE` vs `HAVING`

This is an important distinction.

`WHERE` filters individual rows/entities **before grouping**.

`HAVING` filters groups **after aggregation**.

Suppose we want:

> Cities with more than 20 properties.

We cannot conceptually say:

```
WHERE COUNT(properties) > 20
```

because `COUNT` does not exist until the groups have been created.

Instead:

```java
@Query("""
    SELECT new com.brightmove.dto.CityPropertyCount(
        p.city,
        COUNT(p)
    )
    FROM Property p
    GROUP BY p.city
    HAVING COUNT(p) > :minimum
""")
List<CityPropertyCount> findCitiesWithMoreThan(
        @Param("minimum") long minimum
);
```

Flow:

```
Properties
    ↓
GROUP BY city
    ↓
COUNT each group
    ↓
HAVING count > minimum
```

---

### Combining `WHERE`, `GROUP BY`, and `HAVING`

Business asks:

> Show cities that have more than 10 AVAILABLE properties.

JPQL:

```java
@Query("""
    SELECT new com.brightmove.dto.CityPropertyCount(
        p.city,
        COUNT(p)
    )
    FROM Property p
    WHERE p.status = :status
    GROUP BY p.city
    HAVING COUNT(p) > :minimum
""")
List<CityPropertyCount> findBusyCities(
        @Param("status") PropertyStatus status,
        @Param("minimum") long minimum
);
```

Read in logical order:

```
FROM Property
    ↓
WHERE status = AVAILABLE
    ↓
GROUP BY city
    ↓
COUNT
    ↓
HAVING count > 10
    ↓
return projection
```

This mirrors the SQL reasoning you already learned in the Database classroom.

---

### Relationship Aggregation

Relationships can participate in aggregation too.

Suppose we want:

> Number of viewings for each property.

Projection:

```java
public record PropertyViewingCount(
        UUID propertyId,
        String title,
        long viewingCount
) {}
```

Query:

```java
@Query("""
    SELECT new com.brightmove.dto.PropertyViewingCount(
        p.id,
        p.title,
        COUNT(v)
    )
    FROM Property p
    LEFT JOIN p.viewings v
    GROUP BY p.id, p.title
""")
List<PropertyViewingCount> countViewingsPerProperty();
```

Why `LEFT JOIN`?

Because we may still want:

```
Property with zero viewings
```

in the result.

With a plain inner join, properties without any viewing may disappear entirely.

---

### Why Group by Both `id` and `title`?

We're selecting:

```java
p.id
p.title
COUNT(v)
```

The aggregate is:

```text
COUNT(v)
```

The non-aggregate selected fields are:

```text
p.id
p.title
```

They therefore belong in the grouping expression according to standard grouping rules.

Conceptually:

```text
Group by the property identity/data we're returning
then aggregate its children.
```

## Querying vs Filtering in Java

Suppose there are 100,000 properties.

Bad approach:

```java
List<Property> all = propertyRepository.findAll();

List<Property> edinburgh =
        all.stream()
           .filter(p -> p.getCity().equals("Edinburgh"))
           .toList();
```

This means:

```text
Database sends 100,000 rows
        ↓
Java discards most of them
```

Much better:

```java
propertyRepository.findByCity("Edinburgh");
```

because the filtering happens in the database.

General rule:

> If the database can efficiently filter the data you need, query for that data rather than loading everything and filtering in Java.

Streams are excellent for in-memory transformations.

They are not a replacement for database querying.

## Querying and Business Logic

Repositories answer data questions.

For example:

```java
boolean existsByPropertyIdAndRequestedDateTime(...);
```

answers:

> Does this row/state exist?

The service then decides:

```java
if (exists) {
    throw new BookingConflictException();
}
```

Repositories should generally not encode complete workflows such as:

```java
bookViewingIfAllowedAndNotifyAgent(...)
```

Again:

```text
Repository
→ retrieve/query data

Service
→ interpret data and make business decisions
```

## Query Naming

A repository method should communicate intent clearly.

For simple derived queries:

```java
findByAgentId(...)
findByStatus(...)
existsByReference(...)
```

are excellent.

For explicit JPQL, avoid vague names such as:

```java
query1()
customQuery()
fetchStuff()
```

Prefer:

```java
findPropertySummaries()
countPropertiesByCity()
findCitiesWithMoreThan(...)
```

The method name should tell the service what information it receives, not how SQL happens internally.

## Native SQL

If needed:

```java
@Query(
    value = """
        SELECT p.city, COUNT(*)
        FROM properties p
        GROUP BY p.city
    """,
    nativeQuery = true
)
List<Object[]> countByCityNative();
```

Native queries provide full SQL/database-specific control.

They are useful when:

- JPQL cannot express what you need easily
- database-specific features matter
- query tuning demands SQL-specific functionality

But they trade away some portability and object-model abstraction.

So don't use native SQL merely because SQL is familiar.

---

## Query Selection Framework

When designing a repository operation, use this sequence.

### Need standard CRUD?

```text
findById
findAll
save
delete
```

Use inherited `JpaRepository`.

### Need a simple fixed condition?

```text
findByCityAndStatus
```

Use derived query.

### Need relationship navigation?

```text
findByPropertyAgentId
```

Derived queries may still work well.

### Need a more expressive fixed query?

Use:

```java
@Query
```

with JPQL.

### Need only selected fields?

Use:

```text
DTO projection
```

### Need summarized data?

Use:

```text
aggregate + projection
```

### Need optional, dynamic filter combinations?

Wait for:

```text
Specifications
```

in Module 13.

This prevents one query mechanism from being forced onto every problem.

## Request & Data Flow

Suppose BrightMove exposes:

```text
GET /reports/property-count-by-city
```

Controller:

```java
@GetMapping("/reports/property-count-by-city")
public List<CityPropertyCount> getPropertyCounts() {
    return propertyService.getPropertyCountByCity();
}
```

Service:

```java
@Transactional(readOnly = true)
public List<CityPropertyCount> getPropertyCountByCity() {
    return propertyRepository.countPropertiesByCity();
}
```

Repository:

```java
@Query("""
    SELECT new com.brightmove.dto.CityPropertyCount(
        p.city,
        COUNT(p)
    )
    FROM Property p
    GROUP BY p.city
""")
List<CityPropertyCount> countPropertiesByCity();
```

Flow:

```text
HTTP Request
    ↓
Controller
    ↓
Service
    ↓
Repository JPQL
    ↓
Hibernate translates JPQL
    ↓
SQL
    ↓
PostgreSQL aggregates
    ↓
DTO projection
    ↓
Service
    ↓
Controller
    ↓
JSON
```

Notice that we're not loading full Property entities just to count them.

The query returns exactly the information needed.

## BrightMove Example

A repository may eventually look like:

```java
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {

    List<Property> findByCity(String city);

    List<Property> findByAgentId(UUID agentId);

    Optional<Property> findByReference(
            String reference
    );

    boolean existsByReference(
            String reference
    );

    @Query("""
        SELECT new com.brightmove.dto.PropertySummary(
            p.id,
            p.title,
            p.city,
            a.name
        )
        FROM Property p
        JOIN p.agent a
        WHERE p.status = :status
    """)
    List<PropertySummary> findSummariesByStatus(
            @Param("status") PropertyStatus status
    );

    @Query("""
        SELECT new com.brightmove.dto.CityPropertyCount(
            p.city,
            COUNT(p)
        )
        FROM Property p
        GROUP BY p.city
    """)
    List<CityPropertyCount> countPropertiesByCity();
}
```

Notice the progression:

```text
simple need
→ derived method

specific shaped result
→ JPQL projection

reporting/aggregate need
→ aggregate JPQL
```

---

**What should actually be written in a Repository?**

We add operations that our application specifically needs and which aren’t already supplied by JpaRepository.

```
JpaRepository
│
├── standard CRUD
│   └── inherited — don't rewrite
│
└── application-specific data operations
    │
    ├── derived queries
    ├── relationship queries
    ├── existence queries
    ├── JPQL
    ├── projections
    └── aggregations
```

## Common Mistakes

**Loading everything and filtering in Java**

Avoid:

```java
findAll().stream().filter(...)
```

for database-scale filtering when a query can do the work.

---

**Returning full entities for every read endpoint**

If only four fields are needed, consider a projection.

---

**Using `Object[]` everywhere**

This works:

```java
List<Object[]>
```

but it loses type clarity.

Prefer a DTO/record projection where practical.

---

**Writing JPQL using table names**

Wrong mental model:

```java
SELECT *
FROM properties
```

inside JPQL.

JPQL uses:

```java
SELECT p
FROM Property p
```

because it queries entities.

---

**Confusing `WHERE` and `HAVING`**

Remember:

```text
WHERE
→ filter rows/entities before grouping

HAVING
→ filter groups after aggregation
```

---

**Using derived query names that become unreadable**

Once the method name starts encoding an entire paragraph, move to a clearer query strategy.

---

**Using joins without understanding result semantics**

`INNER JOIN` can remove entities without matches.

`LEFT JOIN` can retain them.

Choose based on the information requirement.

## Engineering Trade-offs

Derived queries optimize developer productivity.

JPQL gives more explicit control.

Projections reduce unnecessary entity loading.

Aggregations push computation into the database, where large datasets can often be processed much more efficiently.

But more complicated repository queries also increase persistence-layer complexity.

A good repository API should expose meaningful data operations without becoming a second business-logic layer.

The goal isn't:

> Put every possible query in the repository.

It is:

> Provide the smallest clear persistence API required by the application's use cases.

## Summary

The querying toolbox is:

```text
JpaRepository CRUD
        ↓
Derived Queries
        ↓
Relationship Queries
        ↓
JPQL
        ↓
DTO Projections
        ↓
Aggregations
```

Key distinctions:

```text
Derived query
→ Spring interprets method name

JPQL
→ explicitly query entities and fields

JOIN
→ navigate/use entity relationships

Projection
→ return only needed data

COUNT / SUM / AVG / MIN / MAX
→ summarize data

GROUP BY
→ aggregate per group

HAVING
→ filter aggregated groups
```

Keep the data-flow principle:

> **Ask the database for the information the use case actually needs.**

And use this decision model:

```text
Need entities?
→ entity query

Need only selected fields?
→ DTO projection

Need summary/statistics?
→ aggregate query

Need many optional filters?
→ Specifications later
```
