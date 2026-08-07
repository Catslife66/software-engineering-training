# Business Invariants

## Drill 1 - Bank Transfer

```
public void transfer(
        Account from,
        Account to,
        BigDecimal amount
) {
    from.withdraw(amount);
    to.deposit(amount);
}
```

Questions:

```
1. What business facts must remain true?
A transfer may move money between accounts, but it must not create or destroy money.

2. What happens if withdrawal succeeds but deposit fails?
The money disappeared from the system and the transfer has violated the invariant.

3. What does correctness mean here?
The code can compile and every individual method can behave correctly, while the overall business transaction is still incorrect because a partial failure can leave the system in an invalid state.
```

```
Code correctness
≠
Business correctness
```

## Drill 2 - Inventory

```
public void purchase(Product product, int quantity) {
    product.reduceStock(quantity);
}
```

Suppose:

```
Current stock = 1

User A buys 1
User B buys 1
```

Both requests arrive almost simultaneously.

Questions:

```
1. What business invariant should always remain true for inventory?
Inventory must never become negative, and the system must never confirm the sale of more items than are available.

2. What invalid state might appear if both purchases succeed?
The current stock becomes a negative number.

3. Why is “both requests executed successfully” not enough to prove the system is correct?
Both requests may execute successfully from the application's perspective, but the final business state is incorrect because the system has oversold inventory.
```

```
Technical Success
≠
Business Success
```

## Drill 3 - Payment Processing

```
public void pay(Order order) {
    paymentGateway.charge(order.getTotal());
    order.markPaid();
}
```

Failure scenario:

```
Payment Gateway

✓ Charged customer
```

Immediately afterwards:

```
Application crashes
```

before:

```
order.markPaid();
```

Later...

The customer refreshes the page.

The browser retries the request.

Questions:

```
1. What business invariant should always remain true?
Each order should result in at most one successful charge unless an explicit retry or additional payment is intentionally created.

2. What business state exists after the crash?
Payment provider: charge = SUCCESSFUL
Order system: status = NOT PAID / PENDING

3. If the request is retried, what incorrect business outcome could happen?
The customer may be charged twice.

4. Why is this not simply a programming bug, but a distributed systems problem?
This is a distributed systems problem because the payment provider and the order system are independent components with separate state and separate failure boundaries. One operation can succeed while the other fails, creating a partial failure. The system therefore cannot rely on one atomic in-process operation to keep both states consistent.
```

```
separate state
+
separate failure boundaries
=
partial failure is possible
```

```
separate systems
→ independent success/failure
→ inconsistent business state
```

## Drill 4 - User Registration

```
public void register(User user) {
    repository.save(user);
    emailService.sendWelcomeEmail(user);
}
```

Scenario:

```
repository.save(user)          ✅
emailService.sendWelcomeEmail  ❌
```

Questions:

```
1. What business invariant should exist around user registration?
A user should not be registered more than once using the same unique identity (for example, email address).
Or
Once registration succeeds, the user must exist in the database.

2. Is “every successfully registered user must immediately receive a welcome email” necessarily part of that core invariant?
No, receiving welcome emails is not critical in registration request path.

3. Should the registration itself be considered failed because email delivery failed?
The registration should still be considered successful because the user has been persisted in the database, which is the source of truth. Email delivery is a side effect and can be retried later.

4. What does this tell you about the difference between a critical business operation and a side effect?
A failure in a critical business operation leaves the system in an incorrect business state. A failure in a side effect usually does not invalidate the primary business operation and can often be recovered asynchronously.
```

## Drill 5 - Order State

```
public void placeOrder(Order order) {
    reserveInventory(order);
    chargeCustomer(order);
    createOrder(order);
}
```

Now imagine this happens:

```
reserveInventory()   ✅
chargeCustomer()     ✅
createOrder()        ❌
```

The application crashes before the order record is created.

Questions:

```
1. What business invariants should this workflow protect?
If an order is successfully placed, inventory must be reserved, the customer must be charged, and the order must exist. These business operations should appear atomic: either the workflow completes successfully, or compensating actions restore the system to a valid state.

2. What business state exists after the crash?
Inventory
✓ Reserved

Payment
✓ Charged

Order
✗ Missing

3. Who is affected by this inconsistency (customer, warehouse, business, or all of them)?
Customer
Paid.
No order.
Terrible experience.

Warehouse
Inventory disappeared.
No order to ship.
They don't know what happened.

Business
Customer support tickets.
Refunds.
Manual investigation.
Loss of trust.
Possibly legal issues.

4. Why is this another example of a partial failure rather than simply "a bug"?
Because different steps in the workflow can succeed or fail independently.
```

```
Technical Atomicity
↓
One database transaction

──────────────

Business Atomicity
↓
Customer experiences

"All or Nothing"

even if multiple services are involved.
```

## Drill 6 - Final Invariant Review

This is your "mini architecture review."

A teammate proposes this checkout workflow:

```
1. Reserve inventory
2. Charge payment
3. Create order
4. Send confirmation email
5. Publish analytics event
```

Now imagine these failures:

```
Inventory succeeds.
Payment succeeds.
Order succeeds.
Email fails.
Analytics event fails.
```

Questions:

```
1. Which steps are part of the core business operation?
Inventory, payment and order

2. Which steps are side effects?
Email and analytics

3. Is the business still in a correct state?
Yes

4. Should the customer see "Order failed"?
No

5. If you had to retry something later, what would you retry, and why?
Retry email and analytics because they are side effects that can be recovered asynchronously.
```
