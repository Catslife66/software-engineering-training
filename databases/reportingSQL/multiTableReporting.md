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

## Customer Revenue by Month

The Product Manager asks:

> “For each month, show how much revenue each customer generated.”

Expected:

| month      | customer_id | customer_name | total_revenue |
| ---------- | ----------: | ------------- | ------------: |
| 2026-07-01 |         101 | Alice         |           500 |
| 2026-07-01 |         102 | Bob           |           200 |
| 2026-07-01 |         103 | Charlie       |           650 |

```
SELECT DATE_TRUNC('month', o.order_date) AS month,
       c.customer_id,
       c.name AS customer_name,
       SUM(oi.quantity * oi.unit_price) AS total_revenue
FROM customers c
JOIN orders o
  ON o.customer_id = c.customer_id
JOIN order_items oi
  ON oi.order_id = o.order_id
GROUP BY DATE_TRUNC('month', o.order_date),
         c.customer_id,
         c.name
ORDER BY month, c.customer_id;
```

## Top Customer per Month

The Product Manager asks:

> “For each month, show the customer who generated the most revenue.”

Expected:

| month      | customer_id | customer_name | total_revenue |
| ---------- | ----------: | ------------- | ------------: |
| 2026-07-01 |         103 | Charlie       |           650 |

```
WITH customer_revenue AS (
    SELECT DATE_TRUNC('month', o.order_date) AS month,
        c.customer_id,
        c.name,
        SUM(oi.quantity * oi.unit_price) AS total_revenue
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.order_id
    JOIN customers c ON c.customer_id = o.customer_id
    GROUP BY c.customer_id,
            c.name,
            DATE_TRUNC('month', o.order_date)
),
revenue_rank AS (
    SELECT month, customer_id, name, total_revenue,
        RANK() OVER (
            PARTITION BY month
            ORDER BY total_revenue DESC
        ) AS r
    FROM customer_revenue
)
SELECT month, customer_id, name, total_revenue
FROM revenue_rank
WHERE r = 1;
```

## New Customers by Month

The Product Manager asks:

> “For each month, how many customers placed their first-ever order that month?”

orders

| order_id | customer_id | order_date |
| -------: | ----------: | ---------- |
|        1 |         101 | 2026-07-02 |
|        2 |         101 | 2026-07-08 |
|        3 |         102 | 2026-07-09 |
|        4 |         103 | 2026-08-11 |
|        5 |         102 | 2026-08-15 |

Excepted:

| month      | new_customers |
| ---------- | ------------: |
| 2026-07-01 |             2 |
| 2026-08-01 |             1 |

```
WITH first_order AS (
    SELECT customer_id,
           MIN(order_date) AS first_order
    FROM orders
    GROUP BY customer_id
)
SELECT DATE_TRUNC('month', first_order) AS month,
       COUNT(customer_id) AS new_customers
FROM first_order
GROUP BY DATE_TRUNC('month', first_order)
ORDER BY month;
```

## New vs Returning Customers by Month

The Product Manager asks:

> “For each month, show how many customers were new and how many were returning.”

Expected:

| month      | new_customers | returning_customers |
| ---------- | ------------: | ------------------: |
| 2026-07-01 |             2 |                   0 |
| 2026-08-01 |             1 |                   1 |

Key:

- New customer: their first-ever order is in this month.
- Returning customer: they order in this month, but their first-ever order happened before this month.

```
WITH first_order AS (
    SELECT customer_id,
           MIN(order_date) AS first_order
    FROM orders
    GROUP BY customer_id
),
first_month AS (
    SELECT customer_id,
           DATE_TRUNC('month', first_order) AS first_month
    FROM first_order
),
customer_activity AS (
    SELECT DISTINCT customer_id,
           DATE_TRUNC('month', order_date) AS activity_month
    FROM orders
),
customer_status AS (
    SELECT ca.customer_id,
           ca.activity_month,
           fm.first_month,
           CASE
               WHEN ca.activity_month = fm.first_month
               THEN 'new'
               ELSE 'returning'
           END AS customer_type
    FROM customer_activity ca
    JOIN first_month fm
      ON fm.customer_id = ca.customer_id
)
SELECT activity_month,
       SUM(
           CASE
               WHEN customer_type = 'new'
               THEN 1
               ELSE 0
           END
       ) AS new_customers,
       SUM(
           CASE
               WHEN customer_type = 'returning'
               THEN 1
               ELSE 0
           END
       ) AS returning_customers
FROM customer_status
GROUP BY activity_month
ORDER BY activity_month;
```

Flow:

```
Problem:
Classify monthly customers as new or returning

Information needed:
When did they first order?
When are they active?

State/data representation:
first_month
activity_month

Rule:
activity_month = first_month → new
activity_month > first_month → returning

Implementation:
MIN()
DATE_TRUNC()
DISTINCT
JOIN
CASE
```

## Customers Returning After a Gap

Product Manager:

> “For each month, how many customers returned after being inactive for at least one full month?”

Consider this activity:

| customer_id | order_date |
| ----------: | ---------- |
|         101 | 2026-01-10 |
|         101 | 2026-02-12 |
|         101 | 2026-04-05 |
|         102 | 2026-01-15 |
|         102 | 2026-03-20 |
|         103 | 2026-03-01 |
|         103 | 2026-04-10 |

For example:

Customer 101:

```
Jan → active
Feb → active
Mar → inactive
Apr → active
Therefore April = returned after a gap
```

Customer 102:

```
Jan → active
Feb → inactive
Mar → active
Therefore March = returned after a gap
```

Customer 103:

```
Mar → active
Apr → active
Therefore April ≠ returned after a gap
```

Expected:

| month      | returned_after_gap |
| ---------- | -----------------: |
| 2026-03-01 |                  1 |
| 2026-04-01 |                  1 |

```
WITH current_active AS (
    SELECT DISTINCT customer_id,
           DATE_TRUNC('month', order_date) AS activity_month
    FROM orders
),
prevs_active AS (
    SELECT customer_id,
           activity_month,
           LAG(activity_month) OVER (
               PARTITION BY customer_id
               ORDER BY activity_month
           ) AS previous_activity_month
    FROM current_active
),
return_groups AS (
    SELECT customer_id,
           activity_month,
           previous_activity_month,
           CASE
               WHEN activity_month > previous_activity_month + INTERVAL '1 month'
               THEN 1
               ELSE 0
           END AS is_return_after_gap
    FROM prevs_active
)
SELECT activity_month,
       SUM(is_return_after_gap) AS returned_after_gap
FROM return_groups
GROUP BY activity_month
HAVING SUM(is_return_after_gap) > 0
ORDER BY activity_month;
```

## Month-over-Month Retention

The Product Manager asks:

> “For each month, what percentage of customers who were active last month are also active this month?”

Consider this activity:

| customer_id | order_date |
| ----------: | ---------- |
|         101 | 2026-01-10 |
|         101 | 2026-02-12 |
|         101 | 2026-03-05 |
|         102 | 2026-01-15 |
|         102 | 2026-03-20 |
|         103 | 2026-02-01 |
|         103 | 2026-03-10 |

```
WITH current_active AS (
    SELECT DISTINCT
           customer_id,
           DATE_TRUNC('month', order_date) AS activity_month
    FROM activities
),
prevs_active AS (
    SELECT customer_id,
           activity_month,
           LAG(activity_month) OVER (
               PARTITION BY customer_id
               ORDER BY activity_month
           ) AS previous_activity_month
    FROM current_active
),
retained_groups AS (
    SELECT customer_id,
           activity_month,
           CASE
               WHEN previous_activity_month + INTERVAL '1 month' = activity_month
               THEN 1
               ELSE 0
           END AS is_retained
    FROM prevs_active
),
retained_count AS (
    SELECT activity_month,
           COUNT(*) AS active_customer,
           SUM(is_retained) AS retained_customer
    FROM retained_groups
    GROUP BY activity_month
),
monthly_retention AS (
    SELECT activity_month,
           active_customer,
           retained_customer,
           LAG(active_customer) OVER (
               ORDER BY activity_month
           ) AS previous_customer
    FROM retained_count
)
SELECT activity_month,
       retained_customer,
       previous_customer,
       ROUND(
           retained_customer::numeric / previous_customer,
           2
       ) AS retention_rate
FROM monthly_retention
WHERE previous_customer IS NOT NULL
ORDER BY activity_month;
```
