# Encapsulation

> Hide internal data and control how it changes

Bundling data and behavior together while protecting the internal state of the object.
Encapsulation allows the class to enforce rules.

The class controls how its internal state changes. External code cannot modify the internal state directly, so all updates must go through methods to enforce business rules.

In practice this means:

- fields are private
- access is controlled through methods

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
