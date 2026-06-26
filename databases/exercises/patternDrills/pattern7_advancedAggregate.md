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
