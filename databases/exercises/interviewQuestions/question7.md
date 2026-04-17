# Question 7

## Question A

Query:

```
SELECT * FROM orders
WHERE customer_id = 10;
```

👉 Should we index customer_id?

Answer: Yes

- customer_id is used in WHERE
- it usually narrows the result well
- this is a common lookup pattern
- it is also often useful for joins

## Question B

Query:

```
SELECT * FROM orders
WHERE status = 'completed';
```

👉 Should we index status?

Answer: No

If most rows are completed, then:

- the query returns a large part of the table
- the index does not narrow much
- the database may prefer a full table scan

That is the key idea:

```
low selectivity → poor index candidate
```

Small nuance:

if status had many values and queries often filtered rare ones, the answer could change.

## Question C

Query:

```
SELECT * FROM orders
WHERE customer_id = 10
AND created_at > '2024-01-01';
```

👉 What index would you create?

Answer: INDEX(customer_id, created_at)

- jump to rows for customer 10
- then scan only rows after the given date within that customer’s section

This is very efficient.
