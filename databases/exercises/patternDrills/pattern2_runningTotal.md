# Pattern 2 — Running Totals

**Dataset**

salse

| sale_id | salesperson | sale_date  | amount |
| ------- | ----------- | ---------- | -----: |
| 1       | Alice       | 2025-01-01 |    100 |
| 2       | Alice       | 2025-01-02 |    200 |
| 3       | Alice       | 2025-01-03 |     50 |
| 4       | Bob         | 2025-01-01 |    300 |
| 5       | Bob         | 2025-01-02 |    100 |

## Drill 1

Return:

| salesperson | sale_date | amount | running_total |

Where:

- running_total = cumulative sales amount per salesperson ordered by date

```
SELECT salesperson, sale_date, amount,
    SUM(amount) OVER (
        PARTITION BY salesperson
        ORDER BY sale_date
    ) AS running_total
FROM sales;
```

**New subtle concept: Window Frame**

Your query actually implies this default behavior:

```
ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
```

Meaning:

```
from first row in partition
up to current row
```

That is why the total “runs.”

You do NOT need to memorize this now — just know:

```
ORDER BY inside OVER()
creates cumulative behavior
```

## Drill 2 — Moving Average

Return:

| salesperson | sale_date | amount | moving_avg |

Where:

- moving_avg = average of current row + all previous rows for that salesperson

```
SELECT salesperson, sale_date, amount,
    AVG(amount) OVER (
        PARTITION BY salesperson
        ORDER BY sale_date ASC
    ) AS moving_avg
FROM sales;
```

## Drill 3 — Previous Row Comparison (VERY common)

Return:

| salesperson | sale_date | amount | previous_amount |

Where:

- previous_amount = previous sale amount for the same salesperson
- if no previous row → NULL

```
SELECT salesperson, sale_date, amount,
    LAG(amount) OVER (
        PARTITION BY salesperson
        ORDER BY sale_date ASC
    ) AS previous_amount
FROM sales;
```

## Drill 4 — Difference from Previous Row

Return:

| salesperson | sale_date | amount | diff_from_previous |

Where:

- diff_from_previous = current amount - previous amount

```
SELECT salesperson, sale_date, amount,
    (
        amount -
        LAG(amount) OVER (
            PARTITION BY salesperson
            ORDER BY sale_date ASC
        )
    ) AS diff_from_previous
FROM sales;
```
