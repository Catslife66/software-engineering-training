# Query execution & EXPLAIN plans

## What is EXPLAIN?

```
EXPLAIN SELECT * FROM orders WHERE customer_id = 10;
```

👉 It shows:

```
How the database plans to execute your query
```

NOT the result — but the strategy.

## Think like the database

Before running a query, the database asks:

```
What is the fastest way to get this data?
- scan whole table?
- use index?
- which index?
- join order?
```

👉 The answer is the query plan

**Example**

Query

```
SELECT *
FROM orders
WHERE customer_id = 10;
```

EXPLAIN output (simplified)

### Case 1 — no index

```
Seq Scan on orders
Filter: customer_id = 10
```

### Case 2 — with index

```
Index Scan using idx_orders_customer_id on orders
Index Cond: customer_id = 10
```

## Key terms

**1. Seq Scan (Sequential Scan)**

```
Read entire table row by row
```

👉 slow for large tables

**2. Index Scan**

```
Use index to find rows quickly
```

👉 fast for selective queries

**3. Index Cond**

```
Condition used inside index
```

**4. Filter**

```
Condition applied after reading rows
```

### Important difference

```
Index Cond = used by index → efficient
Filter = applied after fetching candidate rows → less efficient
```

**Example**

```
SELECT *
FROM orders
WHERE customer_id = 10
AND status = 'completed';
```

- If index is:

```
INDEX(customer_id)
```

Then plan might be:

```
Index Scan
Index Cond: customer_id = 10
Filter: status = 'completed'
```

👉 index helps partially, but still filters rows after

- If index is:

```
INDEX(customer_id, status)
```

Then:

```
Index Scan
Index Cond: customer_id = 10 AND status = 'completed'
```

👉 much better — no extra filtering

## Another important concept: Cost

EXPLAIN shows something like:

```
cost=0.29..8.50
```

👉 This means:

```
estimated cost of executing query
```

Lower cost → better plan

### What affects cost?

- number of rows scanned
- index vs full scan
- disk I/O
- sorting
- joins

## Real optimization thinking

Query

```
SELECT *
FROM orders
WHERE customer_id = 10
ORDER BY created_at DESC;
```

**Bad plan**

```
Seq Scan → Sort
```

👉 scan all rows, then sort

**Good plan**

```
Index Scan using (customer_id, created_at)
```

👉 already sorted, no extra work

## How engineers use EXPLAIN

They ask:

1. Is it using index or seq scan?
2. Is filter happening after scan?
3. Is sorting happening?
4. How many rows are processed?

## The Workflow Engineers Use

When a query is slow:

```
1. Run EXPLAIN (or EXPLAIN ANALYZE)
2. Identify the problem
3. Fix it (index / query rewrite)
4. Re-run EXPLAIN
```

### Case Study 1 — Classic Slow Query

Query

```
SELECT *
FROM orders
WHERE customer_id = 10
AND status = 'completed';
```

EXPLAIN

```
Seq Scan on orders
Filter: customer_id = 10 AND status = 'completed'
```

What’s the problem?

```
Full table scan → slow
```

Even though conditions exist, no index is helping.

Fix

```
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

New EXPLAIN

```
Index Scan using idx_orders_customer_status
Index Cond: customer_id = 10 AND status = 'completed'
```

Lesson

```
If multiple conditions appear in WHERE → consider composite index
```

### Case Study 2 — Hidden Problem

Query

```
SELECT *
FROM orders
WHERE customer_id = 10
AND created_at > '2024-01-01';
```

Existing index

```
INDEX(customer_id)
```

EXPLAIN

```
Index Scan using idx_customer_id
Index Cond: customer_id = 10
Filter: created_at > '2024-01-01'
```

Problem?

```
created_at is not used in index → extra filtering
```

Fix

```
CREATE INDEX idx_customer_created
ON orders(customer_id, created_at);
```

New EXPLAIN

```
Index Scan
Index Cond: customer_id = 10 AND created_at > '2024-01-01'
```

Lesson

```
Equality first, range second in composite index
```

### Case Study 3 — ORDER BY Problem

Query

```
SELECT *
FROM orders
WHERE customer_id = 10
ORDER BY created_at DESC;
```

Bad EXPLAIN

```
Index Scan (customer_id)
→ Sort
```

❓ Problem?

```
Database must sort after fetching rows → slow
```

✅ Fix

```
CREATE INDEX idx_customer_created_desc
ON orders(customer_id, created_at DESC);
```

New EXPLAIN

```
Index Scan
(no Sort step)
```

Lesson

```
Index can eliminate sorting if it matches ORDER BY
```

### Case Study 4 — LIMIT Optimization

Query

```
SELECT *
FROM orders
WHERE customer_id = 10
ORDER BY created_at DESC
LIMIT 5;
```

With correct index

```
INDEX(customer_id, created_at DESC)
```

What happens

```
Jump to customer 10
Read first 5 rows
STOP
```

Huge performance gain

```
Reads 5 rows instead of thousands
```

## Summary of Fix Patterns

### Pattern 1 — Full scan

→ add index on WHERE column

### Pattern 2 — Filter after index

→ extend index to include that column

### Pattern 3 — Sorting

→ include ORDER BY column in index

### Pattern 4 — LIMIT

→ combine WHERE + ORDER BY in index
