# E-commerce System

Design an E-commerce System

We’ll design a simple system like:

```
browse products → add to cart → checkout → pay → create order
```

## Requirements

**Functional requirements**

```
1. Users can browse products
2. Users can view product detail
3. Users can add/remove items from cart
4. Users can place an order
5. Users can pay
6. Users can view order history
```

**Non-functional requirements**

```
1. Product browsing should be fast
2. Checkout must be reliable
3. Payment must be secure
4. Inventory must be consistent
5. System should scale during traffic spikes
```

## High-level components

```
Client
↓
Load Balancer
↓
API Servers
↓
Product Service
Cart Service
Order Service
Payment Service
Inventory Service
Notification Service
↓
Database
Cache
Queue
Payment Provider
```

architecture:

```
Client
↓
API / Backend
↓
Product DB / Order DB / Inventory DB
↓
Queue → Email / Notifications / Analytics
↓
Payment Provider
```

## Product browsing flow

Product pages are usually **read-heavy**.

```
User opens product page
↓
API checks cache
↓
Cache hit → return product quickly
↓
Cache miss → query DB → store in cache → return
```

Why cache helps:

```
many users view same products
database load reduced
faster response
```

Good cache candidates:

```
product details
category pages
homepage featured products
```

## Cart flow

Cart can be stored in:

```
client/browser
database
Redis/session store
```

For logged-in users, database or Redis-backed cart is safer.

Flow:

```
User adds item
↓
Cart Service validates product
↓
Cart updated
↓
Return updated cart
```

Important: cart is not the final source of truth for price/inventory.

At checkout, the system must re-check:

```
current price
stock availability
discount validity
```

## Checkout flow

Checkout is the critical flow.

```
1. User clicks checkout
2. Server validates cart
3. Inventory is reserved or checked
4. Order is created with status = pending
5. Payment request sent to payment provider
6. Payment succeeds
7. Order status = paid
8. Inventory confirmed/deducted
9. Confirmation email goes to queue
```

Key rule:

```
Payment and order creation must be reliable.
Email/analytics can be async.
```

A good checkout flow should not immediately say “paid” or “completed”.

Use states.

```
cart
↓
checkout_started
↓
order_pending_payment
↓
payment_processing
↓
paid
↓
fulfilled
```

A safer flow:

```
1. User clicks Checkout

2. Backend reads cart from server-side cart store / DB

3. Backend re-validates:
   - product still exists
   - price is current
   - discount still valid
   - inventory available
   - user address valid

4. Backend reserves inventory with expiry

5. Backend creates order with status = pending_payment

6. Backend creates payment session with provider

7. User pays via provider

8. Provider sends webhook to backend

9. Backend verifies webhook

10. Backend marks order = paid

11. Backend confirms inventory deduction

12. Backend queues confirmation email / warehouse notification
```

## Payment flow

Usually we do not process card details directly.

Use external provider:

```
Stripe / PayPal / Adyen
```

Flow:

```
1. Backend creates pending order

2. Backend creates payment session with payment provider

3. User completes payment on provider page

4. Provider redirects user back to frontend

5. Provider also sends webhook to backend

6. Backend trusts webhook, not redirect

7. Backend verifies webhook signature

8. Backend updates order status
```

Why webhook matters:

```
client redirect is not trustworthy
payment success must come from payment provider
```

### Webhook handling

Payment providers send events like:

```
payment_succeeded
payment_failed
checkout_expired
refund_created
```

Webhook flow:

```
1. Payment provider sends POST /webhooks/payment

2. Backend verifies signature

3. Backend checks event_id has not been processed before

4. Backend finds related order/payment

5. Backend updates order status

6. Backend confirms or releases inventory

7. Backend stores webhook event

8. Backend returns 200 OK
```

## Inventory reservation

Inventory reservation means:

```
temporarily hold stock while payment is in progress
```

Example:

```
Product A stock = 10
User starts checkout for quantity 2
Available stock becomes 8
Reserved stock becomes 2
```

A simple inventory model:

```
product_id
stock_quantity
reserved_quantity
```

Available stock:

```
available = stock_quantity - reserved_quantity
```

When checkout starts:

```
if available >= requested_quantity:
    reserved_quantity += requested_quantity
else:
    reject checkout
```

When payment succeeds:

```
stock_quantity -= requested_quantity
reserved_quantity -= requested_quantity
```

When payment fails or expires:

```
reserved_quantity -= requested_quantity
```

**Reservation expiry**

Reservation cannot last forever.

Use expiry:

```
reserved_until = now + 15 minutes
```

If user does not pay in time:

```
reservation expires
stock becomes available again
```

A background job can clean expired reservations:

```
every minute:
    find expired reservations
    release stock
```

Mental model:

```
reservation = temporary lock on stock
```

## Inventory problem

This is one of the hardest parts.

Example:

```
Only 1 item left
Two users checkout at same time
```

Possible problem:

```
both users buy same item ❌
```

Solutions:

**Option A — Database transaction + row lock**

Use when inventory is in a relational DB.

Flow:

```
BEGIN TRANSACTION

SELECT inventory
WHERE product_id = ?
FOR UPDATE

check available stock

update reserved_quantity

COMMIT
```

Why it works:

```
only one checkout can update that product row at a time
```

This prevents two users reserving the same last item.

Tradeoff:

```
safe but can reduce throughput on hot products
```

**Option B — Atomic update**

Instead of read then write separately, do one safe update:

```
UPDATE inventory
SET reserved_quantity = reserved_quantity + 1
WHERE product_id = ?
AND stock_quantity - reserved_quantity >= 1;
```

Then check affected rows:

```
1 row updated → reservation success
0 rows updated → not enough stock
```

This is often clean and efficient.

**Option C — Inventory reservation table**

Instead of only updating counters, create reservation records.

```
reservation_id
order_id
product_id
quantity
status
expires_at
```

Statuses:

```
active
confirmed
expired
cancelled
```

Benefits:

```
auditable
easier to recover
supports expiry
```

The inventory count can be derived or maintained with counters.

**Option D — Queue-based inventory processing**

For extreme scale or flash sales:

```
checkout request → inventory queue → single processor per product
```

This serializes inventory updates.

Benefit:

```
prevents race conditions
```

Tradeoff:

```
adds delay and complexity
```

## Queue usage

Use queues for non-critical or slow tasks:

```
send order confirmation email
send invoice
update analytics
notify warehouse
recommendation updates
```

Do not put critical payment verification in a queue unless carefully designed.

Mental model:

```
critical path → synchronous
side effects → queue
```

## Reliability concerns

**Database slow**

```
checkout becomes slow
orders may fail
```

Mitigation:

```
timeouts
connection pooling
indexes
read replicas for product reads
```

**Cache down**

```
product reads hit DB
DB load increases
```

Mitigation:

```
rate limiting
fallback
cache replication
```

**Payment webhook delayed**

```
order remains pending
```

Mitigation:

```
retry webhook processing
reconciliation job
manual review for stuck orders
```

**Duplicate payment webhook**

Payment providers may send the same webhook more than once.

Mitigation:

```
idempotency using provider event ID
unique constraint
```

**Payment succeeds but inventory confirmation fails**

Example:

```
payment_succeeded webhook received
DB update fails
```

Mitigations:

```
1. Store webhook event first
2. Process with retry
3. Use transaction for order update + inventory update
4. Alert if paid order not confirmed
5. Reconciliation job
```

Flow:

```
receive webhook
↓
store event
↓
process event
↓
if processing fails, retry from stored event
```

**Inventory reserved but payment fails**

Problem:

```
stock remains locked
```

Mitigations:

```
1. reservation expiry
2. payment_failed webhook releases reservation
3. checkout_expired webhook releases reservation
4. background cleanup job
```

Rule:

```
every reservation must have an expiry
```

**User refreshes checkout / submits twice**

Problem:

```
duplicate order
duplicate payment session
```

Mitigation:

```
idempotency key
```

Client sends:

```
checkoutRequestId
```

Backend enforces:

```
same user + same checkoutRequestId = same order
```

Payment providers often support idempotency keys too.

**Inventory race condition**

Problem:

```
two users buy last item
```

Mitigations:

```
1. DB transaction + lock
2. Atomic conditional update
3. Unique reservation logic
4. Queue-based serialization for flash sales
```

**Queue grows continuously**

Problem:

```
emails/warehouse/analytics delayed
```

Mitigations:

```
1. Add workers
2. Split priority queues
3. Retry with backoff
4. Dead letter queue
5. Monitor queue depth and age
```

Important metric:

```
queue age
```

Not just queue size.

A queue of 10,000 jobs may be fine if processing fast.

A queue where oldest job is 2 hours old is a problem.

**Dead letter queue**

If a job keeps failing:

```
retry
retry
retry
```

Eventually move it to:

```
dead letter queue
```

Purpose:

```
avoid infinite retry loop
allow manual investigation
```

Example:

```
send invoice failed 10 times
→ move to DLQ
→ alert engineer
```

## Security

Important protections:

```
HTTPS everywhere
passwords hashed with bcrypt
authentication for user accounts
authorization for order access
payment handled by trusted provider
webhook signature verification
rate limiting
```

Key point:

```
Never trust client-side price, stock, or payment status.
```

## Final architecture summary

```
Browse products:
Client → API → Cache → Product DB

Add to cart:
Client → API → Cart Service → Cart Store

Checkout:
Client → API → Order Service → Inventory → Payment Provider

Payment confirmation:
Payment Provider → Webhook → Order Service → update order

Async:
Order Service → Queue → Email / Notification / Analytics
```

## Key mental model

```
Browsing = performance problem
Checkout = consistency problem
Payment = trust problem
Inventory = concurrency problem
Notifications = async problem
```
