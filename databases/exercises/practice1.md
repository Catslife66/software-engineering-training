Exercise 1
Select all columns from the customers table.

SELECT \* FROM customers;

Exercise 2
Select only name and city from customers.

SELECT name, city FROM customers;

Exercise 3
Select all customers who live in London.

SELECT \* FROM customers
WHERE city = 'London';

Exercise 4
Select all products with price greater than 30.

SELECT \* FROM products
WHERE price > 30;

Exercise 5
Select all products ordered by price from highest to lowest.

SELECT \* FROM products
ORDER BY price DESC;

Exercise 6
Select all orders where quantity is greater than or equal to 2.

SELECT \* FROM orders
WHERE quantity >= 2;
