# Specifications & Dynamic Search

So far, our repository queries have usually had fixed conditions:

```
findByCity(...)
findByStatus(...)
findByCityAndStatus(...)
```

That works well when the query shape is known in advance.

But search APIs are different.

Imagine BrightMove supports:

```
city
minPrice
maxPrice
bedrooms
status
agentId
```

Each filter may be optional.

A client might request:

```
city = Edinburgh
status = AVAILABLE
```

Another might request:

```
minPrice = 200000
maxPrice = 400000
bedrooms = 3
```

Another might request only:

```
agentId = ...
```

If we create a repository method for every possible combination, the repository quickly becomes unmanageable.

This module covers:

- dynamic filtering
- `Specification<T>`
- `JpaSpecificationExecutor`
- composing Specifications
- optional filters
- search request DTOs
- combining Specifications with pagination and sorting
- relationship filtering
- where Specifications belong architecturally

## Mental Model

Think of each filter as a small independent condition.

For example:

```
city = Edinburgh
```

is one condition.

```
price >= 200000
```

is another.

```
status = AVAILABLE
```

is another.

Then combine only the conditions that were requested:

```
city = Edinburgh
AND
price >= 200000
AND
status = AVAILABLE
```

A `Specification` represents one of these query conditions.

Conceptually:

```
Specification<Property>
=
one reusable condition about Property
```

Then:

```
Specification A
AND
Specification B
AND
Specification C
```

creates a larger query.

## `JpaSpecificationExecutor`

A normal repository might currently be:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {
}
```

To support Specifications, add:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID>,
                JpaSpecificationExecutor<Property> {
}
```

Now the repository gains operations such as:

```
findAll(specification);
findAll(specification, pageable);
count(specification);
exists(specification);
```

So we now have two different capabilities:

```
JpaRepository
→ standard persistence operations

JpaSpecificationExecutor
→ execute dynamically constructed query conditions
```

## `Specification<T>`

A Specification is essentially a function describing a database predicate.

Example:

```
public static Specification<Property> hasCity(
        String city
) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(
                    root.get("city"),
                    city
            );
}
```

At first this syntax looks intimidating.

Let's break it down.

---

**The Three Parameters**

A Specification lambda receives:

```
(root, query, criteriaBuilder)
```

`root`

root represents the entity being queried.

For:

```
Specification<Property>
```

the root is:

```
Property
```

So:

```
root.get("city")
```

means conceptually:

```
Property.city
```

---

`query`

This represents the overall criteria query.

For many simple Specifications we don't use it directly.

It becomes useful for more advanced operations.

---

`criteriaBuilder`

This is the tool used to construct conditions.

Examples:

```
criteriaBuilder.equal(...)
criteriaBuilder.greaterThan(...)
criteriaBuilder.lessThan(...)
criteriaBuilder.like(...)
```

Conceptually:

```
criteriaBuilder
→ "Build me a WHERE condition."
```

---

Example

**City Specification**

```
public static Specification<Property> hasCity(
        String city
) {
    return (root, query, cb) ->
            cb.equal(
                    root.get("city"),
                    city
            );
}
```

Mentally translate:

```
Property.city = city
```

Conceptual SQL:

```
WHERE city = ?
```

The important thing is not the exact syntax.

The important idea is:

```
hasCity() creates a reusable query condition.
```

**Status Specification**

```
public static Specification<Property> hasStatus(
        PropertyStatus status
) {
    return (root, query, cb) ->
            cb.equal(
                    root.get("status"),
                    status
            );
}
```

Conceptually:

```
WHERE status = ?
```

**Minimum Price**

```
public static Specification<Property> priceAtLeast(
        BigDecimal minPrice
) {
    return (root, query, cb) ->
            cb.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice
            );
}
```

Conceptually:

```
WHERE price >= ?
```

**Maximum Price**

```
public static Specification<Property> priceAtMost(
        BigDecimal maxPrice
) {
    return (root, query, cb) ->
            cb.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice
            );
}
```

Conceptually:

```
WHERE price <= ?
```

**Bedrooms**

```
public static Specification<Property> hasBedrooms(
        Integer bedrooms
) {
    return (root, query, cb) ->
            cb.equal(
                    root.get("bedrooms"),
                    bedrooms
            );
}
```

Again:

```
One method
→ one query condition
```

This keeps each Specification small and reusable.

---

**Combining Specifications**

Now we can combine them.

Suppose:

```
Specification<Property> spec =
        Specification
                .where(hasCity("Edinburgh"))
                .and(hasStatus(PropertyStatus.AVAILABLE))
                .and(priceAtLeast(
                        new BigDecimal("200000")
                ));
```

Conceptually:

```
WHERE city = 'Edinburgh'
AND status = 'AVAILABLE'
AND price >= 200000
```

This is the core power of Specifications.

## Specification Handles Null

Suppose:

```
String city = request.city();
PropertyStatus status = request.status();
BigDecimal minPrice = request.minPrice();
```

Any of these may be null.

We could build the Specification conditionally.

Example:

```
Specification<Property> spec =
        Specification.where(null);

if (request.city() != null) {
    spec = spec.and(
            PropertySpecifications.hasCity(
                    request.city()
            )
    );
}

if (request.status() != null) {
    spec = spec.and(
            PropertySpecifications.hasStatus(
                    request.status()
            )
    );
}

if (request.minPrice() != null) {
    spec = spec.and(
            PropertySpecifications.priceAtLeast(
                    request.minPrice()
            )
    );
}
```

Only supplied filters become part of the query.

We can also design each Specification to ignore missing values.

Example:

```
public static Specification<Property> hasCity(
        String city
) {
    return (root, query, cb) -> {

        if (city == null || city.isBlank()) {
            return cb.conjunction();
        }

        return cb.equal(
                root.get("city"),
                city
        );
    };
}
```

cb.conjunction() means conceptually:

```
always true
```

So it contributes no effective filtering.

Then we can write:

```
Specification<Property> spec =
        Specification
                .where(
                    PropertySpecifications.hasCity(
                        request.city()
                    )
                )
                .and(
                    PropertySpecifications.hasStatus(
                        request.status()
                    )
                )
                .and(
                    PropertySpecifications.priceAtLeast(
                        request.minPrice()
                    )
                );
```

This can make the service cleaner.

---

**Which Null-Handling Style Is Better?**

Both are valid.

**Service decides whether to add Specification**

Advantages:

- explicit
- Specification methods stay simple
- easy to see which filters are active

**Specification ignores null itself**

Advantages:

- composition code is cleaner
- convenient for many optional filters

The main goal is consistency.

Don't mix several patterns randomly across the application.

## Search Request DTO

Instead of adding many controller parameters:

```
@RequestParam String city
@RequestParam BigDecimal minPrice
@RequestParam BigDecimal maxPrice
@RequestParam Integer bedrooms
@RequestParam PropertyStatus status
```

we can define a search DTO:

```
public record PropertySearchRequest(
        String city,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer bedrooms,
        PropertyStatus status,
        UUID agentId
) {}
```

Conceptually:

```
HTTP query parameters
        ↓
PropertySearchRequest
        ↓
Specifications
        ↓
database query
```

This gives the search API a clear input model.

## Validation on Search DTO

Search filters can still use validation.

For example:

```
public record PropertySearchRequest(

        String city,

        @PositiveOrZero
        BigDecimal minPrice,

        @Positive
        BigDecimal maxPrice,

        @Positive
        Integer bedrooms,

        PropertyStatus status,

        UUID agentId
) {}
```

But there's also cross-field validation:

```
minPrice <= maxPrice
```

which may require a class-level validator or explicit application validation.

Again:

```
Input structure
→ DTO validation

Database-dependent/business state
→ service
```

## Relationship Specifications

Specifications can navigate relationships too.

Suppose:

```
Property
  ↓
Agent
  ↓
id
```

We want:

```
Properties owned by one agent.
```

We can join:

```
public static Specification<Property> ownedByAgent(
        UUID agentId
) {
    return (root, query, cb) -> {

        if (agentId == null) {
            return cb.conjunction();
        }

        Join<Property, Agent> agent =
                root.join("agent");

        return cb.equal(
                agent.get("id"),
                agentId
        );
    };
}
```

Conceptually:

```
Property
JOIN Agent
WHERE agent.id = ?
```

So Specifications are not limited to basic fields.

---

**Simpler Relationship Path**

For some simple to-one relationships, you may also be able to navigate:

```
root.get("agent").get("id")
```

Conceptually:

```
Property.agent.id
```

But explicit `join()` becomes useful when relationship query behaviour needs to be clearer or more complex.

## City Contains / Case-Insensitive Search

Suppose user enters:

```
edin
```

and we want:

```
Edinburgh
```

Specification:

```
public static Specification<Property> cityContains(
        String city
) {
    return (root, query, cb) -> {

        if (city == null || city.isBlank()) {
            return cb.conjunction();
        }

        return cb.like(
                cb.lower(root.get("city")),
                "%" + city.toLowerCase() + "%"
        );
    };
}
```

Conceptually:

```
WHERE LOWER(city) LIKE '%edin%'
```

This is a common dynamic-search pattern.

## `and()` vs `or()`

Specifications can compose with:

```
.and(...)
```

and:

```
.or(...)
```

Example:

```
Specification<Property> spec =
        hasCity("Edinburgh")
            .or(hasCity("Glasgow"));
```

Conceptually:

```
WHERE city = 'Edinburgh'
OR city = 'Glasgow'
```

You can also combine groups:

```
(city = Edinburgh OR city = Glasgow)
AND status = AVAILABLE
```

Specifications are effectively a composable boolean query tree.

## Specification Tree

Imagine:

```
AND
├── city = Edinburgh
├── status = AVAILABLE
└── price >= 200000
```

Each leaf is one Specification.

Then Spring/Hibernate converts the tree into a SQL `WHERE` clause.

This is a very useful way to think about dynamic queries.

## Repository Execution

Once the Specification is built:

```
Page<Property> page =
        propertyRepository.findAll(
                spec,
                pageable
        );
```

Notice how nicely this combines our last three modules:

```
Specification
→ WHAT matches

Pageable
→ WHICH window + ordering

Repository
→ execute

Hibernate
→ translate

PostgreSQL
→ filter/sort/page
```

## Service Example

```
@Transactional(readOnly = true)
public Page<PropertyResponse> searchProperties(
        PropertySearchRequest request,
        Pageable pageable
) {

    Specification<Property> spec =
            Specification
                .where(
                    PropertySpecifications.hasCity(
                        request.city()
                    )
                )
                .and(
                    PropertySpecifications.hasStatus(
                        request.status()
                    )
                )
                .and(
                    PropertySpecifications.priceAtLeast(
                        request.minPrice()
                    )
                )
                .and(
                    PropertySpecifications.priceAtMost(
                        request.maxPrice()
                    )
                )
                .and(
                    PropertySpecifications.hasBedrooms(
                        request.bedrooms()
                    )
                )
                .and(
                    PropertySpecifications.ownedByAgent(
                        request.agentId()
                    )
                );

    return propertyRepository
            .findAll(spec, pageable)
            .map(propertyMapper::toResponse);
}
```

This is the central dynamic-search pattern.

## Why This Is Better Than Many Repository Methods

Without Specifications:

```
findByCity()
findByCityAndStatus()
findByCityAndStatusAndBedrooms()
findByStatusAndPriceBetween()
findByCityAndPriceBetween()
...
```

With Specifications:

```
hasCity()
hasStatus()
hasBedrooms()
priceAtLeast()
priceAtMost()
ownedByAgent()
```

Then compose them as needed.

So we change:

```
query-combination explosion
```

into:

```
small reusable conditions
+
composition
```

## Where Should Specifications Live?

A common structure:

```
property/
├── Property.java
├── PropertyRepository.java
├── PropertyService.java
└── specification/
    └── PropertySpecifications.java
```

For example:

```
public final class PropertySpecifications {

    private PropertySpecifications() {
    }

    public static Specification<Property> hasCity(...) {
        ...
    }

    public static Specification<Property> hasStatus(...) {
        ...
    }
}
```

These are persistence/query-building concerns.

They should not contain business workflow decisions.

## Specification Is Not Business Logic

For example:

```
hasStatus(PropertyStatus.AVAILABLE)
```

means:

```
Query rows/entities matching this condition.
```

It does not decide:

```
Is the current user allowed to buy this property?
```

That's business/authorization logic.

Again:

```
Specification
→ describe query condition

Service
→ interpret results and enforce business rules
```

## Specifications and DTO Projections

There is an important practical consideration.

`JpaSpecificationExecutor<Property>` naturally works with entity queries:

```
Page<Property>
```

Then:

```
.map(propertyMapper::toResponse)
```

produces DTOs.

But for highly optimized read APIs, we may want:

```
dynamic filters
+
direct DTO projection
```

This requires more advanced repository/query techniques than the simplest JpaSpecificationExecutor pattern.

For BrightMove, a good starting architecture is:

```
Specification
→ filter Property entities dynamically

Page
→ limit results

Mapper
→ DTO
```

Then optimize specific read endpoints later if measurement shows a need.

## Specifications and N+1

Specifications solve:

```
dynamic WHERE conditions
```

They do not automatically solve fetching problems.

For example:

```
Page<Property> properties =
        propertyRepository.findAll(spec, pageable);
```

Then:

```
property.getAgent().getName();
```

may still trigger lazy loading.

So:

```
Specification
→ filtering problem

JOIN FETCH / EntityGraph
→ fetching problem
```

Keep those concerns separate.

This connects directly to 3.11.

## Dynamic Search + Pagination + Fetching

A realistic search flow is:

```
Search DTO
    ↓
Specifications
    ↓
Filtering
    ↓
Pageable
    ↓
Pagination + sorting
    ↓
Repository
    ↓
Entities
    ↓
Fetch strategy
    ↓
Mapper
    ↓
Response DTO
```

All three concerns must work together:

```
Filtering
Pagination
Fetching
```

but they solve different problems.

## Controller Example

A controller might be:

```
@GetMapping
public PageResponse<PropertyResponse> searchProperties(
        @Valid PropertySearchRequest request,
        Pageable pageable
) {

    Page<PropertyResponse> page =
            propertyService.searchProperties(
                    request,
                    pageable
            );

    return PageResponse.from(page);
}
```

Conceptually:

```
GET /properties
?city=Edinburgh
&minPrice=200000
&bedrooms=3
&page=0
&size=20
```

Spring binds:

```
search parameters
→ PropertySearchRequest

pagination parameters
→ Pageable
```

Then the service combines them.

## Search API Design

One practical issue is distinguishing:

filter parameters

from:

pagination parameters

Example:

```
GET /properties
    ?city=Edinburgh
    &minPrice=200000
    &status=AVAILABLE
    &page=0
    &size=20
    &sort=price,asc
```

Conceptually:

```
PropertySearchRequest
├── city
├── minPrice
└── status

Pageable
├── page
├── size
└── sort
```

That separation is clean because search criteria and pagination are different responsibilities.

## Null vs Empty Search Values

Be careful with:

```
city=
```

Should that mean:

```
city is blank
```

or:

```
no city filter
```

Usually search APIs normalize empty input into:

```
filter absent
```

For example:

```
if (city == null || city.isBlank()) {
    return cb.conjunction();
}
```

This prevents accidental conditions like:

```
WHERE city = ''
```

unless that is genuinely intended.

## Range Filters

Price search commonly uses:

```
minPrice
maxPrice
```

There are four scenarios:

```
both null
→ no price filter

only min
→ price >= min

only max
→ price <= max

both
→ price BETWEEN min AND max
```

Specifications naturally compose these cases.

This is one reason they work well for search APIs.

## Don't Build One Giant Specification Method

Avoid:

```
public static Specification<Property> search(
        String city,
        BigDecimal min,
        BigDecimal max,
        Integer bedrooms,
        PropertyStatus status,
        UUID agentId,
        ...
) {
    // enormous method
}
```

That recreates the same complexity in one place.

Prefer small pieces:

```
hasCity(...)
hasStatus(...)
priceAtLeast(...)
priceAtMost(...)
hasBedrooms(...)
ownedByAgent(...)
```

Then compose them.

This preserves reuse and readability.

## Type Safety Concern

You may have noticed:

```
root.get("city")
```

uses a string.

That means this typo:

```
root.get("ctiy")
```

may not be caught as nicely at compile time.

JPA also supports a static metamodel approach:

```
root.get(Property_.city)
```

which improves type safety.

However, it adds tooling/complexity.

For our current level, string field names are easier to learn and are very common.

The engineering lesson is simply:

> Specification APIs are powerful, but some parts are less type-safe than ordinary Java property access.

## Criteria API Underneath Specifications

Specifications are built on JPA's Criteria API.

Without Spring Data Specifications, Criteria queries can be verbose.

Spring's `Specification<T>` gives us a much cleaner reusable abstraction over that machinery.

So conceptually:

```
JPA Criteria API
        ↓
Specification<T>
        ↓
Spring Data composition
```

This is another example of Spring Data making JPA easier to use.

Just like:

```
EntityManager
        ↓
JpaRepository
```

Spring Data adds a convenient abstraction.

## BrightMove Specification Class

A simplified version:

```
public final class PropertySpecifications {

    private PropertySpecifications() {
    }

    public static Specification<Property> hasCity(
            String city
    ) {
        return (root, query, cb) -> {

            if (city == null || city.isBlank()) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("city"),
                    city
            );
        };
    }

    public static Specification<Property> hasStatus(
            PropertyStatus status
    ) {
        return (root, query, cb) -> {

            if (status == null) {
                return cb.conjunction();
            }

            return cb.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<Property> priceAtLeast(
            BigDecimal minPrice
    ) {
        return (root, query, cb) -> {

            if (minPrice == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice
            );
        };
    }

    public static Specification<Property> priceAtMost(
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {

            if (maxPrice == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice
            );
        };
    }
}
```

Notice how each method answers exactly one query question.

## Request & Data Flow

Request:

```
GET /properties
?city=Edinburgh
&minPrice=200000
&status=AVAILABLE
&page=0
&size=20
```

Flow:

```
HTTP Request
    ↓
PropertySearchRequest
    ↓
Pageable
    ↓
Service
    ↓
build Specification
    ↓
city condition
AND minPrice condition
AND status condition
    ↓
propertyRepository.findAll(spec, pageable)
    ↓
Hibernate Criteria/JPQL machinery
    ↓
SQL WHERE + ORDER + pagination
    ↓
PostgreSQL
    ↓
Page<Property>
    ↓
Mapper
    ↓
Page<PropertyResponse>
    ↓
PageResponse
    ↓
JSON
```

This is the complete search pipeline.

## Search Query Conceptual SQL

Suppose:

```
city = Edinburgh
minPrice = 200000
maxPrice = null
status = AVAILABLE
```

Specifications may produce SQL conceptually like:

```
SELECT ...
FROM properties
WHERE city = 'Edinburgh'
  AND price >= 200000
  AND status = 'AVAILABLE'
ORDER BY price ASC
LIMIT 20 OFFSET 0;
```

Notice:

```
maxPrice absent
```

therefore:

```
no max-price condition
```

That's the defining feature of dynamic search.

## Common Mistakes

**Creating a repository method for every filter combination**

This leads to combinatorial explosion.

---

**Loading all entities and filtering with Streams**

Avoid:

```
findAll()
    .stream()
    .filter(...)
```

for database-scale search.

Push search conditions into the database.

---

**Mixing business rules into Specifications**

Specifications describe data conditions, not application permissions/workflows.

---

**Assuming Specifications solve N+1**

They solve dynamic filtering, not fetching.

---

**Ignoring pagination**

Dynamic search can still return huge datasets.

Combine with `Pageable`.

---

**One giant Specification method**

Prefer composable small conditions.

---

**Treating missing filter as a real value**

Normalize optional search input carefully.

## Engineering Trade-offs

Specifications give us:

- flexible dynamic queries
- reusable conditions
- clean composition
- smaller repositories
- good integration with pagination

But they also introduce:

- Criteria API syntax
- string-based field references in common usage
- more abstraction than derived queries
- potential complexity for advanced joins/projections

So don't use Specifications for every repository query.

For:

```
findByReference(...)
```

a derived query is simpler.

For:

```
10 optional search filters
```

Specifications are much better.

Use the simplest tool that fits the query.

## Query Tool Selection After This Module

We can now complete our repository decision model.

```
Standard CRUD
→ JpaRepository

Simple fixed condition
→ derived query

Complex fixed query
→ JPQL @Query

Specific read shape
→ DTO projection

Dynamic optional filters
→ Specification

Large result set
→ Pageable/Page/Slice

Relationship data needed
→ deliberate fetch strategy
```

These are complementary tools.

They are not competitors.

## The Full Search Architecture

A clean BrightMove search design is:

```
Controller
    ↓
PropertySearchRequest
+
Pagination input
    ↓
Service
    ↓
Specifications
    ↓
PropertyRepository
    ↓
JpaSpecificationExecutor
    ↓
Hibernate
    ↓
PostgreSQL
    ↓
Page<Property>
    ↓
Mapper
    ↓
PageResponse<PropertyResponse>
```

Each component has one job:

```
Search DTO
→ describe requested filters

Specification
→ convert filters into query predicates

Pageable
→ page and sorting

Repository
→ execute persistence query

Mapper
→ API response shape
```

## Summary

The core problem is:

```
many optional filters
→ too many possible derived query combinations
```

Specifications solve this by turning each filter into a reusable predicate:

```
Specification<Property>
```

Repository:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID>,
                JpaSpecificationExecutor<Property> {
}
```

Common Specifications:

```
hasCity()
hasStatus()
priceAtLeast()
priceAtMost()
hasBedrooms()
ownedByAgent()
```

Then compose:

```
spec
    .and(...)
    .and(...)
    .and(...)
```

The mental model is:

```
Search Request
      ↓
Optional filters
      ↓
Reusable Specifications
      ↓
Boolean predicate tree
      ↓
WHERE clause
```

And combine it with the previous modules:

```
Specification
→ WHAT matches

Fetching strategy
→ WHAT related data loads

Pageable
→ HOW MUCH + ORDER

Projection / Mapper
→ WHAT shape returns
```

The most important engineering principle from this module is:

> Dynamic search should be built from small composable query conditions rather than from an explosion of repository methods for every possible filter combination.

---

1. `record` with from()

Entity:

```
@Entity
public class Property {

    private UUID id;
    private String title;
    private String city;
    private BigDecimal price;

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
```

DTO:

```
public record PropertyResponse(
        UUID id,
        String title,
        String city,
        BigDecimal price
) {

    public static PropertyResponse from(
            Property property
    ) {
        return new PropertyResponse(
                property.getId(),
                property.getTitle(),
                property.getCity(),
                property.getPrice()
        );
    }
}
```

Service:

```
public PropertyResponse getProperty(UUID id) {

    Property property =
            propertyRepository.findById(id)
                    .orElseThrow(
                        () -> new PropertyNotFoundException(id)
                    );

    return PropertyResponse.from(property);
}
```

2. Dedicated Mapper

DTO:

```
public record PropertyResponse(
        UUID id,
        String title,
        String city,
        BigDecimal price
) {}
```

Mapper:

```
@Component
public class PropertyMapper {

    public PropertyResponse toResponse(
            Property property
    ) {
        return new PropertyResponse(
                property.getId(),
                property.getTitle(),
                property.getCity(),
                property.getPrice()
        );
    }
}
```

Service:

```
public PropertyResponse getProperty(UUID id) {

    Property property =
            propertyRepository.findById(id)
                    .orElseThrow(
                        () -> new PropertyNotFoundException(id)
                    );

    return propertyMapper.toResponse(property);
}
```
