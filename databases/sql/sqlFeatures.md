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
Need to check a value?
        │
        ▼
Is the value in a known set?
        │
        ▼
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
Need to check a relationship?
        │
        ▼
Does a matching row exist?
        │
        ├───────────────┐
        ▼               ▼
      EXISTS      NOT EXISTS
```

## Correlated Subqueries

Suppose we have:

customers

| customer_id | name    |
| ----------- | ------- |
| 101         | Alice   |
| 102         | Bob     |
| 103         | Charlie |

orders

| order_id | customer_id | amount |
| -------- | ----------- | -----: |
| 1        | 101         |    100 |
| 2        | 101         |    200 |
| 3        | 103         |    300 |

Question

Return customers who have placed an order.

How does SQL think?

Imagine SQL is reading the customers table.

Current row

```
Alice (101)
```

Now SQL runs this:

```
SELECT 1
FROM orders
WHERE customer_id = c.customer_id
```

Notice:

```
c.customer_id
```

Where does that come from?

It comes from the **current row** of the outer query.

For Alice:

```
WHERE customer_id = 101
```

Then SQL moves to Bob.

Now the **same inner query** becomes:

```
WHERE customer_id = 102
```

Then Charlie.

```
WHERE customer_id = 103
```

Instead of running once,

the inner query runs for every outer row.

```
Customers
───────────────

Alice
   ↓
run inner query
using Alice's id

Bob
   ↓
run inner query
using Bob's id

Charlie
   ↓
run inner query
using Charlie's id
```

That is what "correlated" means.

The inner query is connected to the outer query.

Let's compare

**Ordinary subquery**

```
WHERE customer_id IN (
    SELECT customer_id
    FROM orders
)
```

The subquery doesn't know anything about the current customer.

It simply returns:

```
101
103
```

once.

**Correlated subquery**

```
WHERE EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.customer_id = c.customer_id
)
```

Now the inner query changes depending on:

```
Current customer
```

- Value lookup → `IN`
- Current row influences the inner query → correlated subquery

## NOT EXISTS

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

Return customers who have **never** placed an order.

Current row

```
Alice (101)
```

Inner query:

```
Find an order
where customer_id = 101
```

Found?

Yes.

So:

```
EXISTS = TRUE
NOT EXISTS = FALSE
```

Alice is not returned.

Next row

```
Bob (102)
```

Inner query:

```
Find an order
where customer_id = 102
```

Found?

No.

So:

```
EXISTS = FALSE
NOT EXISTS = TRUE
```

Bob is returned.

...

**The mental model**

Notice we never ask:

```
"How many orders?"
```

We simply ask:

```
Can I find one?

Yes?

No?
```

If the answer is No, `NOT EXISTS` returns TRUE.

**Engineering pattern recognition**

Whenever you hear phrases like:

- never
- no
- without
- hasn't
- doesn't have
- missing
- not yet

your brain should immediately think:

```
NOT EXISTS
```

Examples:

- Customers with no orders
- Products without reviews
- Employees without a manager
- Users who haven't logged in
- Students with no enrolments

They're all asking for the absence of a related row.

## `NOT IN` vs `NOT EXISTS`

The famous trap

Query A

```
WHERE customer_id NOT IN (
    SELECT customer_id
    FROM orders
)
```

Query B

```
WHERE NOT EXISTS (
    SELECT 1
    FROM orders o
    WHERE o.customer_id = c.customer_id
)
```

Most people think they're obviously the same.

Usually they are.

Until one thing appears...

NULL

customers

| customer_id | name    |
| ----------: | ------- |
|         101 | Alice   |
|         102 | Bob     |
|         103 | Charlie |

orders

| order_id | customer_id |
| -------: | ----------: |
|        1 |         101 |
|        2 |        NULL |

Notice this bad row.

Perhaps:

- the data is incomplete
- someone imported broken data
- the foreign key wasn't enforced

Whatever the reason, we now have:

customer_id

```
101
NULL
```

Question

Return customers who have **never placed an order**.

Let's reason through both queries.

Query 1 — `NOT EXISTS`

Think about Bob.

Current customer:

```
102
```

Inner query asks:

```
Does there exist an order
whose customer_id = 102?
```

Order table:

```
101
NULL
```

Find 102?

No.

So:

```
NOT EXISTS = TRUE
```

Bob is returned.

Everything works.

Query 2 — `NOT IN`

Now SQL asks:

```
Is 102 NOT IN
{
    101,
    NULL
}
```

And here comes the strange part.

Can SQL prove that 102 is not equal to NULL?

**SQL does not treat NULL as a value.**

So the correct answer isn't True or False.

It's **Unknown**.

### SQL has three logical values

Most programming languages have:

```
TRUE
FALSE
```

SQL has:

```
TRUE
FALSE
UNKNOWN
```

And `NULL` introduces **UNKNOWN**.

For example:

```
102 = NULL
```

does **not** evaluate to **FALSE**.

It evaluates to:

```
UNKNOWN
```

Likewise:

```
102 <> NULL -> UNKNOWN
```

Suppose SQL sees:

WHERE customer_id NOT IN (101, NULL)

First condition:

```
102 <> 101
```

Result:

```
TRUE
```

Second condition:

```
102 <> NULL
```

Result:

```
UNKNOWN
```

So SQL now has:

```
TRUE
AND
UNKNOWN
```

**And here's the crucial rule**

The WHERE clause only keeps rows where the condition is:

```
TRUE
```

It does not keep:

```
FALSE
UNKNOWN
```

So Bob is filtered out.

The same thing happens for Charlie.

In fact...

No rows are returned at all.

This is why experienced SQL developers are cautious with `NOT IN`

hen one bad row with a NULL sneaks into the subquery.

Suddenly your report returns:

```
0 rows
```

The SQL is syntactically correct.

It just isn't logically doing what you intended.

### A rule you'll use for years

Whenever you're checking for the absence of related rows, prefer:

```
✅ NOT EXISTS
```

instead of

```
⚠️ NOT IN (unless you're certain the subquery cannot return NULL).
```
