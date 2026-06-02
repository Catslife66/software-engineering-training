# Database Isolation Levels

Think of transactions as rooms

Imagine:

```
Transaction A
Transaction B
```

working on the same data.

Isolation level decides:

```
How much A can see of B's work
```

## 1. Read Uncommitted - Dirty Read

Lowest isolation.

Possible:

```
Transaction A changes value
but has NOT committed

Transaction B reads it anyway
```

Example:

```
Balance = £100

A changes balance to £0
(not committed)

B reads £0

A rolls back

Balance is actually £100
```

B read fake data.

This is called: **Dirty Read**

## 2. Read Committed - Non-repeatable Read

Most common default in many systems.

Rule:

```
Only read committed data
```

No dirty reads.

But another problem can happen.

Example:

```
Transaction A reads balance = £100

Transaction B updates balance = £200
commits

Transaction A reads again
now sees £200
```

Same transaction.

Different result.

This is **Non-repeatable Read**

## 3. Repeatable Read - Phantom Read

Rule:

```
If transaction reads a row,
it keeps seeing the same row values.
```

Now:

```
A reads £100

B updates to £200

A reads again
still sees £100
```

Good.

But another issue remains.

Example:

```
A:
SELECT all bookings today

returns:
5 rows
```

Meanwhile:

```
B inserts a new booking
commits
```

Now:

```
A runs same query again

returns:
6 rows
```

The extra row is a **Phantom**

## 4. Serializable

Highest isolation.

Database behaves as if:

```
Transactions execute one at a time.
```

Most correct.

Most expensive.

Most restrictive.

## Summary

| Level            | Prevents                           |
| ---------------- | ---------------------------------- |
| Read Uncommitted | almost nothing                     |
| Read Committed   | dirty reads                        |
| Repeatable Read  | dirty reads + non-repeatable reads |
| Serializable     | almost all concurrency anomalies   |
