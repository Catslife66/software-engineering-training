## Problem

Using the same dataset, return:

| customer_name | avg_items_per_order |

Where:

avg_items_per_order = average number of items per order for each customer
only include customers who have at least 1 order
round to 2 decimal places
sort by avg_items_per_order DESC

## Hint
For each customer:
```
total items = SUM(quantity)
number of orders = COUNT(DISTINCT order_id)
average = total_items / number_of_orders
```

## Answer
```
SELECT c.name, 
       ROUND(SUM(i.quantity) * 1.0 / COUNT(DISTINCT o.order_id), 2) AS avg_items_per_order
FROM customers c
JOIN orders o ON o.customer_id = c.customer_id
JOIN order_items i ON i.order_id = o.order_id
GROUP BY c.customer_id, c.name
ORDER BY avg_items_per_order DESC;
```


Why * 1.0?

In some databases, dividing two integers can produce integer division.

Example: 3 / 2 = 1 instead of 1.5.

So * 1. 0 forces decimal division.