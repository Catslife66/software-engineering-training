# Transaction Propagation

One Transaction

Everything we've done so far looks like this:

```
Controller
    ↓
PropertyService
    ↓
Repository
```

One transaction.

```
START
    ↓
Business Logic
    ↓
COMMIT
```

Easy.

But Real Systems Look Like This

```
Controller
    ↓
PropertyService
    ↓
AuditService
```

Now we have:

```
propertyService.createProperty(...)
```

calling:

```
auditService.record(...)
```

Question:

```
Should there be:
One transaction?

OR

Two transactions?
```

That's exactly what propagation decides.

Example

```
@Service
public class PropertyService {

    @Transactional
    public void createProperty(...) {

        propertyRepository.save(property);

        auditService.recordCreation(property);

    }
}
```

and:

```
@Service
public class AuditService {

    @Transactional
    public void recordCreation(...) {

        auditRepository.save(...);

    }
}
```

Question

When:

```
PropertyService
```

calls:

```
AuditService
```

what should happen?

## Spring propagation terms

Propagation decides whether a method joins an existing transaction or starts a different one.

Default Spring behavior is usually:

```
Propagation.REQUIRED
```

Meaning:

```
If a transaction already exists, join it.
If no transaction exists, start one.
```

So in this code:

```
@Transactional
public void createProperty() {
    propertyRepository.save(property);
    auditService.recordCreation(property);
}
```

and:

```
@Transactional
public void recordCreation(...) {
    auditRepository.save(audit);
}
```

by default, recordCreation() joins the existing transaction.

So default behavior is:

```
one shared transaction
```

To make audit independent, we need a different propagation mode later, such as:

```
REQUIRES_NEW
```

## Propagation Mode 1 — REQUIRED (Default)

This is the default for:

```
@Transactional
```

Spring treats it as:

```
@Transactional(propagation = Propagation.REQUIRED)
```

Rule:

```
If a transaction exists
    ↓
Join it

If no transaction exists
    ↓
Create one
```

Example

```
@Transactional
public void createProperty() {

    propertyRepository.save(property);
    auditService.recordCreation();
}

@Transactional
public void recordCreation() {

    auditRepository.save(...);

}
```

Flow:

```
START Transaction
       ↓
Save Property
       ↓
Audit joins same transaction
       ↓
Save Audit
       ↓
COMMIT
```

Only:

```
One transaction
```

## Propagation Mode 2 — REQUIRES_NEW

This one is very different.

Rule:

```
Always create a brand new transaction.
```

Even if another transaction already exists.

Example:

```
@Transactional
public void createProperty() {

    propertyRepository.save(property);
    auditService.recordCreation();

}
@Transactional(propagation = REQUIRES_NEW)
public void recordCreation() {

    auditRepository.save(...);

}
```

Flow:

```
Transaction A starts
        ↓
Save Property
        ↓
Suspend Transaction A
        ↓
Start Transaction B
        ↓
Save Audit
        ↓
Commit Transaction B
        ↓
Resume Transaction A
        ↓
Commit Transaction A
```

Now we have:

```
Two completely separate transactions.
```

## Big Picture

**REQUIRED**

```
One business action

One transaction

Everything succeeds or fails together.
```

**REQUIRES_NEW**

```
Nested business action

Independent transaction

Failure isolation.
```

## Better design for non-critical work

Example

```
@Transactional
public void createProperty(...) {

    propertyRepository.save(property);

    auditService.record(...); // REQUIRES_NEW

    analyticsService.record(...); // REQUIRES_NEW
}
```

If auditService.record(...) throws and nobody catches it:

```
Audit transaction rolls back.
Exception escapes to PropertyService.
Property transaction also rolls back.
```

REQUIRES_NEW makes the audit/analytics transaction independent,
but if it throws an exception and you don’t catch it,
that exception can still escape back to PropertyService and cause the property transaction to roll back.

So REQUIRES_NEW alone does not protect the main workflow.

So a better design:

```
@Transactional
public void createProperty(...) {
    propertyRepository.save(property);

    try {
        auditService.record(...); // REQUIRES_NEW
    } catch (Exception e) {
        // log, do not rethrow if non-critical
    }

    try {
        analyticsService.record(...); // REQUIRES_NEW
    } catch (Exception e) {
        // log, do not rethrow if optional
    }
}
```

Now:

```
Property transaction can commit.
Audit/analytics can fail independently.
Failure does not escape.
```

Key point:

```
REQUIRES_NEW isolates transactions.
It does not automatically swallow exceptions.
```

**Important warning**

Do not use try/catch blindly.

Only catch and continue when the business says:

```
This failure should not stop the main action.
```

For critical steps, let the exception escape so the transaction rolls back.
