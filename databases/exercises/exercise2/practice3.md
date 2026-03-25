## Problem

Using the same dataset, return:

| customer_name | product_name | total_quantity |

Where:

total_quantity = how many times that customer bought that product
include only combinations where total_quantity > 0
sort by:
customer_name ASC
total_quantity DESC

## Hint

You’ll need:

```
customers
JOIN orders
JOIN order_items
JOIN products
GROUP BY (customer, product)
```

## Answer

```
SELECT c.name AS customer_name
       p.product_name,
       SUM(i.quantity) AS total_quantity
FROM customers c
JOIN orders o ON o.customer_id = c.customer_id
JOIN order_items i ON i.order_id = o.order_id
JOIN products p ON p.product_id = i.product_id
GROUP BY c.customer_id, c.name, p.product_id, p.product_name
HAVING SUM(i.quantity) > 0
ORDER BY c.name ASC, total_quantity DESC;
```
