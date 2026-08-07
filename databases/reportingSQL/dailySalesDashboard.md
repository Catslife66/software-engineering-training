# Daily Sales Dashboard

orders

| order_id | order_date | customer_id | amount |
| -------- | ---------- | ----------: | -----: |
| 1        | 2026-07-01 |         101 |    100 |
| 2        | 2026-07-01 |         102 |    200 |
| 3        | 2026-07-01 |         101 |     50 |
| 4        | 2026-07-02 |         103 |    300 |
| 5        | 2026-07-02 |         104 |    100 |
| 6        | 2026-07-03 |         101 |     80 |

Goal:

Build a daily sales report.

Expected:

| Date       | Orders | Revenue |
| ---------- | -----: | ------: |
| 2026-07-01 |      3 |     350 |
| 2026-07-02 |      2 |     400 |
| 2026-07-03 |      1 |      80 |

> The report has a daily grain. It groups orders by order_date and calculates daily order volume and revenue.

```
SELECT
    order_date,
    COUNT(*) AS order_count,
    SUM(amount) AS revenue
FROM orders
GROUP BY order_date
ORDER BY order_date;
```

## Different reporting patterns

Notice how business people often ask two different questions that sound similar.

Question 1

```
How many orders did we receive today?
```

Answer:

```
COUNT(*)
```

Question 2

```
How many customers bought something today?
```

Answer:

```
COUNT(DISTINCT customer_id)
```

## Engineering Thinking

I want to introduce another habit that experienced engineers develop.

Whenever someone asks for a report, mentally ask yourself:

**What am I counting?**

Is it:

```
Rows?
Customers?
Orders?
Products?
Days?
Revenue?
```

The aggregation depends entirely on that answer.

For example:

| Business Question   | SQL Pattern                   |
| ------------------- | ----------------------------- |
| Number of orders    | `COUNT(*)`                    |
| Number of customers | `COUNT(DISTINCT customer_id)` |
| Total revenue       | `SUM(amount)`                 |
| Average order value | `AVG(amount)`                 |
| Largest order       | `MAX(amount)`                 |

When someone asks for a report, don't immediately think:

_"Which SQL function?"_

Instead ask:

**"What decision will someone make from this report?"**

## Engineering diagrams

This is how I think every reporting query is built.

```
Business Question
        │
        ▼
Business Metric
        │
        ▼
Business Formula
        │
        ▼
SQL Patterns
        │
        ▼
SQL Query
```

Let's apply it to an example.

```
Business Question
"What percentage of customers placed more than one order today?"

        │
        ▼

Business Metric
Repeat customer rate

        │
        ▼

Business Formula
Customers with >1 order
────────────────────────
Total ordering customers

        │
        ▼

SQL Patterns
GROUP BY
HAVING
COUNT
COUNT DISTINCT

        │
        ▼

SQL Query
```

## Welcome to real reporting

From now on, every report we build will start with three questions.

**Question 1**

What is one output row?

(Day? Customer? Product? Month?)

**Question 2**

What business questions are we answering?

For example:

- How busy were we?
- How much money did we make?
- How many customers bought something?

**Question 3**

How would we calculate those metrics with a calculator?

Only after answering those three questions do we write SQL.

## A real KPI dashboard

| Metric              | Business Meaning   | Calculator             |
| ------------------- | ------------------ | ---------------------- |
| Orders              | Workload           | Count orders           |
| Customers           | Customer activity  | Count unique customers |
| Revenue             | Money earned       | Sum amounts            |
| Average Order Value | Spending per order | Revenue ÷ Orders       |
| Largest Order       | Biggest purchase   | Maximum amount         |

## Translate to SQL

| order_id | order_date | customer_id | amount |
| -------- | ---------- | ----------: | -----: |

Return:

| order_date | orders | customers | revenue | avg_order_value | largest_order |
| ---------- | -----: | --------: | ------: | --------------: | ------------: |

```
SELECT order_date,
       COUNT(order_id) AS orders,
       COUNT(DISTINCT customer_id) AS customers,
       SUM(amount) AS revenue,
       ROUND(AVG(amount), 2) AS avg_order_value,
       MAX(amount) AS largest_order
FROM orders
GROUP BY order_date
ORDER BY order_date;
```
