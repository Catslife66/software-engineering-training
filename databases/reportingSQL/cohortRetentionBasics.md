# Cohort Retention Basics

A cohort is:

> A group of people who share the same starting point.

```
COHORT RETENTION

Question:
"Of customers who originally STARTED together,
how many are active N months later?"

Denominator:
original cohort size
```

Product Manager asks:

> “Of the customers who first ordered in January, how many were still active in February and March?”

orders

| customer_id | order_date |
| ----------: | ---------- |
|         101 | 2026-01-10 |
|         101 | 2026-02-12 |
|         101 | 2026-03-05 |
|         102 | 2026-01-15 |
|         102 | 2026-03-20 |
|         103 | 2026-02-01 |
|         103 | 2026-03-10 |

For customer 101:

```
first month = January
active = January, February, March
```

For customer 102:

```
first month = January
active = January, March
```

For customer 103:

```
first month = February
active = February, March
```

So the January cohort is:

```
{101, 102}
```

and its activity is:

| activity month | active customers from Jan cohort |
| -------------- | -------------------------------: |
| January        |                                2 |
| February       |                                1 |
| March          |                                2 |

Notice the difference from our previous retention report:

```
Month-over-month retention:
"Did February's customers come back in March?"

Cohort analysis:
"What happened over time to the customers
who originally started in January?"
```

```
WITH first_orders AS (
    SELECT customer_id, MIN(order_date) AS first_order
    FROM orders
    GROUP BY customer_id
),
first_months AS (
    SELECT customer_id, DATE_TRUNC('month', first_order) AS first_month
    FROM first_orders
),
cutomer_activities AS (
    SELECT DISTINCT o.customer_id,
        fm.first_month AS cohort_month,
        DATE_TRUNC('month', o.order_date) AS activity_month
    FROM orders o
    JOIN first_months fm ON fm.customer_id = o.customer_id
)
SELECT cohort_month,
    activity_month,
    COUNT(DISTINCT customer_id) AS active_customers
FROM cutomer_activities
GROUP BY cohort_month, activity_month
```

Template:

```
1. Which group did the customer originally join?
   → cohort_month

2. When was that customer active?
   → activity_month

3. Group by cohort_month + activity_month
   → active customers from each cohort at each point in time

4. Find Month 0
   → cohort_month = activity_month
   → original cohort size

5. Compare later activity against original cohort size
   → cohort retention
```
