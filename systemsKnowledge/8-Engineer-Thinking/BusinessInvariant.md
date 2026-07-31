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

1. What business facts must remain true?
2. What happens if withdrawal succeeds but deposit fails?
3. What does correctness mean here?

## Drill 2 - Inventory

```
public void purchase(Product product, int quantity) {
    product.reduceStock(quantity);
}
```

Questions:

1. What invariant protects stock?
2. What happens if two customers buy the last item?
3. What incorrect state must never appear?

## Drill 3 - Payment Processing

```
public void pay(Order order) {
    paymentGateway.charge(order.getTotal());
    order.markPaid();
}
```

Questions:

1. What happens if charging succeeds but markPaid() fails?
2. What happens if the request is retried?
3. What invariant protects the customer?

## Drill 4 - User Registration

```
public void register(User user) {
    repository.save(user);
    emailService.sendWelcomeEmail(user);
}
```

Questions:

1. Must every saved user receive an email?
2. Is email delivery part of the core invariant?
3. What happens if email sending fails?

## Drill 5 - Order State

```
order.markShipped();
order.cancel();
```

Questions:

1. Can an order be both shipped and cancelled?
2. What state transitions are valid?
3. What invariant protects the order lifecycle?

## Drill 6 - Final Invariant Review

You will receive a larger service containing:

- database writes,
- payment,
- stock,
- messages,
- and retries.

Your task will be to identify:

1. the states,
2. the invariants,
3. the failure points,
4. and which failures are technical versus business failures.
