# Latest Row Per Entity

Whenever you see:

```
choose one row
```

ask yourself:

```
What happens if there is a tie?
```

This is one of the most common SQL interview traps.

## Pattern Checklist

When you see:

```
Latest row per customer
Highest salary employee
Most recent order
Favorite product
Top purchase
```

Immediately think:

```
1. Is there a tie?
2. Keep all ties?
3. Choose one winner?
4. If one winner, what is the tie-break rule?
```

### Option A — Aggregate + Join Back -> bad for ties

```
Find max date
→ join back to orders
```

Pros:

```
Works everywhere
Shows understanding of aggregate→join-back
```

Cons:

```
More steps
Tie handling can get messy
```

### Option B — ROW_NUMBER()

```
ROW_NUMBER() OVER (
    PARTITION BY customer_id
    ORDER BY order_date DESC
)
```

Then:

```
WHERE rn = 1
```

Pros:

```
Shorter
Easier tie-breaking
Very common in interviews
```

Cons:

```
Requires window functions
```

## Drill 1

**Dataset**

orders

| order_id | customer_id | order_date |
| -------- | ----------- | ---------- |
| 1        | 101         | 2025-01-01 |
| 2        | 101         | 2025-01-05 |
| 3        | 102         | 2025-01-02 |
| 4        | 102         | 2025-01-10 |
| 5        | 103         | 2025-01-03 |

Goal:

| customer_id | latest_order_date |
| ----------- | ----------------- |
| 101         | 2025-01-05        |
| 102         | 2025-01-10        |
| 103         | 2025-01-03        |

```
SELECT customer_id, MAX(order_date) AS latest_order_date
FROM orders
GROUP BY customer_id
```

## Drill 2 - Latest Order Row Per Customer

Same table

Goal:

| customer_id | order_id | order_date |
| ----------- | -------- | ---------- |
| 101         | 2        | 2025-01-05 |
| 102         | 4        | 2025-01-10 |
| 103         | 5        | 2025-01-03 |

```
WITH ranking AS (
    SELECT customer_id,
           order_id,
           order_date,
           ROW_NUMBER() OVER (
               PARTITION BY customer_id
               ORDER BY order_date DESC,
                        order_id ASC
           ) AS rn
    FROM orders
)
SELECT customer_id,
       order_id,
       order_date
FROM ranking
WHERE rn = 1;
```
