# Encapsulation

Bundling data and behavior together while protecting the internal state of the object.
Encapsulation allows the class to enforce rules.

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
