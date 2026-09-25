# Session 9 - Encapsulation + object collaboration

Last time, we established an important principle:

> An object should protect the invariants of the state it owns.

Our BankAccount did this:

```
class BankAccount {
    private double balance;

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

Today we'll take one small step further:

> What happens when two objects collaborate in one operation?

## 1. The requirement

Suppose we want:

```
transfer(source, destination, 50);
```

Meaning:

```
Alice account: £100
Bob account:    £40

transfer £50

Alice account: £50
Bob account:   £90
```

We could create:

```
public static boolean transfer(
        BankAccount source,
        BankAccount destination,
        double amount) {
    // ...
}
```

But here's the engineering question:

**Who should actually modify the balances?**

We don't want `transfer()` doing this:

```
source.balance -= amount;       // ❌
destination.balance += amount;  // ❌
```

Apart from `balance` being private, this would bypass the behaviour that each object provides.

Instead, the objects should expose operations:

```
source.withdraw(amount);
destination.deposit(amount);
```

## 2. Add deposit()

The account can protect this operation too:

```
public boolean deposit(double amount) {
    if (amount <= 0) {
        return false;
    }

    balance += amount;
    return true;
}
```

Now the account owns both ways its balance can legitimately change:

```
             BankAccount
                  |
            owns balance
             /         \
       withdraw()     deposit()
           |             |
      decrease       increase
       safely         safely
```

This is encapsulation as **controlled state transition**, not simply `private` fields.

## 3. Building transfer()

Now consider:

```
public static boolean transfer(
        BankAccount source,
        BankAccount destination,
        double amount) {

    if (!source.withdraw(amount)) {
        return false;
    }

    destination.deposit(amount);
    return true;
}
```

Read this as an engineer:

```
Ask source account to withdraw
            ↓
        rejected?
        → transfer fails

        accepted?
            ↓
Ask destination account to deposit
            ↓
transfer succeeds
```

Notice something important.

`transfer()` doesn't need to know:

```
source.balance
```

It asks the object to perform an operation:

```
source.withdraw(amount)
```

That's an OOP idea you'll encounter repeatedly:

> Tell an object what operation to perform rather than reaching inside it and manipulating its state.

## 4. Responsibilities

We now have two levels of responsibility.

`BankAccount` is responsible for:

```
Is this particular balance change valid?
```

`transfer()` is responsible for:

```
How do I coordinate two accounts to perform a transfer?
```

So:

```
BankAccount
→ protects local object state

transfer()
→ coordinates multiple objects
```

Later, when you study service layers, transactions and databases, this distinction becomes extremely important.

## Exercise

Imagine:

```
class InventoryItem {

    private String name;
    private int stock;

    public InventoryItem(String name, int stock) {
        this.name = name;
        this.stock = stock;
    }

    public int getStock() {
        return stock;
    }

    public boolean removeStock(int quantity) {
        if(quantity <= 0){
            return false;
        }
        if(stock < quantity){
            return false;
        }
        stock -= quantity;
        return true;
    }

    public boolean addStock(int quantity) {
        if(quantity <= 0){
            return false;
        }
        stock += quantity;
        return true;
    }

    // Assume both objects represent the same product at different warehouses.
    public static boolean transferStock(
        InventoryItem source,
        InventoryItem destination,
        int quantity) {

        if(!source.removeStock(quantity)){
            return false;
        }
        destination.addStock(quantity);
        return true;
    }
}
```

Business rules:

```
quantity must be > 0

stock must never become negative
```

```
State:
Each `InventoryItem` maintains its current stock.

Invariant:
stock >= 0

removeStock preconditions:
quantity > 0
quantity <= stock

addStock preconditions:
quantity > 0:

Responsibility:
InventoryItem
→ protects its own stock invariant

transferStock()
→ coordinates source and destination
```
