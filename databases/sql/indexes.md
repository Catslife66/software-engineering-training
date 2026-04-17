# Indexes

> An index is a data structure that helps the database find rows quickly without scanning everything.

Example:
We create an index on email:

```
CREATE INDEX idx_users_email ON users(email);
```

Now the database builds a structure (usually a B-tree) like:

```
alice@example.com → row 12345
bob@example.com   → row 67890
carol@example.com → row 99999
```

Now query:

```
SELECT *
FROM users
WHERE email = 'alice@example.com';
```

The database:

- looks up email in the index
- finds the row location
- jumps directly to it
- the work is roughly: O(log n)

## Why indexes matter

Indexes are one of the biggest performance improvements in databases.

Without indexes:

- queries slow down as data grows because FULL TABLE SCAN

With indexes:

- queries stay fast even with millions of rows

## But indexes are not free

1. Storage cost

   Indexes take extra space.

2. Write cost

   When inserting/updating:

   ```
   INSERT → update table + update index
   ```

   So writes become slower.

3. Too many indexes = bad

   If you index everything:
   - writes become slow
   - memory usage increases

## When to use indexes

Good candidates:

```
WHERE id = ?  // Equality lookups
WHERE created_at > '2024-01-01'     // Range queries
JOIN ON orders.customer_id = customers.customer_id       // JOIN conditions -> joins rely heavily on indexes.
ORDER BY created_at DESC  // ORDER BY with LIMIT
GROUP BY columns
```

Bad candidates:

```
columns rarely queried  // No point indexing unused data.
columns frequently updated  // Indexes must be updated too → slows writes
very low selectivity (e.g. gender = 'M'/'F')
```

### Step-by-step decision rule

- Step 1 — Is this column used in WHERE / JOIN / ORDER BY?

  If no → ❌ don’t index

  If yes → go to step 2

- Step 2 — Does it reduce the data a lot?

  Ask: How many rows will this condition return?

  ```
  WHERE email = 'alice@example.com'
  ```

  → returns 1 row

  ✅ index is very useful

  ```
  WHERE gender = 'female'
  ```

  → returns ~50% of table

  ❌ index is not useful

### Key concept: Selectivity

Selectivity = how much the condition narrows the data

| Column      | Selectivity | Good for index? |
| ----------- | ----------- | --------------- |
| email       | very high   | ✅              |
| user_id     | very high   | ✅              |
| customer_id | medium/high | ✅              |
| gender      | very low    | ❌              |

### mental model

Ask: Can the database "jump directly" to the row?

email → yes → index helps

gender → no → still many rows → index not helpful

## Composite Indexes

Real queries often use multiple columns.

Example:

```
SELECT *
FROM orders
WHERE customer_id = 10
AND status = 'completed';
```

We can create:

```
CREATE INDEX idx_orders_customer_status
ON orders(customer_id, status);
```

This is called a **composite index**.

### How it works

The index is ordered like: (customer_id, status)

Example:

```
| customer_id | status    |
| ----------- | --------- |
| 1           | completed |
| 1           | pending   |
| 2           | completed |
| 2           | pending   |
```

### Important rule

Composite indexes follow a **left-to-right** rule:

```
INDEX(A, B)
```

Works well for:

```
WHERE A = ?
WHERE A = ? AND B = ?
```

But NOT for:

```
WHERE B = ?
```

Example:

Index: (customer_id, status)

Works well

```
WHERE customer_id = 10
WHERE customer_id = 10 AND status = 'completed'
```

Does NOT work well

```
WHERE status = 'completed'
```

Because customer_id is missing and status is low selectivity.

## Golden rules(summary)

**Rule 1 — Equality columns first**

```
INDEX(A, B)
WHERE A = ? AND B > ?
```

→ very efficient, narrows the result well, a common lookup pattern

**Rule 2 — Most selective first**

Among equality columns:

```
higher selectivity → earlier in index
```

**Rule 3 — Order matters**

```
INDEX(A, B) ≠ INDEX(B, A)
```

**Rule 4 — One good composite index > many single indexes**

**Rule 5 — Think like the database**

Ask: How can I narrow the data as early as possible?

> Put the MOST selective column first in a composite index

## Trade-offs

| Benefit          | Cost                 |
| ---------------- | -------------------- |
| fast reads       | slower writes        |
| efficient search | extra storage        |
| better joins     | maintenance overhead |
