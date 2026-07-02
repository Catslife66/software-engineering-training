# Pagination & Sorting

The Problem

Suppose we have:

```
List<Property> findByCity(String city);
```

and the database contains:

```
London → 500,000 properties
```

What happens if someone calls:

```
GET /properties?city=London
```

and we return:

```
500,000 Property objects
```

Problems:

```
Huge memory usage
Slow database query
Slow network response
Poor user experience
```

Most users only want:

```
First 20 results
```

not:

```
500,000 results
```

Solution 👇

## Pagination

Instead of:

```
All Results
```

we return:

```
Page 1 → 20 properties
Page 2 → next 20 properties
Page 3 → next 20 properties
```

**SQL Version**

Database conceptually does:

```
SELECT *
FROM properties
LIMIT 20
OFFSET 0;
```

Page 1.

```
SELECT *
FROM properties
LIMIT 20
OFFSET 20;
```

Page 2.

```
SELECT *
FROM properties
LIMIT 20
OFFSET 40;
```

Page 3.

**Spring Data JPA Version**

Instead of:

```
List<Property> findByCity(String city);
```

we can write:

```
Page<Property> findByCity(
    String city,
    Pageable pageable
);
```

Notice two new types:

```
Page<Property>
```

and

```
Pageable
```

## What is Pageable?

Think:

```
Instructions for pagination
```

Example:

```
Pageable pageable = PageRequest.of(0, 20);
```

means:

```
Page 0
20 items per page
```

## What is Page?

Think:

```
Results + Pagination Metadata
```

Not just:

```
List<Property>
```

A Page contains:

```
Properties
Current page
Total pages
Total records
Page size
```

Example:

```
Page<Property> page = repository.findByCity(
                         "London",
                         pageable
                     );
```

You can access:

```
page.getContent()
↓
List<Property>

page.getTotalElements()
↓
500000

page.getTotalPages()
↓
25000
```

## Real API Example

Client:

```
GET /properties?page=0&size=20
```

Controller:

```
@GetMapping
public Page<PropertyDTO> getProperties(Pageable pageable)
```

Service:

```
return propertyRepository.findAll(pageable);
```

Repository:

```
Page<Property> findAll(Pageable pageable);
```

## Real API Problem

Imagine:

```
PageRequest.of(0, 20)
```

returns:

```
Property A
Property B
Property C
...
```

Then somebody inserts a new property.

The next request might return:

```
Property X
Property A
Property B
...
```

Now pagination becomes inconsistent.

Users may see:

```
Duplicates
Missing records
Records moving between pages
```

## Sorting

Real pagination is usually:

```
PageRequest.of(
    0,
    20,
    Sort.by("createdAt")
);
```

or:

```
PageRequest.of(
    0,
    20,
    Sort.by("price").descending()
);
```

or:

```
PageRequest.of(
    0,
    20,
    Sort.by("title")
);
```

## Mental Model

Pagination without sorting:

```
Give me 20 records
```

Database:

```
Which 20?
🤷
```

Pagination with sorting:

```
Give me the first 20
ordered by createdAt
```

Database:

```
Clear instruction.
```

## Why Pagination and Sorting Belong Together

Notice:

```
Pagination
```

answers:

```
Which chunk of data?
```

while:

```
Sorting
```

answers:

```
In what order?
```

Without sorting:

```
Page 1
Page 2
Page 3
```

can be unstable.

With sorting:

```
Cheapest → Most expensive
```

or

```
Newest → Oldest
```

the pages become predictable.

## Real Example

Frontend sends:

```
GET /properties
    ?city=London
    &status=AVAILABLE
    &page=2
    &size=25
    &sort=price,desc
```

Controller:

```
@GetMapping
public Page<PropertyDTO> search(
    String city,
    String status,
    Pageable pageable
)
```

Service:

```
return propertyRepository
    .findByCityAndStatus(
        city,
        status,
        pageable
    );
```

Repository:

```
Page<Property> findByCityAndStatus(
    String city,
    String status,
    Pageable pageable
);
```

This is very close to how a real Spring Boot API is written.

## Alternative: Slice

So far we used:

```
Page<PropertyViewingCountDTO>
```

A Page gives:

```
current results
+
totalElements
+
totalPages
```

But to know:

```
totalElements
```

Spring often has to run an extra count query.

For simple searches this is fine.

But for complex reports with:

```
GROUP BY
HAVING
LEFT JOIN
DTO projection
```

the count query can become expensive.

### Slice

```
Slice<PropertyViewingCountDTO>
```

A `Slice` gives:

```
current results
+
hasNext
```

but **does not** calculate total pages.

So it is cheaper.

### Mental model

Page

```
Tell me:
- results
- total number of results
- total pages
```

More information, more work.

Slice

```
Tell me:
- results
- whether there is another page
```

Less information, often faster.

### When to use what?

Use Page when the UI needs:

```
Page 1 of 25
Total 493 results
```

Use Slice when the UI only needs:

```
Load more
Next page
Infinite scroll
```

### Example

User opens property search.

Frontend requests:

```
GET /properties?page=0&size=20
```

Backend returns:

```
20 properties
hasNext = true
```

User scrolls.

↓

Frontend requests:

```
GET /properties?page=1&size=20
```

Backend returns:

```
20 properties
hasNext = true
```

User scrolls again.

↓

Eventually:

```
20 properties
hasNext = false
```

Frontend knows:

```
Stop requesting more data.
```

No need to know:

```
totalElements = 12,456
totalPages = 623
```
