# Session 8 - Encapsulation

Last session we had an inventory operation:

```
product.reduceStock(quantity);
```

and identified the business invariant:

```
A product's stock must never become negative.
```

There's a design problem, though. Consider this Product:

```
class Product {
    private int stock;

    public void reduceStock(int quantity) {
        stock -= quantity;
    }
}
```

Any caller can accidentally do:

```
product.reduceStock(1000);
```

even when only 5 items are available.

We could check before every call:

```
if (product.getStock() >= quantity) {
    product.reduceStock(quantity);
}
```

But now every caller has to remember the rule.

A stronger OOP design is:

> The object that owns the state should help protect its invariants.

## 1. Move validation into Product

We can redesign the method:

```
public boolean reduceStock(int quantity) {
    if (quantity <= 0) {
        return false;
    }

    if (quantity > stock) {
        return false;
    }

    stock -= quantity;
    return true;
}
```

Now Product protects:

```
quantity > 0
stock >= 0
```

The important part isn't merely that stock is private.

Encapsulation means controlling **how state is allowed to change**.

Instead of exposing:

```
public void setStock(int stock) {
    this.stock = stock;
}
```

we expose a meaningful domain operation:

```
reduceStock(quantity)
```

Compare:

```
setStock(-50)
→ technically changes a field

reduceStock(3)
→ expresses a business operation
```

The second tells us much more about what the object is supposed to do.

## 2. What happens to the service algorithm?

Previously we might have written:

```
Product product = productsById.get(productId);

if (product == null) {
    return false;
}

if (product.getStock() < quantity) {
    return false;
}

product.reduceStock(quantity);

return true;
```

Now Product owns the stock rule:

```
Product product = productsById.get(productId);

if (product == null) {
    return false;
}

return product.reduceStock(quantity);
```

Notice the separation of responsibilities:

```
purchase operation
        ↓
find the correct Product

Product
        ↓
decide whether its stock
can be reduced safely
```

This is a small example of designing responsibilities rather than putting all logic into one method.

## Exercise

Consider:

```
class BankAccount {

    private double balance;

    public BankAccount(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public boolean withdraw(double amount) {

        if (amount <= 0) {
            return false;
        }

        if (amount > balance) {
            return false;
        }

        balance -= amount;
        return true;
    }
}
```

```
ALGORITHMIC THINKING

state
→ balance

invariant
→ balance >= 0

transition
→ balance = balance - amount

preconditions
→ amount > 0
→ amount <= balance

              ↓

OOP THINKING

BankAccount owns balance
        ↓
balance is private
        ↓
withdraw() controls the transition
        ↓
invalid transition rejected
        ↓
object preserves its invariant
```
