# Summerise Data

## GROUP BY

group rows by a column then apply COUNT inside each group

When GROUP BY is used:
**Every** column in `SELECT` must be either:

- inside the `GROUP BY`
- inside an aggregate function

```
SELECT customer_id, COUNT(*) AS order_count
FROM orders
GROUP BY customer_id;

```

**Core Idea**
`GROUP BY` splits rows into groups
Aggregate functions summarize each group

## Common aggregate functions

| Function | Purpose        |
| -------- | -------------- |
| COUNT()  | number of rows |
| SUM()    | total          |
| AVG()    | average        |
| MIN()    | smallest value |
| MAX()    | largest value  |

## HAVING

When we want to filter the groups themselves.

```
SELECT product_id, COUNT(*) AS total_orders
FROM orders
GROUP BY product_id
HAVING COUNT(*) > 1;
```

Steps:
1️⃣ rows grouped by product
2️⃣ COUNT applied
3️⃣ HAVING filters the groups

**KEY RULE**
`WHERE` filters rows before grouping
`HAVING` filters groups after grouping

**Execution order:**
FROM
WHERE
GROUP BY
HAVING
SELECT
ORDER BY
