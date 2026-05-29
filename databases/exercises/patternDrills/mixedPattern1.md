# Mixed Pattern 1

**Dataset**

orders

| order_id | customer_id | status    | amount |
| -------- | ----------- | --------- | -----: |
| 1        | 1           | completed |    100 |
| 2        | 1           | pending   |     50 |
| 3        | 2           | completed |    200 |
| 4        | 2           | completed |    150 |
| 5        | 3           | cancelled |    300 |

## Drill A

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

## Drill B

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

# Mixed Pattern 2

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

# Mixed Pattern 3

**Dataset**

sales

| sale_id | salesperson | product | amount |
| ------- | ----------- | ------- | -----: |
| 1       | Alice       | Phone   |    100 |
| 2       | Alice       | Laptop  |    300 |
| 3       | Alice       | Phone   |    200 |
| 4       | Bob         | Tablet  |    150 |
| 5       | Bob         | Tablet  |    100 |
| 6       | Bob         | Phone   |    250 |

Problem

Return:

| salesperson | favorite_product | total_sales |

Where:

- favorite_product = product with highest total sales amount per salesperson
- keep ties

```
WITH product_totals AS (
    SELECT salesperson,
           product,
           SUM(amount) AS product_sales
    FROM sales
    GROUP BY salesperson, product
),
ranked AS (
    SELECT p.salesperson,
           p.product,
           p.product_sales,
           RANK() OVER (
               PARTITION BY p.salesperson
               ORDER BY p.product_sales DESC
           ) AS ra
    FROM product_totals p
)
SELECT salesperson,
       product AS favorite_product,
       product_sales AS total_sales
FROM ranked
WHERE ra = 1;
```

# Mixed Pattern 4

**Dataset**

sales

| sale_id | salesperson | product | amount |
| ------- | ----------- | ------- | -----: |
| 1       | Alice       | Phone   |    100 |
| 2       | Alice       | Laptop  |    300 |
| 3       | Alice       | Phone   |    200 |
| 4       | Bob         | Tablet  |    150 |
| 5       | Bob         | Tablet  |    100 |
| 6       | Bob         | Phone   |    250 |
| 7       | Bob         | Laptop  |     50 |

Problem

Return:

| salesperson | product | product_sales | rank |

Where:

- calculate total sales per salesperson + product
- return the top 2 products per salesperson
- keep ties
- sort by salesperson ASC, rank ASC

```
WITH product_totals AS (
    SELECT salesperson,
           product,
           SUM(amount) AS product_sales
    FROM sales
    GROUP BY salesperson, product
),
ranked AS (
    SELECT p.salesperson,
           p.product,
           p.product_sales,
           DENSE_RANK() OVER (
               PARTITION BY p.salesperson
               ORDER BY p.product_sales DESC
           ) AS rank
    FROM product_totals p
)
SELECT salesperson, product, product_sales, rank
FROM ranked
WHERE rank <= 2
ORDER BY salesperson ASC, rank ASC;
```
