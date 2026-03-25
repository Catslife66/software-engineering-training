## Problem

Using the same dataset, return:

| product_name | total_quantity_sold |

Where:

total_quantity_sold = total quantity sold for each product
include products that were never ordered
sort by total_quantity_sold DESC

## Answer

```
SELECT p.product_name,
       COALESCE(SUM(i.quantity), 0) AS total_quantity_sold
FROM products p
LEFT JOIN order_items i ON i.product_id = p.product_id
GROUP BY p.product_id, p.product_name
ORDER BY total_quantity_sold DESC;
```
