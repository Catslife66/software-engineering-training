## Question 1

Return:

| customer_id | total_items |

Where:

- total_items = total quantity per customer
- sort by total_items DESC

```
SELECT o.customer_id, SUM(i.quantity) AS total_items
FROM orders o
JOIN order_items i ON o.order_id = i.order_id
GROUP BY o.customer_id
ORDER BY total_items DESC;
```

## Question 2

Return:

| customer_id | number_of_orders |

Where:

- count DISTINCT orders
- include customers with zero orders (assume customers table exists)
- sort DESC

```
SELECT c.customer_id, COUNT(DISTINCT o.order_id) AS number_of_orders
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
GROUP BY c.customer_id
ORDER BY number_of_orders DESC;
```

## Question 3

Return:

| customer_id | avg_items_per_order |

Where:

- avg = total_items / number_of_orders
- only include customers with at least 1 order

```
// 1-step approach
SELECT o.customer_id,
       ROUND(SUM(i.quantity) * 1.0 / COUNT(DISTINCT o.order_id), 2) AS avg_items_per_order
FROM orders o
JOIN order_items i
  ON i.order_id = o.order_id
GROUP BY o.customer_id
ORDER BY avg_items_per_order DESC;

// 2-step approach
WITH order_items_total AS (
    SELECT o.customer_id, o.order_id, SUM(i.quantity) AS total_items
    FROM orders o
    JOIN order_items i ON o.order_id = i.order_id
    GROUP BY o.customer_id, o.order_id
)
SELECT ot.customer_id, ROUND(AVG(ot.total_items)*1.0, 2) AS avg_items_per_order
FROM order_items_total ot
GROUP BY ot.customer_id
ORDER BY avg_items_per_order;
```

## Question 4

Return:

| customer_id | max_items_in_a_single_order |

Where:

- for each customer, find the largest total quantity in any one order
- sort by max_items_in_a_single_order DESC

```
WITH order_totals AS (
  SELECT order_id, SUM(quantity) AS order_qty_sum
  FROM order_items
  GROUP BY order_id
)
SELECT o.customer_id, MAX(ot.order_qty_sum) AS max_items_in_a_single_order
FROM order_totals ot
JOIN orders o ON o.order_id = ot.order_id
GROUP BY o.customer_id
ORDER BY max_items_in_a_single_order DESC;
```

## Question 5

Return:

| customer_name | total_items |

Where:

- include all customers
- only show customers whose total_items >= 3
- customers with no orders should count as 0
- sort by total_items DESC

```
SELECT c.name AS customer_name, COALESCE(SUM(i.quantity), 0) AS total_items
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
LEFT JOIN order_items i ON i.order_id = o.order_id
GROUP BY c.customer_id, c.name
HAVING COALESCE(SUM(i.quantity), 0) >= 3
ORDER BY total_items DESC;
```
