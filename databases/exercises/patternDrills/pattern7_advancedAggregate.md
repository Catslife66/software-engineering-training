# Advanced Aggregate

## Drill 1

**Dataset**

orders

| customer_id | amount |
| ----------- | -----: |
| 101         |    100 |
| 101         |    200 |
| 102         |    500 |
| 102         |    100 |
| 103         |    400 |
| 104         |    700 |

Goal:

Return

| customer_id | total_spent | rank |
| ----------- | ----------: | ---: |
| 104         |         700 |    1 |
| 102         |         600 |    2 |
| 103         |         400 |    3 |

```
WITH order_sum AS (
    SELECT customer_id,
           SUM(amount) AS total_spent
    FROM orders
    GROUP BY customer_id
),
order_ranking AS (
    SELECT customer_id,
           total_spent,
           ROW_NUMBER() OVER (
               ORDER BY total_spent DESC
           ) AS rn
    FROM order_sum
)
SELECT customer_id,
       total_spent
FROM order_ranking
WHERE rn <= 3;
```

## Drill 2 - Top 2 Products Overall

**Dataset**

sales

| product_id | region | amount |
| ---------- | ------ | -----: |
| 1          | North  |    100 |
| 1          | South  |    200 |
| 2          | North  |    500 |
| 3          | South  |    300 |
| 4          | North  |     50 |

Goal:

| product_id | total_sales | rank |
| ---------- | ----------: | ---: |
| 2          |         500 |    1 |
| 1          |         300 |    2 |
| 3          |         300 |    2 |

Return:

- calculate total sales per product
- return top 2 ranks overall
- keep ties

```
WITH product_sales AS (
    SELECT product_id, SUM(amount) AS total_sales
    FROM sales
    GROUP BY product_id
),
sales_ranking AS (
    SELECT product_id, total_sales, DENSE_RANK() OVER(
        ORDER BY total_sales DESC
        ) AS rank
    FROM product_sales
)
SELECT product_id, total_sales, rank
FROM sales_ranking
WHERE rank <= 2;
```

## Drill 3 - Top 2 Products Per Region

Same table

sales

| product_id | region | amount |
| ---------- | ------ | -----: |
| 1          | North  |    100 |
| 1          | South  |    200 |
| 2          | North  |    500 |
| 3          | South  |    300 |
| 4          | North  |     50 |

Goal

| region | product_id | total_sales | rank |
| ------ | ---------: | ----------: | ---: |
| North  |          2 |         500 |    1 |
| North  |          1 |         100 |    2 |
| South  |          3 |         300 |    1 |
| South  |          1 |         200 |    2 |

Return top 2 product ranks inside each region.

- calculate total sales per region + product_id
- rank products within each region
- keep ties

```
WITH region_sales AS (
    SELECT product_id,
           region,
           SUM(amount) AS total_sales
    FROM sales
    GROUP BY product_id, region
),
region_ranking AS (
    SELECT product_id,
           region,
           total_sales,
           DENSE_RANK() OVER (
               PARTITION BY region
               ORDER BY total_sales DESC
           ) AS rank
    FROM region_sales
)
SELECT product_id,
       region,
       total_sales,
       rank
FROM region_ranking
WHERE rank <= 2;
```

## Drill 4 - mixes aggregation and window functions

**Dataset**

orders

| customer_id | amount |
| ----------- | -----: |
| 101         |    100 |
| 101         |    200 |
| 102         |    300 |
| 102         |    100 |
| 103         |    400 |

Question

Return customers whose total spending is above the average customer spending.

Expected

| customer_id | total_spent |
| ----------- | ----------: |
| 102         |         400 |
| 103         |         400 |

```
WITH customer_total AS (
    SELECT customer_id,
           SUM(amount) AS total_spent
    FROM orders
    GROUP BY customer_id
),
customer_avg AS (
    SELECT AVG(total_spent) AS avg_spent
    FROM customer_total
)
SELECT customer_id,
       total_spent
FROM customer_total
WHERE total_spent > (
    SELECT avg_spent
    FROM customer_avg
);
```

## Drill 5

**Dataset**

sales

| salesperson | region | sale_date | amount |
| ----------- | ------ | --------- | -----: |
| Alice       | North  | Jan 1     |    100 |
| Alice       | North  | Jan 5     |    200 |
| Bob         | North  | Jan 2     |    250 |
| Bob         | North  | Jan 8     |    150 |
| Charlie     | South  | Jan 3     |    300 |
| Charlie     | South  | Jan 10    |    350 |

Question

Return the salesperson with the highest total sales in each region.

If totals tie, choose the salesperson whose latest sale happened earliest.

```
WITH sales_sum AS (
    SELECT salesperson,
           region,
           SUM(amount) AS total_sales,
           MAX(sale_date) AS latest_sales
    FROM sales
    GROUP BY salesperson, region
),
sales_ranking AS (
    SELECT salesperson,
           region,
           total_sales,
           ROW_NUMBER() OVER (
               PARTITION BY region
               ORDER BY total_sales DESC,
                        latest_sales ASC
           ) AS rn
    FROM sales_sum
)
SELECT salesperson,
       region,
       total_sales
FROM sales_ranking
WHERE rn = 1;
```
