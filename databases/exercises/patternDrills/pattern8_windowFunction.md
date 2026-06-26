# Window Function

**LEAD()** is commonly used for questions like:

- What is the next purchase?
- What is the next login?
- Days until the next order
- Time between current event and next event
- Customer's next subscription

**FIRST_VALUE()** is often used for questions like:

- First purchase amount
- First login date
- First subscription plan
- First order price

## Drill 1 - LEAD()

**Dataset**

orders

| order_id | customer_id | order_date |
| -------- | ----------- | ---------- |
| 1        | 101         | Jan 1      |
| 2        | 101         | Jan 10     |
| 3        | 101         | Jan 20     |
| 4        | 102         | Jan 5      |
| 5        | 102         | Jan 12     |

Return:

| customer_id | order_date | next_order_date | days_until_next |
| ----------- | ---------- | --------------- | --------------: |
| 101         | Jan 1      | Jan 10          |               9 |
| 101         | Jan 10     | Jan 20          |              10 |
| 101         | Jan 20     | NULL            |            NULL |
| 102         | Jan 5      | Jan 12          |               7 |
| 102         | Jan 12     | NULL            |            NULL |

```
WITH next_orders AS (
    SELECT customer_id,
           order_date,
           LEAD(order_date) OVER (
               PARTITION BY customer_id
               ORDER BY order_date
           ) AS next_order_date
    FROM orders
)
SELECT customer_id,
       order_date,
       next_order_date,
       next_order_date - order_date AS days_until_next
FROM next_orders;
```

## Drill 2 - FIRST_VALUE()

**Dataset**

sales

| salesperson | sale_date | amount |
| ----------- | --------- | -----: |
| Alice       | Jan 1     |    100 |
| Alice       | Jan 5     |    200 |
| Alice       | Jan 10    |    150 |
| Bob         | Jan 2     |     80 |
| Bob         | Jan 8     |    120 |

Return:

| salesperson | sale_date | amount | first_sale |

```
SELECT salesperson,
       sale_date,
       amount,
       FIRST_VALUE(amount) OVER (
           PARTITION BY salesperson
           ORDER BY sale_date
       ) AS first_sale
FROM sales;
```
