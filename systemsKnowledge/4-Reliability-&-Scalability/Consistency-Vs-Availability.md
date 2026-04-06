# Consistency vs availability

Understand a core tradeoff in distributed systems: **you cannot have everything at the same time**

## The Core Idea (Simplified)

In distributed systems, you must balance:

```
Consistency
Availability
```

**Consistency**

All users see the same data at the same time

Example:

```
You update your profile → everyone sees it immediately
```

**Availability**

System always responds (even if data is not perfect)

Example:

```
System returns data even if it's slightly outdated
```

## The Tradeoff (under failure)

During failures:

```
You often must choose one
```

Example Scenario

```
Primary DB crashes
Replica is slightly outdated
```

Now you have two choices:

**Option A — Consistency**

```
Wait until all nodes agree on the latest data
→ system may be unavailable
```

**Option B — Availability**

```
Use replica immediately
→ system works but data may be stale
```

## Real Systems

**Consistency-first (Banking)**

```
correctness > availability
```

**Availability-first (Social Media)**

```
availability > perfect accuracy
```

**Key Insight**

```
Consistency = correctness → "always correct"
Availability = responsiveness → "always responds"
```
