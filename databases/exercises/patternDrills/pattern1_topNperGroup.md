# Pattern 1 — Top N Per Group

“Top 2 customers, keep ties” usually means RANK() <= 2, because it keeps ties within the top 2 positions.

“Top 2 spending levels” means DENSE_RANK() <= 2.

**Dataset**

sales

| sale_id | salesperson | region | amount |
| ------- | ----------- | ------ | -----: |
| 1       | Alice       | North  |    100 |
| 2       | Alice       | North  |    200 |
| 3       | Bob         | North  |    200 |
| 4       | Carol       | South  |    150 |
| 5       | David       | South  |    300 |
| 6       | Eve         | South  |    300 |

## Drill 1 - highest per group keep ties

Return:

| region | salesperson | amount |

Where:

- return the salesperson with the highest sale amount per region
- if tie → return ALL tied salespeople

```
SELECT t.region, t.salesperson, t.amount FROM (
    SELECT s.region, s.salesperson, s.amount,
    RANK() OVER (
        PARTITION BY s.region
        ORDER BY amount DESC
    ) AS r
    FROM sales s
) t
WHERE t.r = 1;
```

## Drill 2 - aggragate then highest per group keep ties

Return

| region | salesperson | total_sales |

Where:

- return the top salesperson in each region based on total sales.
- If totals tie, keep both salespeople.

```
WITH sales_total AS (
    SELECT salesperson,
           region,
           SUM(amount) AS total_sales
    FROM sales
    GROUP BY salesperson, region
),
sales_ranking AS (
    SELECT salesperson,
           region,
           total_sales,
           RANK() OVER (
               PARTITION BY region
               ORDER BY total_sales DESC
           ) AS rank
    FROM sales_total
)
SELECT salesperson,
       region,
       total_sales
FROM sales_ranking
WHERE rank = 1;
```

## Drill 3 - highest per group with no ties

Return:

| region | salesperson | amount |

Where:

- return only ONE salesperson per region
- if tie → choose alphabetically smallest salesperson name

```
SELECT t.region, t.salesperson, t.amount
FROM (
    SELECT s.region, s.salesperson, s.amount,
        ROW_NUMBER() OVER (
            PARTITION BY s.region
            ORDER BY s.amount DESC, s.salesperson ASC
        ) AS rn
    FROM sales s
) t
WHERE t.rn = 1;
```

## Drill 4 — Second Highest Per Group

Return:

| region | salesperson | amount |

Where:

- return the SECOND highest sale amount per region
- if ties exist at second place → return all tied rows

```
SELECT t.region, t.salesperson, t.amount
FROM (
    SELECT s.region, s.salesperson, s.amount,
        DENSE_RANK() OVER (
            PARTITION BY s.region
            ORDER BY s.amount DESC
        ) AS dr
    FROM sales s
) t
WHERE t.dr = 2;
```

## Drill 5 — Top 2 Per Group

Return:

| region | salesperson | amount |

Where:

- return the TOP 2 sale amounts per region
- keep ties

```
SELECT t.region, t.salesperson, t.amount
FROM (
    SELECT s.region, s.salesperson, s.amount,
        DENSE_RANK() OVER (
            PARTITION BY s.region
            ORDER BY s.amount DESC
        ) AS dr
    FROM sales s
) t
WHERE t.dr <= 2;
```

## A reusable interview pattern

| Problem                       | Pattern             |
| ----------------------------- | ------------------- |
| highest per group, keep ties  | `RANK() = 1`        |
| highest per group, one winner | `ROW_NUMBER() = 1`  |
| second highest                | `DENSE_RANK() = 2`  |
| top N per group               | `DENSE_RANK() <= N` |
