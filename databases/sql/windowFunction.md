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
| -------- | ---------- | -----: | ---------: |
| Alice    | Eng        |    100 |        110 |
| Bob      | Eng        |    120 |        110 |
| Carol    | HR         |     80 |         85 |
| David    | HR         |     90 |         85 |

**Key Idea**

PARTITION BY = split the data into groups (like GROUP BY), but keep all rows

```
OVER (PARTITION BY ...)
```

means:

> "group the data BUT do NOT collapse rows" -> give each row a position inside its group

## Most Important Window Function

### ROW_NUMBER()

> always unique ranking (1, 2, 3…)

This is the one that replaces your complex “top per group” logic.

Example:

salaries

| employee_id | name  | department | salary |
| ----------- | ----- | ---------- | -----: |
| 1           | Alice | Eng        |    120 |
| 2           | Bob   | Eng        |    120 |
| 3           | Carol | HR         |     90 |

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

> ties share same rank, gaps appear

### DENSE_RANK()

> ties share same rank, no gaps

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
