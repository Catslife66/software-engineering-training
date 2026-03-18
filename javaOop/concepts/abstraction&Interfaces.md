# Abstraction & Interfaces

Abstraction means:

> Hide how something works, and only expose what it does.

## Abstraction in Java

- Abstract classes
- Interfaces

## Abstract Class

An abstract class is a class that:

- cannot be instantiated
- can have incomplete methods

Example:

```
public abstract class Animal {

    public abstract void makeSound();

    public void sleep(){
        System.out.println("Sleeping...");
    }
}

Animal a = new Animal(); // ❌ not allowed
```

Because it’s abstract.

### Child classes must implement abstract methods

```
public class Dog extends Animal {

    @Override
    public void makeSound(){
        System.out.println("Bark");
    }
}
```

### Why abstraction is powerful

It lets you define **WHAT must be done**, without defining **HOW it is done**

## Interfaces

An interface is like a contract.

> It defines "What a class MUST do".

Example:

```
public interface Payment {

    void pay(double amount);
}

public class CreditCardPayment implements Payment {

    @Override
    public void pay(double amount){
        System.out.println("Paid by credit card: " + amount);
    }
}

public class PayPalPayment implements Payment {

    @Override
    public void pay(double amount){
        System.out.println("Paid via PayPal: " + amount);
    }
}
```

### Why interfaces matter

Now you can write:

```
public void processPayment(Payment payment){
    payment.pay(100);
}

processPayment(new CreditCardPayment());
processPayment(new PayPalPayment());
```

This is the foundation of:

- clean architecture
- dependency injection
- Spring Boot
- scalable systems

## Abstract class vs Interface

| Abstract Class               | Interface                    |
| ---------------------------- | ---------------------------- |
| can have fields              | no instance fields (usually) |
| can have implemented methods | mostly method declarations   |
| "is-a" relationship          | "can-do" capability          |

### Mental model

Interface = contract
Class = implementation

### Example together

Animal → abstract class
Dog → concrete class

Payment → interface
CreditCard → implementation
