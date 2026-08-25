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
