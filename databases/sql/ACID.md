# ACID properties

**A — Atomicity**

All or nothing

**C — Consistency**

Database rules are always valid

**I — Isolation**

Transactions don’t interfere

**D — Durability**

Committed data is permanent

Example: money transfer

```
Transfer £100 from A → B
```

Atomicity -> Both accounts update OR neither

Consistency -> Total money remains correct

Isolation -> Two transfers don’t conflict

Durability -> Once done, it stays done

## Why this matters (very important)

Without ACID:

- money can disappear ❌
- duplicate payments can happen ❌
- data becomes inconsistent ❌

This is critical in:

- banking systems
- ecommerce payments
- booking systems

Imagine this situation:

Two users try to transfer money at the same time:

```
User A: transfer £100
User B: transfer £100
```

Without proper control:

```
balance = £1000
→ both read 1000
→ both subtract 100
→ final = £900 ❌ (wrong)
```

This is a **race condition.**

### SQL level understanding

A transaction looks like this:

```
BEGIN;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1;

UPDATE accounts
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

If something fails:

```
ROLLBACK;
```

> Transaction = a safe unit of work

## Concurrency problem

Initial balance: £1000

Two transactions run:

```
T1 reads → 1000
T2 reads → 1000
T1 writes → 900
T2 writes → 900
```

Final result: £900 ❌ (should be £800)

This is called: **Lost Update Problem**

## Summary

You correctly mapped real-world problems to ACID:

| Scenario              | Property    |
| --------------------- | ----------- |
| Partial transfer      | Atomicity   |
| Double booking        | Isolation   |
| Data lost after crash | Durability  |
| Invalid data state    | Consistency |
