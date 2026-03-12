Exercise 13

Count how many customers live in each city.

Expected output:

| city | total_customers |

SELECT city, COUNT(\*) AS total_customers
FROM customers
GROUP BY city;

Exercise 14

Count how many orders each customer made.
Show:
customer_id
order_count

SELECT c.customer_id, COUNT(o.customer_id) AS order_count
FROM customers c
LEFT JOIN orders o
ON o.customer_id = c.customer_id
GROUP BY c.customer_id;

Exercise 15

Show:
customer name
total orders

SELECT c.name, COUNT(o.order_id) AS total_orders
FROM customers c
LEFT JOIN orders o
ON o.customer_id = c.customer_id
GROUP BY c.customer_id, c.name;

Exercise 19

Return:
| product_id | total_orders |
Include only products ordered more than once.

SELECT product_id, COUNT(_) AS total_orders
FROM orders
GROUP BY product_id
HAVING COUNT(_) > 1;

Why the alias doesn't work here

SQL processes the query in a conceptual order.
HAVING happens BEFORE SELECT,
But the alias total_orders is created in the SELECT step.

Exercise 20

Return:
| product_id | total_quantity |
Where:
total_quantity = SUM(quantity)

SELECT product_id, SUM(quantity) AS total_quantity
FROM orders
GROUP BY product_id;

Exercise 21 (slightly harder)

Return:
| product_id | total_quantity |
Include only products where total quantity sold > 2.

SELECT product_id, SUM(quantity) AS total_quantity
FROM orders
GROUP BY product_id
HAVING SUM(quantity) > 2;
