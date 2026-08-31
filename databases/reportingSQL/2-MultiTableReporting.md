# Multi-table Reporting

Suppose our e-commerce schema is:

```
customers
────────────────────────
customer_id  PK
name
country


orders
────────────────────────
order_id     PK
customer_id  FK
order_date
salesperson
region


order_items
────────────────────────
order_id     FK
product_id   FK
quantity
unit_price


products
────────────────────────
product_id   PK
product_name
category
```

Relationships:

```
customers
    │
    │ 1:N
    ▼
orders
    │
    │ 1:N
    ▼
order_items
    │
    │ N:1
    ▼
products
```

Now imagine:

> “Show monthly revenue by product category.”

We need:

```
month
→ orders.order_date

category
→ products.category

revenue
→ order_items.quantity × order_items.unit_price
```

Therefore we need a path connecting those facts:

```
orders
  │ order_id
  ▼
order_items
  │ product_id
  ▼
products
```

No single table contains the answer.

Notice something important:

We don't need customers.

A common beginner habit is:

> “These four tables are related, so join all four.”

An engineer asks:

> **Which tables contain information required to answer this particular question?**

## Report Requirements → Data Requirements

Before joins, break the business request into pieces.

For:

> “Show monthly revenue by product category.”

Grain

```
one category in one month
```

Required information

```
month
category
revenue
```

Locate each piece

```
month
→ orders.order_date

category
→ products.category

revenue
→ order_items.quantity
  + order_items.unit_price
```

Required tables

```
orders
order_items
products
```

Join path

```
orders
  ↓ order_id
order_items
  ↓ product_id
products
```

Only now should SQL enter the picture.

## The Join Changes Grain

This is one of the most important ideas in this module.

Suppose:

orders

| order_id | customer_id |
| -------: | ----------: |
|        1 |         101 |
|        2 |         102 |

Grain:

```
one row = one order
```

order_items

| order_id | product_id | quantity |
| -------: | ---------: | -------: |
|        1 |         10 |        2 |
|        1 |         11 |        1 |
|        2 |         12 |        1 |

Grain:

```
one row = one order line
```

After:

```
FROM orders o
JOIN order_items oi
  ON oi.order_id = o.order_id
```

we get:

| order_id | customer_id | product_id |
| -------: | ----------: | ---------: |
|        1 |         101 |         10 |
|        1 |         101 |         11 |
|        2 |         102 |         12 |

The joined dataset's grain is now approximately:

> one order-item line

The order information has been repeated.

This explains why:

```
COUNT(*)
```

now returns 3, while there are only 2 orders.

So whenever you join a one-to-many relationship, ask:

> **Did this join multiply rows from the left table?**

That question prevents a large class of reporting bugs.

## Measures and Their Natural Grain

Different metrics naturally belong at different grains.

For example:

Order-item grain

```
quantity × unit_price
→ line revenue
```

Order grain

```
SUM(line revenue)
→ order value
```

Customer grain

```
SUM(order values)
→ customer lifetime revenue
```

Category-month grain

```
SUM(line revenue)
→ category monthly revenue
```

The same underlying data can therefore be transformed:

```
order item
    ↓
order
    ↓
customer

or

order item
    ↓
category-month
```

depending on the business question.

There is no universally correct aggregation path.

## Join First or Aggregate First?

This is where multi-table reporting becomes more interesting.

Suppose we want:

> “Show each customer's total lifetime revenue.”

We can join:

```
customers
    ↓
orders
    ↓
order_items
```

and aggregate at customer grain.

Fine.

But suppose we want:

> “Show each customer's average order value.”

Now blindly aggregating line-item rows at customer grain would be dangerous.

We need:

```
order_items
    ↓
one order
calculate order_value
    ↓
orders
    ↓
one customer
AVG(order_value)
```

Why?

Because:

```
average line-item value
≠
average order value
```

So sometimes the correct pattern is:

> Aggregate first → then join/aggregate again.

This is one of the reasons grain matters more than memorising join syntax.

## Entity Identity vs Display Information

Another useful pattern:

Suppose we want:

> “Revenue by customer.”

We should generally identify the customer using:

```
customer_id
```

rather than assuming:

```
name
```

is unique.

Two customers might both be:

```
John Smith
```

So a report might use:

```
GROUP BY c.customer_id, c.name
```

Conceptually:

```
customer_id
→ identity

name
→ display attribute
```

This distinction becomes important in real schemas.

## Filtering Across Tables

A report may use one table for its metric and another for its filtering condition.

Example:

> “Revenue from Electronics products during July.”

Required information:

```
revenue
→ order_items

category
→ products

date
→ orders
```

Therefore:

```
products.category = 'Electronics'
orders.order_date = July
```

Both filters define the population, even though neither category nor order_date is necessarily part of the final output.

That's another useful distinction:

> **A column can be required for filtering without being part of the final grain.**

## A Reusable Multi-Table Reporting Framework

For any multi-table report:

```
1. BUSINESS QUESTION
        ↓
2. FINAL GRAIN
        ↓
3. METRICS
        ↓
4. REQUIRED INFORMATION
        ↓
5. WHICH TABLE OWNS EACH PIECE?
        ↓
6. JOIN PATH
        ↓
7. WHAT GRAIN EXISTS AFTER EACH JOIN?
        ↓
8. DO I NEED AN INTERMEDIATE AGGREGATION?
        ↓
9. FILTER POPULATION
        ↓
10. FINAL AGGREGATION
```

The new parts compared with Module 2.1 are mainly:

```
Which table owns the information?

How do I connect those tables?

What happens to grain when I connect them?
```

## Worked Reasoning Example

Business request:

> “For each country, show total revenue and number of unique customers who placed an order in July.”

Don't start with joins.

Final grain

```
one country
```

Metrics

```
total revenue
unique purchasing customers
```

Required information

```
country
customer identity
order date
line revenue
```

Where does it live?

```
country
→ customers

customer identity
→ customers / orders

order date
→ orders

revenue
→ order_items
```

Therefore:

```
customers
    ↓ customer_id
orders
    ↓ order_id
order_items
```

`products` isn't required.

After joining, however:

```
one order
→ potentially many order-item rows
```

Therefore customer IDs and order IDs can repeat.

Revenue is fine:

```
SUM(oi.quantity * oi.unit_price)
```

Unique customers require:

```
COUNT(DISTINCT c.customer_id)
```

That reasoning is more important than the final query.

## Exercise

Business request:

> “For each product category, show the total revenue, number of unique orders, and number of unique customers during July 2026.”

Schema:

```
customers
customer_id
name
country

orders
order_id
customer_id
order_date

order_items
order_id
product_id
quantity
unit_price

products
product_id
product_name
category
```

```
1. Final grain:
One category.

2. Metrics:
total revenue
unique orders
unique customers

3. Required information and which table each piece comes from
category      → products
customer_id   → orders
order_id      → orders / order_items
order_date    → orders
quantity      → order_items
unit_price    → order_items

4. Which tables are actually required
orders
order_items
products

5. Join path
orders
    ↓ order_id
order_items
    ↓ product_id
products

6. After all those joins, what does approximately one row of the joined dataset represent?
one order-item line, enriched with its order and product information.
```

```
SELECT
    p.category,
    SUM(oi.quantity*oi.unit_price) AS total_revenue,
    COUNT(DISTINCT o.order_id) AS unique_orders,
    COUNT(DISTINCT o.customer_id) AS unique_customers
FROM orders o
JOIN order_items oi ON oi.order_id = o.order_id
JOIN products p ON p.product_id = oi.product_id
WHERE o.order_date >= DATE '2026-07-01'
    AND o.order_date < DATE '2026-08-01'
GROUP BY p.category;
```

## Joining can distort metrics

Suppose the manager changes the request to:

> “For each category, show total revenue and average order value during July.”

The revenue part is still easy.

But now think carefully about average order value.

One order might contain products from multiple categories.

For example:

| order_id | category    | line_revenue |
| -------: | ----------- | -----------: |
|        1 | Accessories |           50 |
|        1 | Displays    |          300 |

Suppose the July data after joining looks like this:

| order_id | category    | quantity | unit_price |
| -------: | ----------- | -------: | ---------: |
|        1 | Accessories |        1 |        £50 |
|        1 | Displays    |        1 |       £300 |
|        2 | Accessories |        2 |        £40 |
|        2 | Accessories |        1 |        £20 |
|        3 | Displays    |        1 |       £200 |

```
WITH category_orders AS (
    SELECT oi.order_id,
           p.category,
           SUM(oi.quantity * oi.unit_price) AS category_order_value
    FROM order_items oi
    JOIN products p
      ON p.product_id = oi.product_id
    JOIN orders o
      ON o.order_id = oi.order_id
    WHERE o.order_date >= DATE '2026-07-01'
      AND o.order_date <  DATE '2026-08-01'
    GROUP BY oi.order_id, p.category
)
SELECT category,
       SUM(category_order_value) AS total_revenue,
       ROUND(AVG(category_order_value), 2) AS average_category_order_value
FROM category_orders
GROUP BY category;
```

## Why join direction and relationship type matter

Suppose the Product Manager asks:

> “For each customer, show total revenue in July, including customers who placed no orders.”

Schema:

```
customers
customer_id
name

orders
order_id
customer_id
order_date

order_items
order_id
product_id
quantity
unit_price
```

Expected shape:

| customer_id | name    | july_revenue |
| ----------: | ------- | -----------: |
|         101 | Alice   |          500 |
|         102 | Bob     |          200 |
|         103 | Charlie |            0 |

```
Population
→ all customers

Scope
→ completed orders during July

Final grain
→ one customer

Source grains
→ customers: one customer
→ orders: one order
→ order_items: one order line

Metric
→ average order value
```

```
SELECT
    c.customer_id,
    c.name,
    COALESCE(
        SUM(oi.quantity * oi.unit_price),
        0
    ) AS july_revenue
FROM customers c
LEFT JOIN orders o
  ON o.customer_id = c.customer_id
 AND o.order_date >= DATE '2026-07-01'
 AND o.order_date <  DATE '2026-08-01'
LEFT JOIN order_items oi
  ON oi.order_id = o.order_id
GROUP BY c.customer_id, c.name
ORDER BY c.customer_id;
```

To get average order value for each customer:

```
WITH order_values AS (
    SELECT
        o.order_id,
        o.customer_id,
        SUM(oi.quantity * oi.unit_price) AS order_value
    FROM orders o
    JOIN order_items oi
      ON oi.order_id = o.order_id
    WHERE o.order_date >= DATE '2026-07-01'
      AND o.order_date < DATE '2026-08-01'
      AND o.status = 'COMPLETED'
    GROUP BY o.order_id, o.customer_id
)
SELECT c.customer_id,
       c.name,
       COALESCE(AVG(ov.order_value), 0) AS average_order_value
FROM customers c
LEFT JOIN order_values ov
  ON ov.customer_id = c.customer_id
GROUP BY c.customer_id, c.name
ORDER BY c.customer_id;
```

## Excercises

### Drill 1

Problem Card

Business request

The Sales Manager asks:

> For each salesperson, show their total revenue, number of unique orders, average order value, and largest individual order value from completed orders in July 2026. Include salespeople who had no completed orders during July.

Business definitions

- Revenue = sum of quantity × unit_price.
- Number of orders = number of distinct completed July orders.
- Average order value = average value of those individual completed July orders.
- Largest order value = value of the salesperson's largest individual completed July order.
- A salesperson with no qualifying orders should still appear.
- For such a salesperson:
  - revenue = 0
  - number of orders = 0
  - average order value = NULL
  - largest order value = NULL

Schema

```
salespeople
────────────────────
salesperson_id   PK
name
region


orders
────────────────────
order_id         PK
salesperson_id   FK
customer_id      FK
order_date
status


order_items
────────────────────
order_id         FK
product_id       FK
quantity
unit_price
```

Relationships:

```
salespeople
     │
     │ 1:N
     ▼
   orders
     │
     │ 1:N
     ▼
 order_items
```

Required output

```
salesperson_id | name  | total_revenue | order_count | average_order_value | largest_order_value
---------------+-------+---------------+-------------+---------------------+--------------------
1              | Alice | 1200.00       | 4           | 300.00              | 500.00
2              | Bob   | 700.00        | 2           | 350.00              | 450.00
3              | Carol | 0.00          | 0           | NULL                | NULL
```

```
WITH order_summary AS (
    SELECT
        o.order_id,
        o.salesperson_id,
        SUM(oi.quantity * oi.unit_price) AS order_revenue
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.order_id
    WHERE o.order_date >= DATE '2026-07-01'
        AND o.order_date < DATE '2026-08-01'
        AND status = 'COMPLETED'
    GROUP BY o.order_id, o.salesperson_id
)
SELECT
    s.salesperson_id,
    s.name,
    COALESCE(SUM(os.order_revenue), 0) AS total_revenue,
    COUNT(os.order_id) AS order_count,
    ROUND(AVG(os.order_revenue), 2) AS average_order_value,
    MAX(order_revenue) AS largest_order_value
FROM salespeople s
LEFT JOIN order_summary os ON os.salesperson_id = s.salesperson_id
GROUP BY s.salesperson_id, s.name
ORDER BY s.salesperson_id;
```

### Drill 2

Business request

The Product Manager asks:

> For each product category, show July 2026 revenue, unique customers, average category-specific order value, and the largest category-specific order value. Include categories with no July sales.

Business definitions

- Revenue = sum of quantity × unit_price for that category.
- Unique customers = distinct customers who bought at least one product in that category during July.
- Category-specific order value = the value contributed by that category within one order.
- Average category-specific order value = average of those category-order values.
- Largest category-specific order value = maximum category-order value.
- Categories with no qualifying July sales must still appear.
- For those categories:
  - revenue = 0
  - unique customers = 0
  - average category-order value = NULL
  - largest category-order value = NULL

Schema

```
products
────────────────────
product_id      PK
product_name
category


orders
────────────────────
order_id        PK
customer_id
order_date
status


order_items
────────────────────
order_id        FK
product_id      FK
quantity
unit_price
```

Relationships:

```
orders
   │
   │ 1:N
   ▼
order_items
   │
   │ N:1
   ▼
products
```

Required output

```
category | total_revenue | unique_customers | avg_category_order_value | largest_category_order_value
```

```
WITH categories AS (
    SELECT DISTINCT category
    FROM products
),
category_orders AS (
    SELECT
        p.category,
        o.order_id,
        o.customer_id,
        SUM(oi.quantity * oi.unit_price) AS category_order_value
    FROM orders o
    JOIN order_items oi
      ON oi.order_id = o.order_id
    JOIN products p
      ON p.product_id = oi.product_id
    WHERE o.order_date >= DATE '2026-07-01'
      AND o.order_date <  DATE '2026-08-01'
      AND o.status = 'COMPLETED'
    GROUP BY
        p.category,
        o.order_id,
        o.customer_id
)
SELECT
    c.category,
    COALESCE(SUM(co.category_order_value), 0) AS total_revenue,
    COUNT(DISTINCT co.customer_id) AS unique_customers,
    ROUND(AVG(co.category_order_value), 2) AS avg_category_order_value,
    MAX(co.category_order_value) AS largest_category_order_value
FROM categories c
LEFT JOIN category_orders co
  ON co.category = c.category
GROUP BY c.category
ORDER BY c.category;
```

The key: also include categories with no July sales
