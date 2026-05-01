# Query Planner

Even if an index exists, the database does not always use it.

Because the database has a **query planner**.

Its job is to estimate:

```
Which way is cheapest?
- use index?
- scan whole table?
- use one index or another?
```

It chooses the plan it thinks will be fastest.

Example:

Suppose this index exists:

```
CREATE INDEX idx_orders_status ON orders(status);
```

and query:

```
SELECT * FROM orders
WHERE status = 'completed';
```

If 90% of rows are completed, the planner may think:

```
Using the index still touches almost the whole table.
A full scan is cheaper.
```

So it ignores the index.

## What the planner cares about

At a high level:

1. How many rows will match?

```
Few rows → index attractive
Many rows → scan may be better
```

2. Is the index aligned with the query?

```
Good match → useful
Wrong order / wrong column → less useful
```

3. Does the query need all columns?

```
If SELECT *, the database may still need to visit the table after using the index
```

4. Table size

On a tiny table, a full scan may be faster anyway

## Key refinement

```
Small table → scan
Large + selective → index
Large + low selectivity → scan
```
