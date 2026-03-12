# SQL Foundations

## What a relational database is

A relational database stores data in tables.
Each **row** is one record.
Each **column** is one attribute of that record.

## SELECT

`SELECT` is used to retrieve data from a table.

```
SELECT * FROM users;

SELECT name, email FROM users;

```

## WHERE

`WHERE` filters rows.

```
SELECT name, email
FROM users
WHERE id = 2;
```

## Common operators

```
=      equal
!=     not equal
>      greater than
<      less than
>=     greater than or equal
<=     less than or equal
```

```
SELECT *
FROM orders
WHERE user_id = 1 AND total > 30;

SELECT *
FROM users
WHERE name = 'Alice' OR name = 'Bob';
```

## ORDER BY

`ORDER BY` sorts the result.

```
SELECT *
FROM users
ORDER BY name ASC, id DESC;
```
