## Problem

Using the same dataset, return:

| customer_name | number_of_orders |

Where:

count the number of distinct orders per customer
include customers with zero orders
sort by number_of_orders DESC, then customer_name ASC

## Answer
```
SELECT c.name AS customer_name, COUNT(DISTINCT o.order_id) AS number_of_orders
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
GROUP BY c.customer_id, c.name
ORDER BY number_of_orders DESC, customer_name ASC;
```