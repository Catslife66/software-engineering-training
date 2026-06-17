# Dynamic Queries

This is very common in real property/search APIs.

Imagine BrightMove search supports:

```
city
status
minPrice
maxPrice
bedrooms
propertyType
```

A beginner might try:

```
findByCityAndStatusAndPriceBetweenAndBedroomsGreaterThanEqual(...)
```

That becomes ugly very quickly.

## Dynamic Filtering

Build the query dynamically:

```
Start with all properties
If city exists → add city condition
If status exists → add status condition
If minPrice exists → add minPrice condition
If maxPrice exists → add maxPrice condition
```

This is where Spring Data JPA often uses:

```
Specification
```

Conceptually, a Specification means:

```
A reusable query condition
```

Example:

```
cityEquals("London")
statusEquals("AVAILABLE")
priceBetween(300000, 800000)
```

Then combine them:

```
city condition
AND status condition
AND price condition
```

**Key idea**

```
Query derivation is good for fixed simple queries.
Specifications are better for flexible search filters.
```

Example:

```
public class PropertySearchRequestDTO {
    private String city;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer bedrooms;
    private String propertyType;
}
```

For `GET /properties/search`, filters often still arrive as query parameters, but Spring can bind them into a DTO-like object.

Then the flow becomes:

```
Controller receives search DTO
→ Service builds search conditions
→ Repository executes dynamic query
→ returns paginated results
```

## Specification

Think of dynamic filtering like building a `WHERE` clause step by step.

Example request:

```
PropertySearchRequestDTO request
```

contains:

```
city = London
status = AVAILABLE
minPrice = 300000
maxPrice = null
bedrooms = 3
```

The service should not add every condition. It should only add conditions that exist.

Conceptually:

```
Start with all properties

if city is present:
    add condition city = London

if status is present:
    add condition status = AVAILABLE

if minPrice is present:
    add condition price >= 300000

if maxPrice is present:
    skip, because null

if bedrooms is present:
    add condition bedrooms >= 3
```

Final query idea:

```
WHERE city = 'London'
AND status = 'AVAILABLE'
AND price >= 300000
AND bedrooms >= 3
```

In Spring Data JPA, each condition can become a small `Specification`.

Mental model:

```
Specification = one reusable filter condition
```

Examples:

```
cityEquals(city)
statusEquals(status)
priceGreaterThanOrEqual(minPrice)
priceLessThanOrEqual(maxPrice)
bedroomsGreaterThanOrEqual(bedrooms)
```

Then combine:

```
citySpec
AND statusSpec
AND minPriceSpec
AND bedroomsSpec
```

The flow becomes:

```
Controller
→ receives query parameters / DTO
→ passes request + pageable to service

Service
→ decides which filters exist
→ builds/composes Specifications
→ calls repository

Repository
→ executes database query
→ returns Page<Property>
```

## Why Specifications Are Powerful

Imagine next year the Product Manager adds:

```
minimumBathrooms
gardenRequired
parkingRequired
energyRating
```

With query derivation you'd need:

```
findByCityAndStatusAndPriceBetweenAndBedrooms...
```

which becomes impossible to maintain.

With Specifications:

```
Add new Specification
Combine with AND
Done

propertyRepository.findAll(specification, pageable);
```

## What Query Are We Building?

Suppose:

```
PropertySearchRequestDTO
```

contains:

```
city = null
status = AVAILABLE
minPrice = null
maxPrice = 800000
bedrooms = null
```

Conceptually:

```
WHERE status = 'AVAILABLE'
AND price <= 800000
```

Notice something important:

We are not building:

```
WHERE city = null
```

or

```
WHERE bedrooms >= null
```

Those conditions simply don't exist.

## This Is The Core Idea Of Specifications

Think:

```
Specification
    ↓
Optional Query Condition
```

Every filter can be:

```
Added
or
Skipped
```

independently.

## Compare To Query Derivation

Imagine trying to support:

```
city
status
minPrice
maxPrice
bedrooms
propertyType
```

using repository methods.

You'd quickly end up with things like:

```
findByCityAndStatus(...)

findByCityAndStatusAndPriceBetween(...)

findByStatusAndBedroomsGreaterThan(...)

findByCityAndPropertyTypeAndPriceBetween(...)
```

and hundreds of combinations.

That's not scalable.

## The Real Engineering Mindset

Think:

```
Search Request
      ↓
Generate Conditions
      ↓
Combine Conditions
      ↓
Execute Query
```

instead of:

```
One Repository Method
for every possible filter combination
```

## JpaSpecificationExecutor

For specifications to work, the repository usually extends:

```
JpaSpecificationExecutor<Property>
```

So instead of only:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {
}
```

we write:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID>,
                JpaSpecificationExecutor<Property> {
}
```

It gives us methods like:

```
findAll(Specification<Property> spec, Pageable pageable)
```

So the service can do:

```
Page<Property> result = propertyRepository.findAll(spec, pageable);
```

**Mental model**

```
JpaRepository = normal CRUD and simple queries

JpaSpecificationExecutor = dynamic search queries
```

**Flow**

```
Controller receives filters + pageable
→ Service builds Specification
→ Repository executes findAll(spec, pageable)
→ Hibernate generates SQL
→ PostgreSQL returns matching page
```

## Repository Toolkit So Far

You now know three major ways to query:

### 1. Query Derivation

```
findByEmail(...)
```

Good for:

```
Simple fixed queries
```

### 2. JPQL

```
@Query(...)
```

Good for:

```
Custom joins
DTO projections
Reports
Aggregations
```

### 3. Specifications

```
findAll(spec, pageable)
```

Good for:

```
Dynamic search screens
Optional filters
Complex search forms
```
