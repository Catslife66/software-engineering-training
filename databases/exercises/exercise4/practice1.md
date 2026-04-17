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

Problem

Return:

| customer_id | favorite_product |

Where:

- favorite_product = product with highest total quantity per customer
- tie → smaller product_id
- sort by customer_id ASC

using group by method

```
WITH customer_orders AS (
    SELECT o.customer_id, i.product_id, SUM(i.quantity) AS product_sum
    FROM orders o
    JOIN order_items i ON i.order_id = o.order_id
    GROUP BY o.customer_id, i.product_id
),
product_sums AS (
    SELECT customer_id, MAX(product_sum) AS most_buy
    FROM customer_orders
    GROUP BY customer_id
)
SELECT co.customer_id, MIN(co.product_id) AS favorite_product
FROM customer_orders co
JOIN product_sums ps ON ps.customer_id = co.customer_id AND co.product_sum = ps.most_buy
GROUP BY co.customer_id
ORDER BY co.customer_id ASC;
```

using window funcion method

```
WITH products_total AS (
    SELECT o.customer_id, i.product_id, SUM(i.quantity) AS total_qty
    FROM orders o
    JOIN order_items i ON i.order_id = o.order_id
    GROUP BY o.customer_id, i.product_id
),
ranked_product AS (
    SELECT pt.customer_id, pt.product_id, pt.total_qty,
        ROW_NUMBER() OVER (
            PARTITION BY pt.customer_id
            ORDER BY pt.total_qty DESC, pr.product_id ASC
        ) AS rn
    FROM products_total pt
)
SELECT rp.customer_id,
       rp.product_id AS favorite_product
FROM ranked_products rp
WHERE rp.rn = 1
ORDER BY rp.customer_id ASC;
```
