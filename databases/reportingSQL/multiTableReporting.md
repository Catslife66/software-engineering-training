# Multi-table Reporting

Imagine you've joined an e-commerce company.

Here's the database.

customers

| customer_id | name    | signup_date |
| ----------: | ------- | ----------- |
|         101 | Alice   | 2026-07-01  |
|         102 | Bob     | 2026-07-05  |
|         103 | Charlie | 2026-07-10  |

orders

| order_id | customer_id | order_date |
| -------: | ----------: | ---------- |
|        1 |         101 | 2026-07-02 |
|        2 |         101 | 2026-07-08 |
|        3 |         102 | 2026-07-09 |
|        4 |         103 | 2026-07-11 |

order_items

| order_id | product_id | quantity | unit_price |
| -------: | ---------: | -------: | ---------: |
|        1 |          1 |        2 |         50 |
|        1 |          2 |        1 |        100 |
|        2 |          3 |        1 |        300 |
|        3 |          2 |        2 |        100 |
|        4 |          1 |        1 |         50 |
|        4 |          3 |        2 |        300 |

products

| product_id | product_name | category    |
| ---------: | ------------ | ----------- |
|          1 | Keyboard     | Accessories |
|          2 | Mouse        | Accessories |
|          3 | Monitor      | Displays    |

## Revenue by Product Category

Product Manager's request

> Show me total revenue by product category.

```
SELECT p.category,
       SUM(oi.quantity * oi.unit_price) AS total_revenue
FROM order_items oi
JOIN products p ON p.product_id = oi.product_id
GROUP BY p.category
ORDER BY total_revenue DESC;
```

## Revenue by Category Last Month

The Product Manager asks:

> Show total revenue by product category for July 2026 only.

```
SELECT p.category,
       SUM(oi.quantity * oi.unit_price) AS total_revenue
FROM order_items oi
JOIN products p ON p.product_id = oi.product_id
JOIN orders o ON o.order_id = oi.order_id
WHERE o.order_date >= DATE '2026-07-01'
  AND o.order_date <  DATE '2026-08-01'
GROUP BY p.category
ORDER BY total_revenue DESC;
```

## Revenue by Category and Month

The Product Manager now asks:

> “Show total revenue by product category for each month.”

Expected:

| month      | category    | total_revenue |
| ---------- | ----------- | ------------: |
| 2026-07-01 | Accessories |           ... |
| 2026-07-01 | Displays    |           ... |
| 2026-08-01 | Accessories |           ... |
| 2026-08-01 | Displays    |           ... |

```
SELECT DATE_TRUNC('month', o.order_date) AS month,
    p.category,
    SUM(oi.quantity * oi.unit_price) AS total_revenue
FROM order_items oi
JOIN products p ON p.product_id = oi.product_id
JOIN orders o ON o.order_id = oi.order_id
GROUP BY p.category, DATE_TRUNC('month', o.order_date);
```

The Product Manager asks:

> "For each month, show me the TOP selling category."

Expected:

| Month | Category    | Revenue |
| ----- | ----------- | ------: |
| Jul   | Accessories |     800 |
| Aug   | Displays    |    1200 |

```
WITH monthly_total AS (
    SELECT DATE_TRUNC('month', o.order_date) AS month,
        p.category,
        SUM(oi.quantity * oi.unit_price) AS total_revenue
    FROM order_items oi
    JOIN products p ON p.product_id = oi.product_id
    JOIN orders o ON o.order_id = oi.order_id
    GROUP BY p.category, DATE_TRUNC('month', o.order_date)
),
monthly_rn AS (
    SELECT month,
        category,
        total_revenue,
        ROW_NUMBER() OVER (
            PARTITION BY month,
            ORDER BY total_revenue DESC
        ) AS rn
    FROM monthly_total
)
SELECT month, category, total_revenue
FROM monthly_rn
WHERE rn = 1
ORDER BY month;
```

## Top Product per Category

Now the Product Manager asks:

> “For each category, show the product that generated the most revenue.”

Expected:

| category    | product_name | total_revenue |
| ----------- | ------------ | ------------: |
| Accessories | Mouse        |           ... |
| Displays    | Monitor      |           ... |

```
WITH product_revenue AS (
    SELECT p.product_id,
        p.category,
        p.product_name,
        SUM(oi.quantity * oi.unit_price) AS total_revenue,
    FROM order_items oi
    JOIN products p ON p.product_id = pv.product_id
    GROUP BY p.product_id, p.category, p.product_name
),
product_rank AS (
    SELECT product_id,
        product_name,
        category,
        total_revenue,
        RANK() OVER (
            PARTITION BY category
            ORDER BY total_revenue DESC
        ) AS r
    FROM product_revenue
)
SELECT category, product_name, total_revenue
FROM product_rank
WHERE r = 1;
```
