**DATASET**

employees

| employee_id | name  | department | salary |
| ----------- | ----- | ---------- | ------ |
| 1           | Alice | Eng        | 120    |
| 2           | Bob   | Eng        | 120    |
| 3           | Carol | Eng        | 100    |
| 4           | David | HR         | 90     |
| 5           | Eve   | HR         | 80     |

**Problem**

Return:

| department | employee_name | salary |

Where:

- show employees who have the highest salary per department
- if there is a tie, return ALL tied employees

```
WITH ranking AS (
    SELECT department, name, salary,
        RANK() OVER (
            PARTITION BY department
            ORDER BY salary DESC
        ) AS rank
    FROM employees
)
SELECT r.department, r.name AS employee_name, r.salary
FROM ranking r
WHERE r.rank = 1;
```

**Problem**

Return:

| department | employee_name | salary |

Where:

- return the second highest salary per department
- if there is a tie at second place, return all tied employees
- sort by department ASC

```
WITH ranking AS (
    SELECT department, name, salary,
        DENSE_RANK() OVER (
            PARTITION BY department
            ORDER BY salary DESC
        ) AS dr
    FROM employees
)
SELECT r.department, r.name AS employee_name, r.salary
FROM ranking r
WHERE r.dr = 2
ORDER BY r.department ASC;
```
