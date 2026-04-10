# Interview Round 3 (Classic trap question)

**DATASET**

customers

| customer_id | name  |
| ----------- | ----- |
| 1           | Alice |
| 2           | Bob   |
| 3           | Carol |

orders

| order_id | customer_id |
| -------- | ----------- |
| 1        | 1           |
| 2        | 1           |
| 3        | 2           |

order_items

| order_id | product_id |
| -------- | ---------- |
| 1        | 10         |
| 1        | 11         |
| 2        | 10         |
| 3        | 12         |

**Problem**

Return:

| customer_id | distinct_products_bought |

Where:

- count how many different products each customer has bought
- include customers with zero purchases
- sort by distinct_products_bought DESC, then customer_id ASC

**Answer**

```
SELECT c.customer_id, COUNT(DISTINCT i.product_id) AS distinct_products_bought
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
LEFT JOIN order_items i ON i.order_id = o.order_id
GROUP BY c.customer_id
ORDER BY distinct_products_bought DESC, customer_id ASC;
```
