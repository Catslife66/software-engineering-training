# Interview Pattern Drills

## The 6 Most Important SQL Interview Patterns

| Pattern              | Example             |
| -------------------- | ------------------- |
| Aggregation          | totals per customer |
| Group vs global      | above average       |
| Top-N-per-group      | highest salary      |
| Ranking              | 2nd highest         |
| Running calculations | cumulative totals   |
| Gap/island patterns  | consecutive dates   |

## Pattern 1 — Top N Per Group

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

### Drill 1A

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

### Drill 1B

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

### Drill 1C — Second Highest Per Group

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

### Drill 1D — Top 2 Per Group

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

### A reusable interview pattern

| Problem                       | Pattern             |
| ----------------------------- | ------------------- |
| highest per group, keep ties  | `RANK() = 1`        |
| highest per group, one winner | `ROW_NUMBER() = 1`  |
| second highest                | `DENSE_RANK() = 2`  |
| top N per group               | `DENSE_RANK() <= N` |

## Pattern 2 — Running Totals

**Dataset**

salse

| sale_id | salesperson | sale_date  | amount |
| ------- | ----------- | ---------- | -----: |
| 1       | Alice       | 2025-01-01 |    100 |
| 2       | Alice       | 2025-01-02 |    200 |
| 3       | Alice       | 2025-01-03 |     50 |
| 4       | Bob         | 2025-01-01 |    300 |
| 5       | Bob         | 2025-01-02 |    100 |

### Drill 2A

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

### Drill 2B — Moving Average

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

### Drill 2C — Previous Row Comparison (VERY common)

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

### Drill 2D — Difference from Previous Row

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

### Drill 2E - Gap Between Dates

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-05 |
| 1       | 2025-01-06 |

Return:

| user_id | login_date | days_since_previous |

Where:

- calculate number of days since previous login

```
SELECT user_id, login_date,
    (
        login_date -
        LAG(login_date) OVER (
            PARTITION BY user_id
            ORDER BY login_date ASC
        )
    ) AS days_since_previous
FROM logins;
```

## Pattern 3 - Conditional Aggregation

**Dataset**

orders

| order_id | customer_id | status    |
| -------- | ----------- | --------- |
| 1        | 1           | completed |
| 2        | 1           | pending   |
| 3        | 2           | completed |
| 4        | 2           | cancelled |
| 5        | 2           | completed |

### Drill 3A

Return:

| customer_id | completed_orders |

Where:

- count only completed orders per customer

**New Pattern**

Think:

```
SUM(
    CASE WHEN ... THEN 1 ELSE 0 END
)
```

because:

```
true → 1
false → 0
```

```
SELECT customer_id,
       SUM(
           CASE
               WHEN status = 'completed'
               THEN 1
               ELSE 0
           END
       ) AS completed_orders
FROM orders
GROUP BY customer_id;
```

### Drill 3B

Return:

| customer_id | completed_orders | pending_orders |

```
SELECT customer_id,
    SUM(
        CASE
            WHEN status = 'completed'
            THEN 1
            ELSE 0
        END
    ) AS completed_orders,
    SUM(
        CASE
            WHEN status = 'pending'
            THEN 1
            ELSE 0
        END
    ) AS pending_orders
FROM orders
GROUP BY customer_id;
```

### Common interview pattern

You will often see:

```
SUM(CASE WHEN ... THEN 1 ELSE 0 END)
```

or:

```
COUNT(CASE WHEN ... THEN 1 END)
```

## Mixed Pattern 1

**Dataset**

orders

| order_id | customer_id | status    | amount |
| -------- | ----------- | --------- | -----: |
| 1        | 1           | completed |    100 |
| 2        | 1           | pending   |     50 |
| 3        | 2           | completed |    200 |
| 4        | 2           | completed |    150 |
| 5        | 3           | cancelled |    300 |

### Drill A

Return:

| customer_id | completed_total | ranking |

Where:

- completed_total = total amount from completed orders only
- rank customers by completed_total descending
- keep ties

```
WITH customer_amount AS (
    SELECT customer_id,
    SUM(
        CASE
            WHEN status = 'completed'
            THEN amount
            ELSE 0
        END
    ) AS completed_total
    FROM orders
    GROUP BY customer_id
)
SELECT c.customer_id, c.completed_total,
    RANK() OVER (
        ORDER BY c.completed_total
    ) AS rank
FROM customer_amount c;
```

### Drill B

**Dataset**

transactions

| transaction_id | user_id | type       | amount |
| -------------- | ------- | ---------- | -----: |
| 1              | 1       | deposit    |    100 |
| 2              | 1       | withdrawal |     50 |
| 3              | 1       | deposit    |    200 |
| 4              | 2       | deposit    |    300 |
| 5              | 2       | withdrawal |    100 |
| 6              | 3       | withdrawal |     50 |

Return:

| user_id | deposit_total | rank |

Where:

- deposit_total = total deposited amount only
- rank users by deposit_total descending
- keep ties
- exclude users with deposit_total = 0

```
WITH total AS (
    SELECT user_id,
        SUM(
            CASE
                WHEN type = 'deposit'
                THEN amount
                ELSE 0
            END
        ) AS deposit_total
    FROM transactions
    GROUP BY user_id
),
ranked AS (
    SELECT user_id, deposit_total,
        RANK() OVER (
            ORDER BY deposit_total DESC
        ) AS rank
    FROM total
    WHERE deposit_total > 0
)
SELECT user_id, deposit_total, rank
FROM ranked
ORDER BY rank, user_id;
```

## Mixed Pattern 2

**Dataset**

orders

| order_id | customer_id | amount |
| -------- | ----------- | -----: |
| 1        | 1           |    100 |
| 2        | 1           |    200 |
| 3        | 2           |     50 |
| 4        | 2           |     60 |
| 5        | 3           |    500 |
| 6        | 4           |     20 |

🎯 Problem

Return:

| customer_id | total_spent | rank |

Where:

- total_spent = total order amount per customer
- only include customers who spent MORE than the average customer spending
- rank remaining customers by total_spent descending
- keep ties

Solution:

```
WITH total AS (
  SELECT customer_id,
         SUM(amount) AS total_spent
  FROM orders
  GROUP BY customer_id
),
total_avg AS (
  SELECT AVG(total_spent) AS avg_spent
  FROM total
)
SELECT t.customer_id,
       t.total_spent,
       RANK() OVER (
         ORDER BY t.total_spent DESC
       ) AS rank
FROM total t
CROSS JOIN total_avg a
WHERE t.total_spent > a.avg_spent
ORDER BY rank;
```

OR

```
WITH total AS (
  SELECT customer_id, SUM(amount) AS total_spent
  FROM orders
  GROUP BY customer_id
)
SELECT customer_id,
       total_spent,
       RANK() OVER (ORDER BY total_spent DESC) AS rank
FROM total
WHERE total_spent > (
  SELECT AVG(total_spent)
  FROM total
)
ORDER BY rank;
```
