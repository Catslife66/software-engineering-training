# Custom Validation

There are two common choices:

Option 1: custom Bean Validation annotation

Option 2: service-level validation

## Option 1 — Custom annotation

Example idea:

```
@FutureViewingTime
public class CreateViewingRequest {
    private LocalDate viewingDate;
    private LocalTime viewingTime;
}
```

This means:

```
The whole request object must pass a custom rule.
```

The validator checks:

```
combine viewingDate + viewingTime
compare with current date/time
```

Good when the rule is really part of the API request contract.

## Option 2 — Service validation

Inside service:

```
if (viewingDateTime.isBefore(LocalDateTime.now())) {
    throw new InvalidViewingTimeException();
}
```

Good when the rule depends on business context, database state, user role, or system configuration.

## Rule of thumb

```
Pure request shape rule → DTO validation
Business/context rule → Service validation
```

For “viewing cannot be in the past”, either can be acceptable, but I’d often put it in the service if booking rules may grow later.

Example future rules:

```
viewings only allowed Mon–Sat
must be at least 24 hours ahead
agent must have availability
property must be active
```

Those are business rules, so service becomes a better home.

## Complete Request Lifecycle

Imagine this endpoint:

```
@PostMapping("/viewings")
public ResponseEntity<ViewingDTO> createViewing(
    @Valid @RequestBody CreateViewingRequest request
) {
    return ResponseEntity.ok(
        viewingService.createViewing(request)
    );
}
```

Step 1 — HTTP Request

Client sends:

```
{
    "propertyId": "...",
    "viewingDate": "2026-08-01",
    "visitorEmail": "john@example.com",
    "numberOfVisitors": 2
}
```

Step 2 — JSON → DTO

Spring executes:

```
@RequestBody
```

Result:

```
CreateViewingRequest
```

Now we have a Java object.

Step 3 — Structural Validation

Spring sees:

```
@Valid
```

and checks:

```
✓ propertyId exists?

✓ visitorEmail valid?

✓ numberOfVisitors between 1 and 6?

✓ viewingDate exists?
```

If any of these fail:

```
400 Bad Request
```

Controller is **never called**.

Step 4 — Controller

If validation succeeds:

```
viewingService.createViewing(request);
```

Notice something important.

The service can now assume:

```
propertyId is not null

email format is valid

visitor count is valid
```

The service **doesn't need to check those again**.

This keeps the business logic clean.

Step 5 — Business Validation

Now the service asks questions like:

```
Does the property exist?

Is it ACTIVE?

Is the current user allowed to book?

Is the slot already reserved?
```

These require:

- database queries
- business rules
- authorization

If one fails:

```
403 Forbidden

404 Not Found

409 Conflict

...
```

depending on the situation.

Step 6 — Transaction

Only after all validation passes:

```
Begin transaction

Reserve slot

Create viewing

Save outbox message

Commit
```

## The Entire Backend Pipeline

This is now your complete mental model for a typical write request.

```
HTTP Request
        │
        ▼
@RequestBody
(JSON → DTO)
        │
        ▼
@Valid
(Structural validation)
        │
        ▼
Controller
        │
        ▼
Service
(Business validation)
        │
        ▼
@Transactional
        │
        ▼
Repository
        │
        ▼
Database
        │
        ▼
HTTP Response
```
