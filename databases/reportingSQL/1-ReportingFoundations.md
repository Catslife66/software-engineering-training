# Reporting Mental Model

We have already established the central reporting model:

```
Business request
      ↓
Grain
      ↓
Dimensions
      ↓
Metrics
```

Now let's complete the rest of the module as one connected topic.

## Grain, Dimensions, Metrics

Imagine an e-commerce manager asks:

> “Give me a report showing our sales.”

That request isn't enough.

What should one row of the report represent?

It could be:

Report A

| date  | revenue |
| ----- | ------: |
| 1 Jul |  £5,000 |
| 2 Jul |  £6,200 |
| 3 Jul |  £4,800 |

Here:

> One row = one day

Report B

| salesperson | revenue |
| ----------- | ------: |
| Alice       | £50,000 |
| Bob         | £42,000 |
| Charlie     | £38,000 |

Here:

> One row = one salesperson

Report C

| month | category    | revenue |
| ----- | ----------- | ------: |
| Jul   | Accessories | £20,000 |
| Jul   | Displays    | £35,000 |
| Aug   | Accessories | £24,000 |
| Aug   | Displays    | £31,000 |

Here:

> One row = one category in one month

All three reports measure sales, but they have different grains.

---

**What is GRAIN**

Grain describes what one row of a dataset represents.

You'll also hear terms such as:

- granularity
- level of detail
- level of aggregation

They are closely related.

The question I want you to develop as a reflex is:

> What does one row represent?

---

Consider:

| month | category    | revenue | orders | customers |
| ----- | ----------- | ------: | -----: | --------: |
| Jul   | Accessories |    £20k |    500 |       320 |
| Jul   | Displays    |    £35k |    200 |       170 |

**Grain**

```
one category in one month
```

> What does one output row represent?

**Dimensions**

The attributes defining that grain:

```
month
category
```

> How do we divide or describe the data?

**Metrics**

Things we're measuring about that grain:

```
revenue
orders
customers
```

> What numerical property are we measuring?

Grain is not the purpose of the report.

It is the **level at which the report describes the business**.

## Business Formula Before SQL Formula

Suppose the request is:

> “How much revenue did each salesperson generate?”

Don't start with:

```
SUM(amount)
```

First ask:

> What should one output row represent?

Answer:

```
one salesperson
```

Therefore:

```
grain = salesperson
```

Then the metric:

```
revenue
```

Only after that do we eventually derive something like:

```
GROUP BY salesperson

Notice the order of reasoning:

Business request
      ↓
Grain
      ↓
Metric
      ↓
SQL
```

not:

```
Business request
      ↓
SUM()?
GROUP BY?
JOIN?
```

## Multi-Dimensional Grain

This becomes especially important when the report contains multiple dimensions.

Consider:

> “Show revenue for each salesperson for each month.”

One row is not simply:

```
one salesperson
```

because Alice appears repeatedly:

| month | salesperson | revenue |
| ----- | ----------- | ------: |
| Jan   | Alice       |    £10k |
| Feb   | Alice       |    £14k |
| Mar   | Alice       |    £12k |

And it's not simply:

```
one month
```

because January contains multiple salespeople.

The grain is:

```
one salesperson in one month
```

or:

```
grain = month + salesperson
```

This predicts the eventual aggregation:

```
GROUP BY month, salesperson
```

That's why grain is so useful.

## Think in Unique Combinations

Another way to understand grain is through unique combinations.

If the grain is:

```
month + salesperson
```

then every output row represents one unique pair:

```
(January, Alice)
(January, Bob)
(February, Alice)
(February, Bob)
```

If the grain becomes:

```
month + salesperson + region
```

then rows represent:

```
(January, Alice, North)
(January, Alice, South)
(January, Bob, North)
...
```

Each added dimension creates a **finer grain**.

## Fine Grain vs Coarse Grain

Suppose our raw data is:

| order_id | customer | date  | amount |
| -------: | -------- | ----- | -----: |
|        1 | Alice    | Jul 1 |    £50 |
|        2 | Alice    | Jul 1 |   £100 |
|        3 | Bob      | Jul 1 |    £70 |

Its grain is:

```
one row = one order
```

That's relatively fine-grained data.

Now aggregate:

| date  | revenue |
| ----- | ------: |
| Jul 1 |    £220 |

Grain:

```
one row = one day
```

We've moved to a **coarser grain**.

Think:

```
Fine
──────────────────────────────→ Coarse

individual orders
      ↓
customer-day
      ↓
day
      ↓
month
      ↓
year
```

As we aggregate, we lose detail but gain summaries.

## Counting Is a Business Decision

Consider:

| order_id | customer_id |
| -------: | ----------: |
|        1 |         101 |
|        2 |         101 |
|        3 |         101 |
|        4 |         102 |

Three different questions:

How many orders?

```
4
```

How many customers?

```
2
```

How many order rows?

```
4
```

With an orders table, questions 1 and 3 happen to produce the same number.

But that's because:

```
one row = one order
```

If we joined to order_items, that assumption could disappear.

Therefore, don't begin with:

```
COUNT(*) or COUNT(DISTINCT ...)?
```

Begin with:

```
What business entity am I trying to count?
```

Then SQL follows.

## Time Is Usually a Dimension

Business reports frequently introduce time:

- daily revenue
- monthly customers
- weekly orders
- quarterly sales by region

A raw timestamp might be:

```
2026-07-18 14:37:22
```

but the business grain might require:

```
day
week
month
quarter
year
```

So we transform the raw timestamp into the required business dimension.

For PostgreSQL:

```
DATE_TRUNC('month', order_date)
```

Conceptually:

```
raw timestamp
      ↓
business time bucket
      ↓
report dimension
```

This is why:

```
GROUP BY order_date
```

and:

```
GROUP BY DATE_TRUNC('month', order_date)
```

answer fundamentally different questions.

## Filtering: Which Rows Belong to the Report?

Once the grain and metrics are understood, another question appears:

Which source rows belong to this report?

Example:

> “Show July revenue by category.”

We have two independent ideas:

```
grain
→ one category

population
→ orders placed during July
```

The date condition defines the population being analysed.

That's why it belongs before aggregation:

```
WHERE order_date >= DATE '2026-07-01'
  AND order_date <  DATE '2026-08-01'
```

Then:

```
July rows
   ↓
group into categories
   ↓
calculate revenue
```

## WHERE vs HAVING — Business Mental Model

Instead of memorising:

```
WHERE before GROUP BY
HAVING after GROUP BY
```

think:

`WHERE`

> Which source rows participate?

`HAVING`

> Which resulting groups survive?

Example:

> “For July, show categories generating more than £10,000.”

There are two filters.

First:

```
Which orders?
→ July orders
→ WHERE
```

Then:

```
Which category groups?
→ revenue > £10,000
→ HAVING
```

Conceptually:

```
all orders
    ↓ WHERE
July orders
    ↓ GROUP BY category
category totals
    ↓ HAVING
categories > £10,000
```

## Intermediate Grain

Request:

> “For each region, show the salesperson with the highest revenue.”

Final grain:

```
one region
```

But we cannot immediately calculate the winner.

First we need:

```
one salesperson per region
```

with:

```
total_revenue
```

So:

```
raw sales
    ↓
INTERMEDIATE GRAIN
region + salesperson
    ↓
rank/select winner
    ↓
FINAL GRAIN
region
```

This is the beginning of a much broader engineering idea:

> Complex reports are often sequences of meaningful grain transformations.

A CTE can represent one such transformation.

## A Practical Rule

Whenever you receive a reporting problem, don't ask:

“What SQL should I use?”

Start with:

Question 1

> What should one output row represent?

Question 2

> Which dimensions uniquely identify that row?

Question 3

> What metrics are being measured about that row?

Only then move toward SQL.

Before writing SQL, run through this:

```
BUSINESS QUESTION
       │
       ├── Who are we reporting about?
       │       → POPULATION
       │
       ├── Which activity counts?
       │       → SCOPE
       │
       ├── What does one output row represent?
       │       → FINAL GRAIN
       │
       ├── What identifies that row?
       │       → DIMENSIONS
       │
       ├── What are we measuring?
       │       → METRICS
       │
       ├── What does each source row represent?
       │       → SOURCE GRAIN
       │
       └── Do we need another representation?
               → INTERMEDIATE GRAIN

                       ↓

                      SQL
```

## Exercise

orders

| order_id | customer_id | salesperson | region | order_date |
| -------: | ----------: | ----------- | ------ | ---------- |
|        1 |         101 | Alice       | North  | 2026-07-02 |
|        2 |         102 | Alice       | North  | 2026-07-05 |
|        3 |         101 | Bob         | South  | 2026-07-10 |
|        4 |         103 | Bob         | South  | 2026-08-03 |

order_items

| order_id | product_id | quantity | unit_price |
| -------: | ---------: | -------: | ---------: |
|        1 |         10 |        2 |         50 |
|        1 |         11 |        1 |        100 |
|        2 |         10 |        1 |         50 |
|        3 |         12 |        2 |        200 |
|        4 |         11 |        3 |        100 |

The Sales Director asks:

> “For July, show each region's total revenue, number of orders, number of unique customers, and average order value.”

```
1. Final grain
one region in July

2. Dimensions
region + month

3. Metrics
total revenue
number of orders
unique customers
average order value

4. Business formula for average order value
average order = total revenue / number of orders

5. Population
July orders

6. Source grain of orders and order_items
For orders: one row = one order
For order_items: one row = one product line within an order
```

**Population = the set of entities/records that are eligible to appear in or contribute to the report.**

**Source grain = what one row of the input dataset/table represents before our reporting transformations.**

```
SELECT
    o.region,
    COUNT(DISTINCT o.customer_id) AS number_of_orders,
    COUNT(DISTINCT o.order_id) AS unique_customers,
    SUM(oi.quantity * oi.unit_price) AS total_revenue,
    ROUND(
        SUM(oi.quantity * oi.unit_price)::numeric/
        COUNT(DISTINCT o.customer_id),
        2
    ) AS average_order_value
FROM orders o
JOIN order_items oi ON oi.order_id = o.order_id
WHERE o.order_date >= DATE '2026-07-01'
    AND o.order_date < DATE '2026-08-01'
GROUP BY o.region;
```
