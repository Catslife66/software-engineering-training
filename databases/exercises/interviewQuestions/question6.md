# Interview Round 6

**DATASET**

employees

| employee_id | name  | department  |
| ----------- | ----- | ----------- |
| 1           | Alice | Engineering |
| 2           | Bob   | Engineering |
| 3           | Carol | HR          |
| 4           | David | HR          |
| 5           | Eve   | Sales       |

salaries

| employee_id | salary |
| ----------- | ------ |
| 1           | 100    |
| 2           | 120    |
| 3           | 80     |
| 4           | 90     |
| 5           | 200    |

**Problem**

Return:

| department | employee_name | salary |

Where:

- show the employee with the highest salary in each department
- if there is a tie, return the employee with the smaller employee_id
- sort by department ASC

**Answer**

```
WITH department_salaries AS (
    SELECT e.employee_id, e.name, e.department, s.salary
    FROM employees e
    JOIN salaries s ON s.employee_id = e.employee_id
),
highest_department_salaries AS (
    SELECT department, MAX(salary) AS hightest_salary
    FROM department_salaries
    GROUP BY department
),
salary_winners AS (
    SELECT ds.employee_id, ds.name, ds.department, ds.salary
    FROM department_salaries ds
    JOIN highest_department_salaries hds
      ON hds.department = ds.department
     AND hds.hightest_salary = ds.salary
),
min_id_winner AS (
    SELECT department, MIN(employee_id) AS min_employee_id
    FROM salary_winners
    GROUP BY department
)
SELECT sw.department, sw.name AS employee_name, sw.salary
FROM salary_winners sw
JOIN min_id_winner mw ON mw.department = sw.department
            AND mw.min_employee_id = sw.employee_id
ORDER BY sw.department ASC;
```

## What is a "tie"?

> A tie = multiple rows share the same value for the thing we are ranking

If multiple employees in the same department have the same highest salary, choose the smaller employee_id to represent the highest salary in that department.

## What "tie-break" means?

> Tie-break = a rule to choose ONE row when multiple rows tie

which row should we pick if multiple share that value? -> return the employee with the smaller employee_id

## Where ties appear in SQL problems

Very common in:

**1. Top salary per department**

→ multiple employees may share same salary

**2. Favorite product**

→ multiple products may have same total quantity

**3. Latest record**

→ multiple rows may have same timestamp

## General pattern

1. find best value (MAX / MIN)
2. join back → may return multiple rows (tie)
3. apply tie-break rule → choose one
