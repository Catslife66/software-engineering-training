# Salesperson Performance

Imagine you're a backend engineer at Shopify.

The Sales Director asks:

"Build me a report showing how each salesperson performed this month."

sales

| sale_id | salesperson | customer_id | amount | sale_date |
| ------: | ----------- | ----------: | -----: | --------- |
|       1 | Alice       |         101 |    300 | Jul 1     |
|       2 | Alice       |         102 |    200 | Jul 5     |
|       3 | Bob         |         103 |    500 | Jul 2     |
|       4 | Bob         |         101 |    100 | Jul 7     |
|       5 | Charlie     |         104 |    250 | Jul 8     |

- grain = one salesperson
- Total Sales = Add together the value of every sale made by the salesperson.
- Number of Customers = Count the unique customers served by the salesperson.
- Average Sale = Add all sales made by the salesperson and divide by the number of sales.
- Largest Sale = The highest value of a sale by a salesperson

## One of the biggest differences between programming and software engineering

A programmer might immediately start writing SQL.

A software engineer first asks:

> "What exactly do you mean?"

That one question can save hours of building the wrong thing.

```
Business Request
↓
Clarify Requirements
↓
Business Definition
↓
Business Formula
↓
Report Design
↓
SQL Patterns
↓
SQL Query
```
