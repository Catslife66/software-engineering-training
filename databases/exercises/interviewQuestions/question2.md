# Interview Round 2 (Harder)

**Dataset**

orders

| order_id | customer_id |
| -------- | ----------- |
| 1        | 1           |
| 2        | 1           |
| 3        | 2           |
| 4        | 2           |
| 5        | 3           |

order_items

| order_id | quantity |
| -------- | -------- |
| 1        | 2        |
| 1        | 1        |
| 2        | 3        |
| 3        | 1        |
| 4        | 2        |
| 5        | 10       |

**Problem**

Return:

| customer_id | total_items |

Where:

- total_items = total quantity across all orders
- only include customers whose total_items is greater than the average total_items of all customers
- sort by total_items DESC

**Answer**

```
WITH customer_total AS (
    SELECT o.customer_id, SUM(i.quantity) AS total_items
    FROM orders o
    JOIN order_items i ON i.order_id = o.order_id
    GROUP BY o.customer_id
)
SELECT ct.customer_id, ct.total_items
FROM customer_total ct
WHERE ct.total_items > (
    SELECT AVG(total_items)
    FROM customer_total
)
ORDER BY total_items DESC;
```

orders

| order_id | customer_id |
| -------- | ----------- |
| 1        | 1           |
| 2        | 1           |
| 3        | 2           |
| 4        | 2           |
| 5        | 3           |

order_items

| order_id | product_id | quantity |
| -------- | ---------- | -------- |
| 1        | 10         | 2        |
| 1        | 11         | 1        |
| 2        | 10         | 3        |
| 3        | 12         | 1        |
| 4        | 12         | 2        |
| 5        | 13         | 10       |
