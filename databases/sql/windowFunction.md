# Window Function

**Example**

salaries

| employee | department | salary |
| -------- | ---------- | -----: |
| Alice    | Eng        |    100 |
| Bob      | Eng        |    120 |
| Carol    | HR         |     80 |
| David    | HR         |     90 |

**Goal**

Add a column:

```
average salary per department
```

**Using GROUP BY (old way)**

```
SELECT department, AVG(salary)
FROM salaries
GROUP BY department;
```

❌ Problem:

```
loses individual rows
```

**Using Window Function**

```
SELECT employee,
       department,
       salary,
       AVG(salary) OVER (PARTITION BY department) AS avg_salary
FROM salaries;
```

**Result**

| employee | department | salary | avg_salary |
| -------- | ---------- | ------ | ---------- |
| Alice    | Eng        | 100    | 110        |
| Bob      | Eng        | 120    | 110        |
| Carol    | HR         | 80     | 85         |
| David    | HR         | 90     | 85         |

**Key Idea**

PARTITION BY = split the data into groups (like GROUP BY), but keep all rows

```
OVER (PARTITION BY ...)
```

means:

> "group the data BUT do NOT collapse rows" -> give each row a position inside its group

## Most Important Window Function

| Function        | Purpose                  |
| --------------- | ------------------------ |
| `ROW_NUMBER()`  | number rows              |
| `RANK()`        | ranking with gaps        |
| `DENSE_RANK()`  | ranking without gaps     |
| `SUM()`         | running/partition totals |
| `AVG()`         | moving averages          |
| `LAG()`         | look backward            |
| `LEAD()`        | look forward             |
| `FIRST_VALUE()` | first value in window    |
| `LAST_VALUE()`  | last value in window     |

### ROW_NUMBER()

> always unique ranking (1, 2, 3…)

This is the one that replaces your complex “top per group” logic.

Example:

salaries

| employee_id | name  | department | salary |
| ----------- | ----- | ---------- | ------ |
| 1           | Alice | Eng        | 120    |
| 2           | Bob   | Eng        | 120    |
| 3           | Carol | HR         | 90     |

Goal:

Get highest salary per department (with tie-break)

Window Function Solution

```
SELECT *
FROM (
    SELECT e.employee_id,
           e.name,
           e.department,
           s.salary,
           ROW_NUMBER() OVER (
               PARTITION BY e.department
               ORDER BY s.salary DESC, e.employee_id ASC
           ) AS rn
    FROM employees e
    JOIN salaries s ON s.employee_id = e.employee_id
) t
WHERE rn = 1;
```

Why this works

```
PARTITION BY department → group per department
ORDER BY salary DESC → highest first
ORDER BY employee_id ASC → tie-break
ROW_NUMBER → assigns rank 1,2,3...
```

Then:

```
WHERE rn = 1
```

👉 gives the winner per group

### RANK()

> ties share same rank, gaps appear (1, 1, 3...)

### DENSE_RANK()

> ties share same rank, no gaps (1, 1, 2...)

Example:

| name  | score |
| ----- | ----- |
| Alice | 100   |
| Bob   | 100   |
| Carol | 90    |

- ROW_NUMBER()

| name  | score | rn  |
| ----- | ----- | --- |
| Alice | 100   | 1   |
| Bob   | 100   | 2   |
| Carol | 90    | 3   |

- RANK()

| name  | score | rank |
| ----- | ----- | ---- |
| Alice | 100   | 1    |
| Bob   | 100   | 1    |
| Carol | 90    | 3    |

- DENSE_RANK()

| name  | score | dense_rank |
| ----- | ----- | ---------- |
| Alice | 100   | 1          |
| Bob   | 100   | 1          |
| Carol | 90    | 2          |

```
ROW_NUMBER() → pick ONE (with tie-break)
RANK()       → keep ALL ties
DENSE_RANK() → keep ALL ties, best for "Nth highest"
```

### LAG()

> look at previous row inside a window partition - peeks at another row in the same partition

LAG() very common for:

- compare current vs previous sales
- detect changes
- calculate growth
- identify trends
- find consecutive events

Example: sales growth

You can do:

```
amount - LAG(amount)
```

to calculate:

```
difference from previous sale
```

**Mental model**

```
LAG(column, N)
```

means "give me the value from N rows earlier"

### LEAD() - opposite of LAG()

```
LAG() = look backward
LEAD() = look forward

LEAD(amount, 1)

means look 1 row forward
```

### FIRST_VALUE()

What is the first value in this partition?

```
FIRST_VALUE(amount) OVER (
    PARTITION BY salesperson
    ORDER BY sale_date
)
```

### LAST_VALUE()

With the default window frame, LAST_VALUE() returns the last value **up to the current row**, not the last value in the whole partition.

orders

| salesperson | sale_date | amount |
| ----------- | --------- | -----: |
| Alice       | Jan 1     |    100 |
| Alice       | Jan 5     |    200 |
| Alice       | Jan 10    |    150 |
| Bob         | Jan 2     |     80 |
| Bob         | Jan 8     |    120 |

```
LAST_VALUE(amount) OVER (
    PARTITION BY salesperson
    ORDER BY sale_date
)
```

Returns

| sale_date | amount | last_sale |
| --------- | -----: | --------: |
| Jan 1     |    100 |       100 |
| Jan 5     |    200 |       200 |
| Jan 10    |    150 |       150 |

**Key idea**

```
FIRST_VALUE is usually straightforward.
LAST_VALUE needs an explicit window frame.
```

To get the true last sale for every row, you must widen the frame:

```
LAST_VALUE(amount) OVER (
    PARTITION BY salesperson
    ORDER BY sale_date
    ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING
) AS last_sale
```

Or to avoid trap, use:

```
FIRST_VALUE(amount) OVER (
    PARTITION BY salesperson
    ORDER BY sale_date DESC
)
```

to get the true last value

## Important window-function family

| Category             | Functions                          |
| -------------------- | ---------------------------------- |
| Ranking              | `ROW_NUMBER`, `RANK`, `DENSE_RANK` |
| Running calculations | `SUM OVER`, `AVG OVER`             |
| Row comparison       | `LAG`, `LEAD`                      |

## ROWS BETWEEN

A window function with a ROWS BETWEEN clause allows you to perform calculations on a specific "sliding frame" of rows relative to the row you are currently on.

Instead of looking at the entire table at once, or grouping everything down into a single row, it lets you maintain the individual rows while looking "backward" or "forward" in your data.

Example:

```
SUM(amount) OVER (
    ORDER BY visited_on
    ROWS BETWEEN 2 PRECEDING AND CURRENT ROW
)
```

## Interview Tips

```
1. Do I need a value or a row?
2. Is there a tie? (One winner or keep ties?)
3. Where does the ranking restart?
4. How is the winner decided?
```
