# Transactions & Consistency

Scenario — Booking a Viewing

```
POST /properties/{propertyId}/viewings
```

When a booking succeeds, the system should:

1. create viewing booking
2. save booking
3. decrement available viewing slots
4. create notification record
5. send confirmation email

What happens if:

```
booking saved successfully
BUT
notification save fails?
```

OR

```
booking saved
slots decremented
BUT
email sending crashes?
```

Now your database may become inconsistent.

## Booking Flow

Inside transaction:

- save booking
- decrement slot
- save notification/outbox record (if you store notification intent)

Outside transaction:

- actually send email
- retry if sending fails

## Transaction management

The service knows which operations must succeed together because they represent one business operation.

Either all related state changes succeed together or all fail together. Otherwise the system can become inconsistent.

So transaction boundary should wrap the service method.

```
@Transactional
public Result<Void> bookViewing(...) {
    ...
}
```

## Outbox Pattern — reliable email after transaction

Problem

Bad flow:

```
1. save booking
2. decrement slot
3. send email
```

If email fails, the system may forget to retry.

The Outbox Pattern solves:

```
Database commit succeeded
BUT
external action failed
```

Without a durable record, the job can be lost forever.

**Better flow**

Inside transaction:

1. save booking
2. decrement slot
3. save email task into outbox table

After transaction:

```
background worker reads outbox
sends email
marks outbox item as sent
retries failed ones
```

### Outbox Model

```
OutboxMessage
- id
- type
- recipient
- payload/message
- status: PENDING / SENT / FAILED
- createdAt
- sentAt
```

### Service Flow

```
@Transactional
bookViewing(...)

- validate user/property/time
- create booking
- decrement available slot
- save booking
- save OutboxMessage("VIEWING_CONFIRMATION_EMAIL")
- return success
```

Notice: No actual email sending inside transaction

### Background job flow

```
EmailWorker

- find pending outbox messages
- send email
- if success -> mark SENT
- if fail -> keep PENDING or mark FAILED for retry
```

### Why this is good

```
Database state stays consistent
Email sending becomes reliable
External failure does not break booking
Retries are possible
```
