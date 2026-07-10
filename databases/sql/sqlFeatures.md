# SQL Features

## EXISTS

Problem

Tables:

customers

| customer_id | name    |
| ----------- | ------- |
| 101         | Alice   |
| 102         | Bob     |
| 103         | Charlie |

orders

| order_id | customer_id |
| -------- | ----------- |
| 1        | 101         |
| 2        | 101         |
| 3        | 103         |

Question

Return customers who have placed at least one order.

Expected:

| customer_id | name    |
| ----------- | ------- |
| 101         | Alice   |
| 103         | Charlie |

Solution without `EXISTS` would look like

```
WITH order_counts AS (
    SELECT customer_id,
           COUNT(*) AS order_count
    FROM orders
    GROUP BY customer_id
)
SELECT c.customer_id,
       c.name
FROM customers c
JOIN order_counts o
  ON c.customer_id = o.customer_id
WHERE o.order_count >= 1;
```

But do we actually care how many orders Alice has?

No.

The business question is:

```
Has Alice placed at least one order?
```

Not:

```
How many orders has Alice placed?
```

**This is exactly why EXISTS was invented.**

Instead of saying:

```
Count everything.
```

we say:

```
Tell me whether one exists.
```

Conceptually:

```
SELECT customer_id,
       name
FROM customers c
WHERE EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.customer_id = c.customer_id
);
```

### A Rule That Will Save You in Interviews

Whenever you read a question, ask yourself:

> Is the answer a number or a yes/no?

For example:

| Business Question | Natural SQL Tool |
| ----------------- | ---------------- |
| Total sales       | `SUM()`          |
| Number of orders  | `COUNT()`        |
| Highest salary    | `MAX()`          |
| Has an order?     | `EXISTS`         |
| Never logged in?  | `NOT EXISTS`     |
| Has a manager?    | `EXISTS`         |
| Has no manager?   | `NOT EXISTS`     |

```
What is the question asking?
│
├── A number?
│     ├── SUM
│     ├── COUNT
│     ├── AVG
│     └── GROUP BY
│
├── A yes/no?
│     ├── EXISTS
│     └── NOT EXISTS
│
├── Compare to previous?
│     ├── LAG
│
├── Compare to next?
│     ├── LEAD
│
├── Need exactly one row?
│     ├── ROW_NUMBER
│
├── Keep ties?
│     ├── RANK
│     └── DENSE_RANK
│
├── Keep original rows?
│     ├── Window function
│
├── Collapse rows?
│     ├── GROUP BY
│
└── Same table relationship?
      ├── Self Join
```

## EXISTS vs IN

Tables

customers

| customer_id | name    |
| ----------: | ------- |
|         101 | Alice   |
|         102 | Bob     |
|         103 | Charlie |
|         104 | David   |

orders

| order_id | customer_id |
| -------: | ----------: |
|        1 |         101 |
|        2 |         101 |
|        3 |         103 |

Question

Return customers who have placed an order.

**If I'm checking whether a value belongs to a known set**

I naturally reach for:

```
IN
```

Example:

```
WHERE department_id IN (10, 20, 30)
```

or

```
WHERE country IN ('UK', 'US', 'CA')
```

**If I'm checking whether a related row exists**

I naturally reach for:

```
EXISTS
```

Example:

```
Customers who have orders
Employees who have direct reports
Products that have reviews
Users who have logged in today
```

Those are all relationship questions.

### This is the distinction I use professionally

I almost never think:

```
IN vs EXISTS
```

Instead I think:

```
Am I checking

a VALUE?

or

a RELATIONSHIP?
```

**Value**

```
Country
Department
Category
Status
Type
Role
```

↓

```
IN
```

**Relationship**

```
Customer ↔ Orders

Employee ↔ Manager

Product ↔ Reviews

User ↔ Login
```

↓

```
EXISTS
```
