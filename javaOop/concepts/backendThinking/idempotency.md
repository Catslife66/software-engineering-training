# Idempotency

Scenario:

A client books a viewing:

```
POST /properties/123/viewings
```

But:

```
network timeout happens
```

The client does not know whether booking succeeded.

So the client retries the request.

Now your backend may accidentally create:

```
duplicate bookings
```

Problem

Without protection:

```
Request #1 → booking created
Request #2 → duplicate booking created
```

This happens a lot in real systems. Especially with:

- payments
- bookings
- checkout
- subscriptions

## Important Concept

```
Idempotency
=
same request repeated multiple times
still produces one safe result
```

## Common Solution

Client sends:

```
Idempotency-Key: abc-123
```

Backend stores:

```
key
→ previous successful result
```

If same request comes again:

```
return previous result
DO NOT create duplicate booking
```

### 1. Where should idempotency checking happen?

service layer, or a dedicated IdempotencyService called by the service

- which user is making the request
- what operation is being performed
- what result should be stored
- whether the request is still processing
- how to prevent duplicate business actions

### 2. What data should be stored?

```
IdempotencyRecord
- key
- userId
- operationType
- requestHash
- responseCode
- responseBody / result
- status: PROCESSING / COMPLETED / FAILED
- createdAt
- expiresAt
```

Important addition: requestHash

If the same key is reused with a different request body, that should be rejected.

Example:

```
same key + same request = return same result
same key + different request = 409 Conflict
```

### 3. Flow

- Client sends Idempotency-Key
- Controller receives request and key
- Controller calls BookingService.bookViewing(propertyId, request, key)

Inside service:

- check idempotency record by key + user
- if COMPLETED -> return previous result
- if PROCESSING -> return conflict / try later
- if not exists -> create PROCESSING record
- run booking transaction:
  - validate user/property/time
  - create booking
  - decrement slot
  - save outbox notification record
- save final Result into idempotency record as COMPLETED
- return Result
