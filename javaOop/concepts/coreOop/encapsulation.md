# Encapsulation

> Hide internal data and control how it changes

Bundling data and behavior together while protecting the internal state of the object.
Encapsulation allows the class to enforce rules and maintain consistency.

The class controls how its internal state changes. External code cannot modify the internal state directly, so all updates must go through methods to enforce business rules.

In practice this means:

- fields are private (`private` fields)
- access is controlled through methods (getters/setters)
- validation inside methods

```
public class BankAccount {

    private double balance;

    public double getBalance(){
        return balance;
    }

    public void deposit(double amount){
        if(amount > 0){
            balance += amount;
        }
    }
}
```
