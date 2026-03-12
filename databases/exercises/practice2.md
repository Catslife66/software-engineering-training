## Exercise 7

Join orders and customers to show:

- order_id
- customer name
- quantity

SELECT orders.order_id, customers.name, orders.quantity
FROM orders
JOIN customers
ON orders.customer_id = customers.customer_id;

## Exercise 8

Join orders and products to show:

- order_id
- product_name
- quantity

SELECT order_id, product_name, quantity FROM orders
JOIN products ON products.product_id = orders.product_id;

## Exercise 9

Join orders + customers + products to show:

- customer name
- product_name
- quantity

Goal output:
| customer | product | quantity |
| -------- | -------- | -------- |
| Alice | Keyboard | 1 |
| Alice | Mouse | 2 |
| Bob | Monitor | 1 |
| Carol | Mouse | 1 |

SELECT customers.name AS customer,
products.product_name AS product,
orders.quantity
FROM customers
INNER JOIN orders
ON orders.customer_id = customers.customer_id
INNER JOIN products
ON products.product_id = orders.product_id;

**Important SQL Engineering Habit**
FROM table
we normally start from the table that represents the main entity of the query.

Instead of query from customers, query from orders

## Exercise 10

Show:

- customer name
- order_id
  Include all customers, even those without orders.

SELECT customers.name, orders.order_id
FROM customers
LEFT JOIN orders
ON customers.customer_id = orders.customer_id;

**customers is the left table. So this query keeps all customers.**

## Exercise 11

Show:

- customer name
- product_name
- quantity
  Include customers even if they never ordered anything.

SELECT customers.name, products.product_name, orders.quantity
FROM customers
LEFT JOIN orders
ON customers.customer_id = orders.customer_id
LEFT JOIN products
ON orders.product_id = products.product_id;
