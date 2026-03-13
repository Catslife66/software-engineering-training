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
  This requires a join table.
