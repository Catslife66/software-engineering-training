# Monthly Sales Summary

orders

| order_id | order_date | customer_id | amount |
| -------: | ---------- | ----------: | -----: |
|        1 | 2026-07-01 |         101 |    100 |
|        2 | 2026-07-15 |         102 |    200 |
|        3 | 2026-08-02 |         101 |    300 |
|        4 | 2026-08-20 |         103 |    150 |
|        5 | 2026-08-25 |         104 |    250 |

Exepcted

| month      | orders | customers | revenue |
| ---------- | -----: | --------: | ------: |
| 2026-07-01 |      2 |         2 |     300 |
| 2026-08-01 |      3 |         3 |     700 |

## DATE_TRUNC()

This is a brand new function, so don't worry that you didn't know it.

Let's understand what it does instead of memorizing it.

Suppose we have:

| order_date |
| ---------- |
| 2026-07-01 |
| 2026-07-15 |
| 2026-07-28 |
| 2026-08-02 |
| 2026-08-20 |

Now apply:

```
DATE_TRUNC('month', order_date)
```

The result becomes:

| order_date | month      |
| ---------- | ---------- |
| 2026-07-01 | 2026-07-01 |
| 2026-07-15 | 2026-07-01 |
| 2026-07-28 | 2026-07-01 |
| 2026-08-02 | 2026-08-01 |
| 2026-08-20 | 2026-08-01 |

Every date in July became:

```
2026-07-01
```

Every date in August became:

```
2026-08-01
```

It doesn't matter whether the order happened on the 2nd, 15th, or 31st.

They all become the **same month value**.

Now the SQL becomes simple:

```
SELECT DATE_TRUNC('month', order_date) AS month,
       COUNT(*) AS orders,
       COUNT(DISTINCT customer_id) AS customers,
       SUM(amount) AS revenue
FROM orders
GROUP BY DATE_TRUNC('month', order_date)
ORDER BY month;
```

## This is one of the biggest ideas in reporting SQL

You've probably noticed a pattern now.

Daily report:

```
Grain = Day
```

Monthly report:

```
Grain = Month
```

Tomorrow we could build:

```
Grain = Year
```

or

```
Grain = Customer
```

or

```
Grain = Product
```

The metrics hardly change.

Only the **grain** changes.

## Grain

> If I point to one row in the final report, what does that row represent?

That's all grain means.

**Example 1**

Daily report

| Date  | Revenue |
| ----- | ------: |
| Jul 1 |     350 |
| Jul 2 |     400 |

If I point here:

| Date      | Revenue |
| --------- | ------: |
| **Jul 2** | **400** |

What does this row represent?

Answer:

```
One day.
```

That's the grain.

**Example 2**

Customer report

| Customer | Revenue |
| -------- | ------: |
| Alice    |     600 |
| Bob      |     300 |

If I point here:

| Customer  | Revenue |
| --------- | ------: |
| **Alice** | **600** |

What does this row represent?

Answer:

```
One customer.
```

That's the grain.

## Metrics

Metrics answer questions about that row.

Daily report:

| Date | Orders | Customers | Revenue |
| ---- | -----: | --------: | ------: |

The row represents:

```
One day
```

The metrics describe that day:

```
orders
customers
revenue
```

Think of building a class in Java

```
class Customer {
    String name;
    int age;
    String email;
}
```

What does one object represent?

```
One customer.
```

That's like the **grain**.

The fields:

```
name
age
email
```

are like the **metrics**.

Exactly the same idea.
