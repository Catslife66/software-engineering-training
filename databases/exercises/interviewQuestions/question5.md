# Interview Round 5 (Advanced pattern)

**DATASET**

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

**Problem**

Return:

| customer_id | favorite_product |

Where:

- favorite_product = the product_id the customer bought the most total quantity of
- if tie → return the smaller product_id
- sort by customer_id ASC

**Answer**

```
WITH order_records AS (
    SELECT o.customer_id, i.product_id, SUM(i.quantity) AS total_qty
    FROM orders o
    JOIN order_items i ON i.order_id = o.order_id
    GROUP BY o.customer_id, i.product_id
), customer_max AS (
    SELECT r.customer_id, MAX(r.total_qty) AS max_qty
    FROM order_records r
    GROUP BY r.customer_id
)
SELECT r.customer_id, MIN(r.product_id) AS favorite_prodcut
FROM order_records r
JOIN customer_max m ON m.customer_id = r.customer_id AND m.max_qty = r.total_qty
GROUP BY r.customer_id
ORDER BY r.customer_id ASC;
```
