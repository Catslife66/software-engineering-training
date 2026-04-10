## Question 1

Return:

| customer_name | order_count |

Include customers with zero orders.

```
SELECT c.name AS customer_name, COUNT(o.order_id) AS order_count
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
GROUP BY c.customer_id, c.name;
```

## Question 2

Return:

| product_id | total_quantity |

Only include products whose total quantity is greater than 2.

Use Table order_items:

```
| order_id | product_id | quantity |
| -------- | ---------- | -------- |
| 1        | 1          | 2        |
| 1        | 2          | 1        |
| 2        | 1          | 3        |
| 3        | 2          | 1        |
```

```
SELECT product_id, SUM(quantity) AS total_quantity
FROM order_items
GROUP BY product_id
HAVING SUM(quantity) > 2;
```

## Question 3

Return:

| customer_name | total_items |

Include only customers whose total_items is greater than the average total_items across all customers who have at least one order.

```
Step 1 → total per customer (GROUP BY customer)
Step 2 → average of those totals
Step 3 → compare each customer to that average
```

```
WITH customer_totals AS (
    SELECT c.customer_id, c.name AS customer_name, SUM(i.quantity) AS total_items
    FROM customers c
    JOIN orders o ON o.customer_id = c.customer_id
    JOIN order_items i ON i.order_id = o.order_id
    GROUP BY c.customer_id, c.name
)
SELECT customer_name, total_items
FROM customer_totals
WHERE total_items > (
    SELECT AVG(total_items)
    FROM customer_totals
);
```

## Clean mental model

Think like this:

```
SQL processes in layers
Layer 1: GROUP BY customer → total_items
Layer 2: AVG(total_items)
Layer 3: filter customers using that average
```

## Pattern to remember

When you see:

> compare each group to the average of all groups

think:

```
WITH grouped_data AS (...)
SELECT ...
FROM grouped_data
WHERE value > (SELECT AVG(value) FROM grouped_data);
```

SQL requires multi-level aggregation to be done in separate query layers (subquery or CTE)
