# Covering Index

This is a very **practical optimization** and shows up in interviews.

**Problem**

Even with an index:

```
SELECT * FROM users WHERE email = 'alice@example.com';
```

The database:

1. uses index to find row location
2. goes back to table to fetch full row

👉 This is called:

```
index lookup + table lookup
```

## Solution: Covering Index

If the index already contains all needed columns:

```
CREATE INDEX idx_users_email_name
ON users(email, name);
```

Then query:

```
SELECT name FROM users WHERE email = 'alice@example.com';
```

Now:

```
database reads ONLY the index
```

👉 No table lookup needed

This is called **Index-only scan (covering index)**

**Why it’s faster**

```
Index is smaller → fewer disk reads
No extra table lookup → less work
```

If all selected columns are in the index → covering index

## Why SELECT \* is bad

```
SELECT * FROM users WHERE email = 'alice@example.com';
```

Even with index:

- ❌ still needs table lookup
- ❌ cannot be covered

This is better

```
SELECT name FROM users WHERE email = 'alice@example.com';
```

With index:

```
INDEX(email, name)
```

- ✅ fully covered
- ✅ faster

## Mental model

```
Index = shortcut
Covering index = shortcut + full answer
```

## Index Design Thinking

**Real world scenario**

Table: `orders`

Columns:

- order_id (PK)
- customer_id
- status
- created_at
- total_amount

Query 1

```
SELECT *
FROM orders
WHERE customer_id = 10
ORDER BY created_at DESC
LIMIT 5;
```

**Question**

What index would you design?

**What the database wants**

Ideally:

```
Jump to customer_id = 10
→ already sorted by created_at DESC
→ read first 5 rows
→ stop
```

**Best index**

```
CREATE INDEX idx_orders_customer_created_at
ON orders(customer_id, created_at DESC);
```
