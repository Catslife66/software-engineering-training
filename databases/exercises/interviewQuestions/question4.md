# Interview Round 4 (slightly harder)

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

| department | highest_salary |

Where:

- highest_salary = max salary in each department
- only include departments where highest_salary is below the average salary of all employees
- sort by highest_salary DESC

**Answer**

```
SELECT e.department, MAX(s.salary) AS highest_salary
FROM employees e
JOIN salaries s ON s.employee_id = e.employee_id
GROUP BY e.department
HAVING MAX(s.salary) < (
    SELECT AVG(salary)
    FROM salaries
)
ORDER BY highest_salary DESC;
```
