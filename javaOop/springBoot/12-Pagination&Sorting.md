# Pagination & Sorting

Until now, many repository examples returned complete lists:

```
List<Property> properties = propertyRepository.findAll();
```

That is fine for small datasets.

But imagine BrightMove grows to:

```
10,000 properties
100,000 properties
1,000,000 properties
```

Returning everything in one request becomes inefficient.

We would be:

```
Database
   ↓
load huge result set
   ↓
Hibernate creates huge number of objects
   ↓
application memory
   ↓
serialize huge JSON response
   ↓
network
   ↓
client
```

Instead, APIs normally return results in pages.

For example:

```
Page 0
→ Properties 1–20

Page 1
→ Properties 21–40

Page 2
→ Properties 41–60
```

This module covers:

- Pageable
- Page
- Slice
- Sort
- repository pagination
- pagination metadata
- API design
- sorting
- pagination + filtering
- performance implications
- pagination with relationships

## Mental Model

Pagination answers:

> Which small window of a larger result set should I retrieve?

Suppose there are:

```
1,000 properties
```

and our page size is:

```
20
```

Instead of:

```
load all 1,000
```

we ask:

```
Give me 20,
starting from a particular position.
```

Conceptually:

```
All data

[1 ... 20]      → page 0
[21 ... 40]     → page 1
[41 ... 60]     → page 2
...
```

Pagination therefore has two primary pieces of information:

```
page number
+
page size
```

Spring represents this request using:

```
Pageable
```

## `Pageable`

`Pageable` describes which page we want and how it should be sorted.

Example:

```
Pageable pageable =
        PageRequest.of(
                0,
                20
        );
```

This means:

```
page = 0
size = 20
```

Spring uses zero-based page numbering.

So:

```
page 0
→ first page

page 1
→ second page

page 2
→ third page
```

This can initially be slightly surprising if a frontend displays:

```
Page 1
Page 2
Page 3
```

The external API and internal representation can be adjusted if required, but Spring's default page index starts at zero.

---

**`JpaRepository` Already Supports Pagination**

Remember:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {
}
```

JpaRepository ultimately inherits pagination support.

So we can call:

```
Page<Property> properties =
        propertyRepository.findAll(pageable);
```

We don't need to create:

```
findAllPaginated(...)
```

ourselves for standard pagination.

## `Page<T>`

Notice the return type:

```
Page<Property>
```

not:

```
List<Property>
```

A Page contains both:

```
the actual results
+
pagination metadata
```

For example:

```
Page<Property> page =
        propertyRepository.findAll(pageable);
```

can tell us:

```
page.getContent();
page.getNumber();
page.getSize();
page.getTotalElements();
page.getTotalPages();
page.hasNext();
page.hasPrevious();
```

So:

```
Page<Property>
│
├── content
│     ├── Property A
│     ├── Property B
│     └── ...
│
├── pageNumber
├── pageSize
├── totalElements
├── totalPages
├── hasNext
└── hasPrevious
```

---

**Example**

Suppose BrightMove contains:

```
95 Properties
```

Request:

```
PageRequest.of(0, 20)
```

Then:

```
content
→ first 20 properties

page number
→ 0

page size
→ 20

total elements
→ 95

total pages
→ 5

hasNext
→ true

hasPrevious
→ false
```

---

**Conceptual SQL**

Traditional SQL pagination often uses concepts like:

```
LIMIT 20
OFFSET 0
```

for the first page.

Second page:

```
LIMIT 20
OFFSET 20
```

Third:

```
LIMIT 20
OFFSET 40
```

Spring/Hibernate generates database-appropriate pagination SQL for us.

Conceptually:

```
PageRequest.of(page, size)

        ↓

Hibernate / database dialect

        ↓

LIMIT / OFFSET-style SQL
```

Exact SQL differs between databases.

## Repository Query + Pageable

Pagination also works with derived queries.

Suppose:

```
Page<Property> findByCity(
        String city,
        Pageable pageable
);
```

Then:

```
Pageable pageable =
        PageRequest.of(0, 20);

Page<Property> properties =
        propertyRepository.findByCity(
                "Edinburgh",
                pageable
        );
```

Conceptually:

```
WHERE city = Edinburgh
+
page 0
+
20 rows
```

The `Pageable` parameter does not describe business filtering.

It describes:

```
window
+
ordering
```

## Sorting

Results need deterministic ordering.

Without an explicit order, SQL **does not guarantee** a stable result order.

So pagination should normally have a defined sort.

For example:

```
Sort sort = Sort.by("price");

Pageable pageable =
        PageRequest.of(
                0,
                20,
                sort
        );
```

Conceptually:

```
20 Properties
ordered by price
```

---

**Ascending and Descending**

Ascending:

```
Sort.by(
    Sort.Direction.ASC,
    "price"
);
```

Descending:

```
Sort.by(
    Sort.Direction.DESC,
    "price"
);
```

Example:

```
Pageable pageable =
        PageRequest.of(
                0,
                20,
                Sort.by(
                    Sort.Direction.DESC,
                    "createdAt"
                )
        );
```

---

**Multiple Sort Fields**

Suppose properties can have identical prices.

If we sort only:

```
price
```

several rows may tie.

For more deterministic ordering:

```
Sort sort = Sort.by(
        Sort.Order.asc("price"),
        Sort.Order.asc("id")
);
```

Conceptually:

```
ORDER BY price ASC, id ASC
```

The second field provides a stable tie-breaker.

This matters particularly in pagination because unstable ordering can cause records to move unexpectedly between pages.

## Controller Pagination

A controller could accept:

```
GET /properties?page=0&size=20
```

Example:

```
@GetMapping
public Page<PropertyResponse> getProperties(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) {

    Pageable pageable =
            PageRequest.of(page, size);

    return propertyService.getProperties(pageable);
}
```

Service:

```
@Transactional(readOnly = true)
public Page<PropertyResponse> getProperties(
        Pageable pageable
) {

    return propertyRepository.findAll(pageable)
            .map(propertyMapper::toResponse);
}
```

Notice something interesting:

```
Page.map(...)
```

lets us transform:

```
Page<Property>
```

into:

```
Page<PropertyResponse>
```

while preserving pagination metadata.

---

`Page.map()`

Suppose:

```
Page<Property>

content:
Property A
Property B
Property C

metadata:
page = 0
size = 3
total = 100
```

Then:

```
page.map(propertyMapper::toResponse)
```

produces:

```
Page<PropertyResponse>

content:
Response A
Response B
Response C

metadata:
page = 0
size = 3
total = 100
```

The metadata remains.

Only the content is transformed.

This is much cleaner than manually rebuilding the page.

## Let Spring Bind Pageable

Spring MVC can also construct Pageable directly from request parameters.

Example:

```
@GetMapping
public Page<PropertyResponse> getProperties(
        Pageable pageable
) {
    return propertyService.getProperties(pageable);
}
```

A request such as:

```
GET /properties?page=0&size=20&sort=price,asc
```

can be converted into a Pageable.

Conceptually:

```
?page=0
&size=20
&sort=price,asc

        ↓

Pageable
```

This is convenient.

However, in public APIs you may want tighter control over:

- maximum page size
- allowed sort fields
- default sorting

rather than exposing arbitrary entity properties directly.

---

**Returning Page Directly From the API**

We can technically return:

```
Page<PropertyResponse>
```

directly.

Spring/Jackson will serialize pagination information.

However, this exposes Spring's pagination representation as part of our API contract.

For a small/internal application, that can be acceptable.

For a carefully designed public API, we may prefer our own response DTO.

Example:

```
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {}
```

Then:

```
Spring Page
    ↓
our PageResponse
    ↓
stable API contract
```

This avoids coupling clients directly to Spring Data's JSON structure.

---

**Example Page Response**

API might return:

```
{
  "content": [
    {
      "id": "...",
      "title": "Flat A"
    },
    {
      "id": "...",
      "title": "Flat B"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 95,
  "totalPages": 5,
  "hasNext": true
}
```

This gives the frontend enough information to build navigation such as:

```
Previous
1 2 3 4 5
Next
```

## Why Does Page Need an Extra Count Query?

This is an important performance concept.

Suppose we ask:

```
Page<Property> page =
        propertyRepository.findByCity(
                "Edinburgh",
                pageable
        );
```

To return:

```
page.getTotalElements();
page.getTotalPages();
```

Spring needs to know:

> How many total matching records exist?

The page query only returns 20 rows.

So conceptually Spring may execute:

Query 1 — content

```
SELECT ...
FROM properties
WHERE city = 'Edinburgh'
LIMIT 20 OFFSET 0;
```

Query 2 — count

```
SELECT COUNT(*)
FROM properties
WHERE city = 'Edinburgh';
```

Therefore:

```
Page often means

content query
+
count query
```

This is important.

Page gives rich metadata, but that metadata has a cost.

## `Slice<T>`

Sometimes the client doesn't need:

```
total elements
total pages
```

Maybe it only needs:

Give me the next 20 results and tell me whether another page exists.

Then we can use:

```
Slice<Property>
```

Example repository:

```
Slice<Property> findByCity(
        String city,
        Pageable pageable
);
```

A Slice knows things such as:

```
slice.getContent();
slice.hasNext();
slice.hasPrevious();
slice.getNumber();
```

But it doesn't need to provide:

```
getTotalElements();
getTotalPages();
```

Therefore a separate full count query can often be avoided.

## `Page` vs `Slice`

`Page`

Use when the UI needs:

```
Page 1 of 47

952 total results
```

It requires total-count information.

`Slice`

Use when the UI needs:

```
show 20

"Load More"

has next?
```

No total count required.

Mental model:

```
Page
→ "Where am I in the entire result set?"

Slice
→ "Give me this chunk and tell me if more exists."
```

---

**Example: Traditional Pagination vs Infinite Scroll**

A property search website might show:

```
Page 1 of 85
```

Then:

```
Page<PropertySummary>
```

makes sense.

A mobile app might use:

```
Load More
```

or infinite scrolling.

Then:

```
Slice<PropertySummary>
```

may be sufficient and cheaper.

The API requirement should determine the abstraction.

## `List` vs `Slice` vs `Page`

Think of them as increasing amounts of pagination information.

```
List<T>
│
└── results only


Slice<T>
│
├── results
└── is there another slice?


Page<T>
│
├── results
├── next/previous
├── total elements
└── total pages
```

So don't automatically use Page merely because it contains more information.

More information may require more database work.

## Pagination With Projections

Pagination works particularly well with projections.

Suppose property search only needs:

```
id
title
city
price
```

Instead of:

```
Page<Property>
```

followed by lazy relationship navigation, we can directly query a DTO.

Example:

```
@Query("""
    SELECT new com.brightmove.dto.PropertySummary(
        p.id,
        p.title,
        p.city,
        p.price
    )
    FROM Property p
""")
Page<PropertySummary> findPropertySummaries(
        Pageable pageable
);
```

Now:

```
database
    ↓
only required columns
    ↓
only requested page
    ↓
DTO
```

This can be an excellent read-API design.

## Pagination + Derived Query

We might have:

```
Page<Property> findByStatus(
        PropertyStatus status,
        Pageable pageable
);
```

Then:

```
Pageable pageable =
        PageRequest.of(
                0,
                20,
                Sort.by(
                    Sort.Direction.DESC,
                    "createdAt"
                )
        );

Page<Property> properties =
        propertyRepository.findByStatus(
                PropertyStatus.AVAILABLE,
                pageable
        );
```

Conceptually:

```
AVAILABLE Properties
+
newest first
+
first 20
```

## Pagination + JPQL

Custom JPQL can also take Pageable.

Example:

```
@Query("""
    SELECT p
    FROM Property p
    WHERE p.city = :city
      AND p.status = :status
""")
Page<Property> search(
        String city,
        PropertyStatus status,
        Pageable pageable
);
```

Spring applies pagination to the query.

For a Page, it may also derive or require a count query.

## Complex JPQL and Count Queries

Sometimes Spring can automatically derive the count query.

But with complex queries involving:

```
joins
grouping
projections
fetch joins
```

the count query may need to be explicitly supplied.

For example:

```
@Query(
    value = """
        SELECT p
        FROM Property p
        JOIN p.agent a
        WHERE a.active = true
    """,
    countQuery = """
        SELECT COUNT(p)
        FROM Property p
        JOIN p.agent a
        WHERE a.active = true
    """
)
Page<Property> findActiveAgentProperties(
        Pageable pageable
);
```

This separates:

```
content query
```

from:

```
count query
```

The need becomes more common as queries become sophisticated.

## Pagination and Relationships

This connects directly to Module 11.

Suppose:

```
@Query("""
    SELECT p
    FROM Property p
    LEFT JOIN FETCH p.viewings
""")
Page<Property> findAllWithViewings(
        Pageable pageable
);
```

This is dangerous.

Why?

One Property may produce many SQL rows:

```
Property A | Viewing 1
Property A | Viewing 2
Property A | Viewing 3
Property B | Viewing 4
...
```

But pagination wants:

```
20 unique Properties
```

while the database sees:

```
joined rows
```

The boundaries don't align cleanly.

Hibernate may need inefficient in-memory handling or may warn/fail depending on configuration/query.

So remember:

> Paginating parent entities while fetch-joining a to-many collection is usually something to avoid or design very carefully.

## To-One Fetch Joins With Pagination

Fetching:

```
Property → Agent
```

is generally much less problematic.

Each Property has at most one Agent:

```
Property A | Agent 1
Property B | Agent 2
Property C | Agent 1
```

Each Property still corresponds roughly to one joined row.

So:

```
JOIN FETCH p.agent
```

can often work well with pagination.

The dangerous multiplication mainly appears with:

```
@OneToMany
@ManyToMany
```

collection joins.

## Offset Pagination

What we've described so far is commonly called offset pagination.

For example:

```
page 0 size 20
→ OFFSET 0

page 1 size 20
→ OFFSET 20

page 10000 size 20
→ OFFSET 200000
```

The database may need to walk past many rows before reaching a deep offset.

So deep pagination can become expensive on very large datasets.

## Deep Pagination Problem

Imagine:

```
page = 50,000
size = 20
```

Conceptually:

```
OFFSET 1,000,000
LIMIT 20
```

The database may have to locate/skip a huge number of matching rows before returning 20.

This can become inefficient.

For ordinary administrative applications and moderate datasets, offset pagination is often perfectly acceptable.

For very large/high-throughput feeds, another approach may be better.

## Keyset / Cursor Pagination

An alternative is keyset pagination, often exposed as cursor pagination.

Instead of:

```
Give me page 50,000
```

we say:

```
Give me the next 20 records after this known value.
```

For example:

```
lastSeenId = 82372
```

or:

```
lastCreatedAt = ...
```

Conceptual SQL:

```
WHERE id > :lastSeenId
ORDER BY id
LIMIT 20;
```

Now the database can use an index to continue from a known position rather than skip a huge offset.

## Offset vs Cursor

**Offset/Page pagination**

Advantages:

- simple
- supports page numbers
- easy to jump directly to page 10
- Spring Pageable works naturally

Disadvantages:

- deep offsets can be expensive
- results can shift if records are inserted/deleted between requests

---

**Cursor/Keyset pagination**

Advantages:

- efficient for large sequential datasets
- stable for feeds/infinite scroll

Disadvantages:

- harder to jump to arbitrary page numbers
- cursor design is more complex
- sorting requirements become stricter

For BrightMove, ordinary Spring pagination is a sensible starting point.

Knowing keyset pagination exists is enough for now.

## Pagination Consistency

Suppose results are ordered:

```
newest first
```

Client loads page 0.

Then another Property is inserted.

Client requests page 1.

Because offset positions shifted, the client may:

```
see one record twice
```

or:

```
miss a record
```

This isn't necessarily a bug in Spring.

It's inherent in offset pagination over changing datasets.

For many business UIs this is acceptable.

For highly dynamic feeds where stable traversal matters, cursor pagination is often better.

## Indexes and Sorting

Sorting large datasets can be expensive.

Suppose:

```
ORDER BY created_at DESC
```

If `created_at` is indexed appropriately, PostgreSQL may satisfy that query efficiently.

Without a useful index, it may need to sort a large result set.

So:

```
pagination
+
filtering
+
sorting
```

eventually connects to database indexing.

For example:

```
WHERE status = 'AVAILABLE'
ORDER BY created_at DESC
```

may benefit from an index strategy designed around that access pattern.

Index design itself belongs primarily to database engineering, but a backend engineer should understand that query design affects index requirements.

## Stable Sorting

Always think about deterministic ordering.

Suppose:

```
ORDER BY createdAt DESC
```

and 20 Properties have exactly the same timestamp.

The database is not required to return tied rows in the same order every time.

So a more stable sort is:

```
createdAt DESC,
id DESC
```

This creates a tie-breaker.

For pagination:

> Prefer a deterministic sort containing a unique or near-unique tie-breaker.

This becomes particularly important for cursor pagination.

## BrightMove Search Endpoint Example

An endpoint might eventually look like:

```
GET /properties
    ?city=Edinburgh
    &page=0
    &size=20
    &sort=price,asc
```

But we're not yet handling many optional filters.

For now, service:

```
@Transactional(readOnly = true)
public Page<PropertySummary> getProperties(
        String city,
        Pageable pageable
) {

    return propertyRepository
            .findByCity(city, pageable)
            .map(propertyMapper::toSummary);
}
```

In Module 13, this evolves into:

```
city optional
price optional
bedrooms optional
status optional
agent optional
```

using Specifications.

## Custom API Page DTO

For a stable API contract:

```
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {

    public static <T> PageResponse<T> from(
            Page<T> page
    ) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
```

Service/controller can convert:

```
Page<PropertySummary> result = ...;

return PageResponse.from(result);
```

Now Spring Data remains an internal implementation detail.

## Request & Data Flow

Suppose:

```
GET /properties?page=1&size=20&sort=price,asc
```

Flow:

```
HTTP Request
      ↓
Controller
      ↓
page = 1
size = 20
sort = price ASC
      ↓
Pageable
      ↓
Service
      ↓
Repository
      ↓
Hibernate
      ↓
SQL with filtering/order/pagination
      ↓
PostgreSQL
      ↓
20 rows
      ↓
Page<Property>
      ↓
map()
      ↓
Page<PropertyResponse>
      ↓
PageResponse DTO
      ↓
JSON
```

If using `Page`:

```
content query
+
count query
```

may occur.

That is the complete mental model.

## `Page` and Persistence Context

Suppose repository returns:

```
Page<Property>
```

The content contains managed entities while inside the persistence context.

So the same rules from 3.11 still apply:

```
page.map(propertyMapper::toResponse);
```

If toResponse accesses lazy relationships, those accesses may trigger SQL.

Pagination does **not** solve N+1.

For example:

```
20 Properties
+
agent lazy loading
```

could still generate:

```
1 paginated Property query
+
up to 20 Agent queries
```

So pagination and fetching strategy must be designed together.

## Projection + Pagination Often Fits Read APIs Well

Suppose the search page needs:

```
title
city
price
agent name
```

A projection-based paginated query can often be clean:

```
@Query("""
    SELECT new com.brightmove.dto.PropertySummary(
        p.id,
        p.title,
        p.city,
        p.price,
        a.name
    )
    FROM Property p
    JOIN p.agent a
""")
Page<PropertySummary> findSummaries(
        Pageable pageable
);
```

Now:

```
correct page
+
correct fields
+
no entity graph traversal
```

This can be a strong design for read-only list/search endpoints.

## Common Mistakes

**Returning findAll() for potentially large tables**

Always consider whether the dataset can grow.

---

**Allowing arbitrary page sizes**

Protect the backend with reasonable limits.

---

**Paginating without stable sorting**

Result ordering can become inconsistent.

---

**Assuming `Page` is free**

`Page` often requires a count query.

---

**Using `Page` when only "has next" is needed**

`Slice` may be cheaper.

---

**Fetch-joining collections with pagination casually**

Joined rows and parent pagination don't align cleanly.

---

**Forgetting N+1 inside paginated mapping**

Twenty rows can still cause twenty relationship queries.

---

**Exposing entity field names directly as unrestricted sort parameters**

This leaks persistence details and can allow undesirable database workload.

---

**Filtering after pagination in Java**

Avoid:

```
repository.findAll(pageable)
        .stream()
        .filter(...)
```

if the filter belongs in the database query.

You would be filtering only the current page rather than the entire matching dataset, which also produces logically incorrect pagination.

## Engineering Trade-offs

Pagination reduces:

- memory usage
- database result size
- network response size
- serialization cost

But it introduces design decisions:

```
Page or Slice?

Offset or cursor?

What page size?

What sort order?

What metadata should API expose?

How should relationships be fetched?

Do we need total count?
```

There is no universal best option.

The API use case determines the right design.

## Pagination Design Framework

When designing a paginated endpoint, reason in this order:

```
1. What data is being listed?

2. Could this dataset become large?

3. What is the default page size?

4. What is the maximum page size?

5. What deterministic sort order should be used?

6. Does the UI need total count/pages?
      │
      ├── yes → Page
      └── no  → Slice may be enough

7. Does the response need full entities?
      │
      ├── yes → design fetch plan
      └── no  → projection may be better

8. Are to-many relationships being fetch-joined?
      ↓
   beware pagination

9. Could users navigate very deeply?
      ↓
   consider keyset/cursor pagination

10. Are filter/sort columns indexed appropriately?
```

## Summary

The central abstraction is:

```
Pageable
```

which describes:

```
page
+
size
+
sort
```

Repository:

```
Page<Property> findByCity(
        String city,
        Pageable pageable
);
```

returns:

```
content
+
pagination metadata
```

The important difference:

```
Page<T>
→ content + total counts/pages

Slice<T>
→ content + whether more exists
```

Therefore:

```
Need "Page 3 of 50"
→ Page

Need "Load More"
→ Slice
```

Sorting should be deterministic:

```
createdAt DESC
+
id DESC
```

rather than relying on unspecified database order.

And keep the connection to module 11:

```
Pagination
→ controls HOW MUCH data

Fetching
→ controls WHICH related data

Projection
→ controls WHICH fields/data shape
```

Together:

```
Use Case
    ↓
Query/filter
    ↓
Projection or Entity
    ↓
Fetch plan
    ↓
Sort
    ↓
Pagination
    ↓
Efficient API response
```

The most important engineering principle is:

> Pagination is not simply a UI feature; it is a database and API resource-management strategy.
