# Mock Interview

## Drill 1

**Dataset**

orders

| order_id | customer_id | order_date | amount |
| -------: | ----------: | ---------- | -----: |
|        1 |         101 | Jan 1      |    100 |
|        2 |         101 | Jan 5      |    200 |
|        3 |         101 | Jan 10     |    150 |
|        4 |         102 | Jan 2      |    300 |
|        5 |         102 | Jan 8      |    250 |
|        6 |         103 | Jan 3      |    400 |

Question

For each customer, calculate days_between_orders

Exepcted:

| customer_id | order_date | amount | days_between_orders |
| ----------: | ---------- | -----: | ------------------: |
|         101 | Jan 1      |    100 |                NULL |
|         101 | Jan 5      |    200 |                   4 |
|         101 | Jan 10     |    150 |                   5 |
|         102 | Jan 2      |    300 |                NULL |
|         102 | Jan 8      |    250 |                   6 |
|         103 | Jan 3      |    400 |                NULL |

```
SELECT customer_id,
       order_date,
       amount,
       order_date -
       LAG(order_date) OVER (
           PARTITION BY customer_id
           ORDER BY order_date
       ) AS days_between_orders
FROM orders;
```

## Drill 2

**Dataset**

orders

| customer_id | amount |
| ----------: | -----: |
|         101 |    100 |
|         101 |    200 |
|         102 |    300 |
|         102 |    100 |
|         103 |    400 |
|         104 |     50 |

Question

Return the top 2 customers by total spending.

If there is a tie for second place, keep all tied customers.

Expected:

| customer_id | total_spent |
| ----------: | ----------: |
|         103 |         400 |
|         102 |         400 |

```
WITH order_sum AS (
    SELECT customer_id, SUM(amount) AS total_spent
    FROM orders
    GROUP BY customer_id
),
sum_ranking AS (
    SELECT customer_id, total_spent, RANK() OVER (
        ORDER BY total_spent DESC
    ) AS rank
    FROM order_sum
)
SELECT customer_id, total_spent
FROM sum_ranking
WHERE rank <= 2;
```

## Drill 3

**Dataset**

sales

| salesperson | sale_date | amount |
| ----------- | --------- | -----: |
| Alice       | Jan 1     |    100 |
| Alice       | Jan 5     |    200 |
| Alice       | Jan 10    |    150 |
| Bob         | Jan 2     |    300 |
| Bob         | Jan 8     |    250 |

Question

For each salesperson, return:

- first sale amount
- latest sale amount
- difference between latest and first

Expected:

| salesperson | first_sale | latest_sale | difference |
| ----------- | ---------: | ----------: | ---------: |
| Alice       |        100 |         150 |         50 |
| Bob         |        300 |         250 |        -50 |

```
WITH sales_rank AS (
  SELECT salesperson,
         amount,
         sale_date,
         ROW_NUMBER() OVER (
           PARTITION BY salesperson
           ORDER BY sale_date
         ) AS first_rn,
         ROW_NUMBER() OVER (
           PARTITION BY salesperson
           ORDER BY sale_date DESC
         ) AS last_rn
  FROM sales
),
first_rank AS (
  SELECT salesperson,
         amount AS first_sale
  FROM sales_rank
  WHERE first_rn = 1
),
last_rank AS (
  SELECT salesperson,
         amount AS latest_sale
  FROM sales_rank
  WHERE last_rn = 1
)
SELECT f.salesperson,
       f.first_sale,
       l.latest_sale,
       l.latest_sale - f.first_sale AS difference
FROM first_rank f
JOIN last_rank l
  ON f.salesperson = l.salesperson;
```

Alternative:

```
WITH first_lastest_sales AS (
    SELECT DISTINCT salesperson,
        FIRST_VALUE(amount) OVER (
            PARTITION BY salesperson
            ORDER BY sale_date
        ) AS first_sale,
        FIRST_VALUE(amount) OVER (
            PARTITION BY salesperson
            ORDER BY sale_date DESC
        ) AS latest_sale
    FROM sales;
)
SELECT salesperson,
       first_sale,
       latest_sale,
       latest_sale - first_sale AS difference
FROM first_lastest_sales;
```
