# Pattern 3 - Conditional Aggregation

**Dataset**

orders

| order_id | customer_id | status    |
| -------- | ----------- | --------- |
| 1        | 1           | completed |
| 2        | 1           | pending   |
| 3        | 2           | completed |
| 4        | 2           | cancelled |
| 5        | 2           | completed |

## Drill 1

Return:

| customer_id | completed_orders |

Where:

- count only completed orders per customer

**New Pattern**

Think:

```
SUM(
    CASE WHEN ... THEN 1 ELSE 0 END
)
```

because:

```
true → 1
false → 0
```

```
SELECT customer_id,
       SUM(
           CASE
               WHEN status = 'completed'
               THEN 1
               ELSE 0
           END
       ) AS completed_orders
FROM orders
GROUP BY customer_id;
```

## Drill 2

Return:

| customer_id | completed_orders | pending_orders |

```
SELECT customer_id,
    SUM(
        CASE
            WHEN status = 'completed'
            THEN 1
            ELSE 0
        END
    ) AS completed_orders,
    SUM(
        CASE
            WHEN status = 'pending'
            THEN 1
            ELSE 0
        END
    ) AS pending_orders
FROM orders
GROUP BY customer_id;
```

## Common interview pattern

You will often see:

```
SUM(CASE WHEN ... THEN 1 ELSE 0 END)
```

or:

```
COUNT(CASE WHEN ... THEN 1 END)
```
