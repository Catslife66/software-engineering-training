# Pattern 1 — Top N Per Group

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

## Drill 1A

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

## Drill 1B

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

## Drill 1C — Second Highest Per Group

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

## Drill 1D — Top 2 Per Group

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
