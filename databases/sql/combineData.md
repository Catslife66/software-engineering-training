## Combining Data

## JOIN

A join connects tables using related columns.

Example:
orders.customer_id = customers.customer_id

```
SELECT *
FROM orders
JOIN customers
ON orders.customer_id = customers.customer_id;
```

## INNER JOIN

An inner join returns **only rows that match in both tables**.

```
SELECT orders.id, users.name AS customer, products.name AS product, orders.quantity
FROM orders
INNER JOIN users ON orders.user_id = users.id
INNER JOIN products ON orders.product_id = products.id;
```

## LEFT JOIN

Gets **all rows from the left table** (even if no match).
For example, if a user has no orders, you can still see them. And it shows NULL in the column order_id.

LEFT JOIN keeps ALL rows from the LEFT table.
`SELECT... FROM xxx, xxx is the LEFT table`

```
SELECT customers.name, orders.order_id
FROM customers
LEFT JOIN orders
ON customers.customer_id = orders.customer_id;
```

## RIGHT JOIN

Opposite of LEFT JOIN (all from right table).

RIGHT JOIN keeps ALL rows from the RIGHT table.
`SELECT... FROM xxx, xxx is the RIGHT table`

## FULL JOIN

Everything from both, even if no match (not all databases support FULL JOIN).

## NULL

NULL means: **no value / unknown / missing data**

Important SQL rule:

```
NULL ≠ 0
NULL ≠ ""
NULL ≠ false
```

### table prefix

1. Readability

When queries become large (10+ joins), this is much easier to understand.

2. Prevents future bugs

If another developer later adds a column with the same name, the query suddenly breaks.

3. Common professional style

Large production queries almost always qualify columns.

### aliases

You should always use the alias everywhere.

```
SELECT u.name
FROM users u
JOIN orders o
ON u.id = o.user_id;
```
