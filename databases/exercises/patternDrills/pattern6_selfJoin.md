# Self Join

Whenever you see a question like:

```
same manager
same department
same city
same category
parent-child
employee-manager
friend relationships
```

Ask:

```
Am I comparing one row to another row
in the SAME table?
```

If yes:

```
Self Join candidate
```

## Drill 1

**Dataset**

employees

| employee_id | name    | manager_id |
| ----------- | ------- | ---------- |
| 1           | Alice   | NULL       |
| 2           | Bob     | 1          |
| 3           | Charlie | 1          |
| 4           | David   | 2          |

Goal:

| employee_name | manager_name |
| ------------- | ------------ |
| Alice         | NULL         |
| Bob           | Alice        |
| Charlie       | Alice        |
| David         | Bob          |

```
SELECT e.name AS employee_name,
       m.name AS manager_name
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.employee_id;
```

## Drill 2

Same dataset

return only employees who do have a manager

```
SELECT e.name AS employee_name,
       m.name AS manager_name
FROM employees e
JOIN employees m ON e.manager_id = m.employee_id;
```

## Drill 3

Same dataset

Goal:

| manager_name | direct_reports |
| ------------ | -------------: |
| Alice        |              2 |
| Bob          |              1 |

```
SELECT m.name AS manager_name,
       COUNT(e.employee_id) AS direct_reports
FROM employees e
JOIN employees m ON e.manager_id = m.employee_id
GROUP BY m.employee_id, m.name;
```

## Drill 4

**Dataset**

employees

| employee_id | name    | department |
| ----------- | ------- | ---------- |
| 1           | Alice   | IT         |
| 2           | Bob     | IT         |
| 3           | Charlie | HR         |
| 4           | David   | IT         |

Goal:

| employee | coworker |
| -------- | -------- |
| Alice    | Bob      |
| Alice    | David    |
| Bob      | Alice    |
| Bob      | David    |
| David    | Alice    |
| David    | Bob      |

```
SELECT e.name AS employee_name,
       c.name AS coworker_name
FROM employees e
JOIN employees c
  ON c.department = e.department
 AND c.employee_id != e.employee_id
WHERE e.department = 'IT'
ORDER BY employee_name, coworker_name;
```

## Drill 5

**Dataset**

employees

| employee_id | name    | manager_id |
| ----------- | ------- | ---------- |
| 1           | Alice   | NULL       |
| 2           | Bob     | 1          |
| 3           | Charlie | 1          |
| 4           | David   | 2          |

Goal:

who appears as someone else's manager?

| employee_name |
| ------------- |
| Alice         |
| Bob           |

```
SELECT DISTINCT e.name AS employee_name
FROM employees e
JOIN employees m ON m.manager_id = e.employee_id;
```

## Drill 6

Same dataset

Goal:

Return employees who have the same manager as Bob.

| employee_name |
| ------------- |
| Charlie       |

```
SELECT e2.name AS employee_name
FROM employees e1
JOIN employees e2
  ON e1.manager_id = e2.manager_id
 AND e1.employee_id != e2.employee_id
WHERE e1.name = 'Bob';
```

## Drill 7 - Same Department Coworker Count

**Dataset**

employees

| employee_id | name    | department |
| ----------- | ------- | ---------- |
| 1           | Alice   | IT         |
| 2           | Bob     | IT         |
| 3           | Charlie | HR         |
| 4           | David   | IT         |
| 5           | Eve     | HR         |

Goal:

| employee_name | coworker_count |
| ------------- | -------------: |
| Alice         |              2 |
| Bob           |              2 |
| Charlie       |              1 |
| David         |              2 |
| Eve           |              1 |

```
SELECT e1.name AS employee_name, COUNT(e2.employee_id) AS coworker_count
FROM employees e1
JOIN employees e2 ON e2.department = e1.department AND e1.employee_id != e2.employee_id
GROUP BY e1.employee_id, e1.name
```

## Drill 8

**Dataset**

customers

| customer_id | customer_name | referred_by |
| ----------- | ------------- | ----------- |
| 1           | Alice         | NULL        |
| 2           | Bob           | 1           |
| 3           | Charlie       | 1           |
| 4           | David         | 2           |
| 5           | Eve           | 2           |

Goal:

| customer_name | referrals |
| ------------- | --------: |
| Alice         |         2 |
| Bob           |         2 |

```
SELECT c2.customer_name, COUNT(c1.customer_id) AS referrals
FROM customers c1
JOIN customers c2 ON c1.referred_by = c2.customer_id
GROUP BY c2.customer_id, c2.customer_name;
```

## Drill 9

**Dataset**

employees

| employee_id | name    | manager_id |
| ----------- | ------- | ---------- |
| 1           | Alice   | NULL       |
| 2           | Bob     | 1          |
| 3           | Charlie | 1          |
| 4           | David   | 2          |
| 5           | Eve     | 2          |

Goal:

Return managers who manage at least 2 employees.

| manager_name |
| ------------ |
| Alice        |
| Bob          |

```
SELECT m.name
FROM employees e
JOIN employees m ON e.manager_id = m.employee_id
GROUP BY m.employee_id, m.name
HAVING COUNT(e.employee_id) >= 2;
```
