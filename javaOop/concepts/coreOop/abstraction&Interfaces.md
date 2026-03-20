# Abstraction & Interfaces

Abstraction means:

> Hide how something works, and only expose what it does. (Define WHAT must be done, not HOW)

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
> It allows one method to handle many implementations

Example:

```
// Step 1 — Interface (contract)
public interface Payment {
    void pay(double amount);
}

// Step 2 — Implementations (HOW)
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

// Step 3 — Service
public class PaymentService {
    public void processPayment(Payment payment){
        payment.pay(100);
    }
}

// Step 4 — Usage
public class Main {
    public static void main(String[] args){

        PaymentService service = new PaymentService();

        service.processPayment(new CreditCardPayment());
        service.processPayment(new PayPalPayment());
    }
}

```

👉🏻 Why is this better?

```
processPayment(Payment payment)

// instead of
// processCreditCard(...)
// processPayPal(...)
```

It’s better because processPayment(Payment payment) works with any implementation of Payment, so new payment type can be added without changing the existing method. This makes the system more flexible, easier to extend, and easier to maintain.

### Why interfaces matter

This is the foundation of:

- clean architecture
- dependency injection
- Spring Boot
- scalable systems

👉🏻 **Service** = controls workflow

👉🏻 **Interface** = defines capability

👉🏻 **Class** = provides implementation

Visual model

```
Main / Controller
 ↓
Service (PaymentService - business logic)
 ↓
Interface (Payment - abstraction)
 ↓
Implementation (CreditCardPayment / PayPalPayment - actual behaviour)
```

### This is real software engineering

This pattern is used everywhere:

In Spring Boot:

```
Controller → Service → Interface → Implementation
```

In Django (conceptually):

```
View → Service logic → Model / backend
```

### Loose coupling

Add new feature WITHOUT changing existing code

Your code does NOT depend on concrete classes.

## Abstract class vs Interface

| Abstract Class               | Interface                    |
| ---------------------------- | ---------------------------- |
| can have fields              | no instance fields (usually) |
| can have implemented methods | mostly method declarations   |
| "is-a" relationship          | "can-do" capability          |

### Mental model

Abstract class = shared structure (shared data + shared logic)
Interface = shared behaviour contract only

👉🏻 Use an **abstract class** when multiple classes share common state or implementation.

shared state(fields) OR shared implementation(methods)

👉🏻 Use an **interface** when you only need to define a contract for behavior without sharing data.

a contract (no shared data)

### Example together

Animal → abstract class
Dog → concrete class

Payment → interface
CreditCard → implementation
