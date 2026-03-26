## Problem

Return:

| customer_name | last_product_purchased |

Where:

last_product_purchased = product name from the most recent order
if customer has no orders → NULL
sort by customer_name ASC


```
WITH last_order AS (
    SELECT c.customer_id, c.name AS customer_name, MAX(o.order_id) AS last_order_id
    FROM customers c
    LEFT JOIN orders o ON o.customer_id = c.customer_id
    GROUP BY c.customer_id, c.name
)
SELECT lo.customer_name, p.product_name AS last_product_purchased
FROM last_order lo
LEFT JOIN order_items i ON i.order_id = lo.order_id
LEFT JOIN products p ON p.product_id = i.product_id
WHERE i.product_id = (
    SELECT MAX(oi.product_id) 
    FROM order_items oi
    WHERE oi.order_id = lo.last_order_id
) OR lo.last_order_id IS NULL
ORDER BY lo.customer_name ASC;
```