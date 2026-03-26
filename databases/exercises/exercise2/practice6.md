## Problem

Using the same dataset, return:

| customer_name | most_recent_order_id |

Where:

show every customer
if a customer has no orders, show NULL
sort by customer_name ASC

## Answer
```
SELECT c.customer_id, c.name AS customer_name, MAX(o.order_id) AS most_recent_order_id
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
GROUP BY c.customer_id, c.name
ORDER BY customer_name ASC;
```