# Interview Round 1 (Core SQL)

**Dataset**

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

| department | avg_salary |

Where:

- avg_salary = average salary per department
- only include departments where avg_salary > 100
- sort by avg_salary DESC

**Answer**

```
SELECT e.department, AVG(s.salary) AS avg_salary
FROM employees e
JOIN salaries s ON s.employee_id = e.employee_id
GROUP BY e.department
HAVING AVG(s.salary) > 100
ORDER BY avg_salary DESC;
```
