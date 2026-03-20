# Database Design

## Entities

A table represents an entity.
An entity is a real-world object we want to store data about.

Example:
| Entity | Table |
| -------- | --------- |
| Customer | customers |
| Product | products |
| Order | orders |

customers table
| customer_id | name | city |
| ----------- | ----- | ---------- |
| 1 | Alice | London |
| 2 | Bob | Manchester |

Columns represent attributes of the entity.

## Primary Key

Every table needs a primary key.

Properties of a primary key:

- unique
- not NULL
- stable (does not change)

## Foreign Key

A foreign key connects two tables.

Example:
orders.customer_id → customers.customer_id

## Relationship types

Relational databases model relationships between entities.

- One-to-Many
  One customer can have many orders.

- Many-to-Many
  One order can contain many products.
  One product can appear in many orders.
  This requires a **join table**.

## Normalization

### What problem does normalization solve?

Example:

```
| order_id | customer_name | customer_city | product_name | price |
| -------- | ------------- | ------------- | ------------ | ----- |
| 1        | Alice         | London        | Keyboard     | 50    |
| 2        | Alice         | London        | Mouse        | 20    |
| 3        | Bob           | Manchester    | Monitor      | 200   |
```

**Problems with this design**

1. Data duplication

   Alice, London appears multiple times

2. Update problem - **Update anomaly**

   If Alice moves from London to Bristol → we must update multiple rows.

   If we miss one → inconsistent data.

3. Insert problem - **Insert anomaly**

   What if we want to add a new customer with no orders?

   We cannot because this table requires an order.

4. Delete problem - **Delete anomaly**

   If we delete Alice’s last order: we lose Alice completely

### Solution: Normalize the data

We split the table into separate entities.

customers

```
| customer_id | name | city |
```

products

```
| product_id | product_name | price |
```

orders

```
| order_id | customer_id |
```

order_items

```
| order_id | product_id | quantity |
```

- No duplication

  Customer info stored once

- Safe updates

  Change Alice’s city in one place

- Flexible inserts

  Can add customers without orders

- Safe deletes

  Deleting an order doesn’t delete the customer

### Key idea of normalization

Each piece of data should live in ONE place

Examples:

- customer city belongs in `customers`
- product price belongs in `products`
- which customer made an order belongs in `orders`
- which products are in an order belongs in `order_items`

### Why this matters in real systems

Bad schema design causes:

- inconsistent data
- hard-to-maintain code
- slow queries
- bugs in production
- Good schema design makes everything easier.

## Natural keys vs Surrogate keys

### Natural key

> A natural key is a real-world value that already exists in the business domain.

Examples:

- email
- ISBN
- national insurance number
- username

### Surrogate key

> A surrogate key is an artificial key created only for the database.

Usually:

- integer ID
- UUID

Examples:

- customer_id
- order_id
- product_id

These do not have business meaning. They only identify rows.

### Why surrogate keys are common

Because natural keys can change.

Example:

- a user changes email
- a product code changes
- a phone number changes

If the primary key changes, that can create complications across related tables.

Surrogate keys are:

- stable
- simple
- small
- easy to reference

That is why most production systems use them.

### But natural keys are still useful

Even if email is not the primary key, it may still need a UNIQUE constraint.

Example:

users

```
| user_id | email | name |
```

Rules:

user_id = primary key

email = unique

This gives:

- stable internal identity
- real-world uniqueness where needed
