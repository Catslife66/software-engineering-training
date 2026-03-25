## Problem

Return:

| customer_name | total_quantity |

Where:

total_quantity = total number of items purchased by the customer
include customers with zero orders
sort by total_quantity DESC

## Hint:

You need to:

1. connect customers → orders
2. connect orders → order_items
3. sum quantity
4. group by customer
5. handle customers with no orders

## Answer:

```
SELECT c.name AS customer_name,
COALESCE(SUM(i.quantity), 0) AS total_quantity
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.customer_id
LEFT JOIN order_items i ON i.order_id = o.order_id
GROUP BY c.customer_id, c.name
ORDER BY total_quantity DESC;
```
