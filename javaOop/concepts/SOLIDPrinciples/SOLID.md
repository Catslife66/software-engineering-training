# SOLID Principles

SOLID is a group of design principles that help you write code that is:

- easier to extend
- easier to test
- easier to maintain
- less fragile when requirements change

The 5 principles are:

- Single Responsibility Principle
- Open/Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

## S - Single Responsibility Principle(SRP)

> A class should have **one reason to change**.

That means:

- one clear responsibility
- not many unrelated jobs mixed together

Example:

```
// Bad Design
public class OrderService {

    public void createOrder() {
        System.out.println("Creating order...");
    }

    public void saveToDatabase() {
        System.out.println("Saving order to database...");
    }

    public void sendConfirmationEmail() {
        System.out.println("Sending confirmation email...");
    }
}

// Better Design
public class OrderService {
    public void createOrder() {
        System.out.println("Creating order...");
    }
}
public class OrderRepository {
    public void save() {
        System.out.println("Saving order to database...");
    }
}
public class EmailService {
    public void sendConfirmationEmail() {
        System.out.println("Sending confirmation email...");
    }
}
```

Now each class has one job.

### Why SRP matters

Because when code mixes responsibilities:

- classes become harder to read
- changes become riskier
- testing becomes harder
- unrelated parts break more easily

## O — Open/Closed Principle (OCP)

> Software should be **open for extension** but **closed for modification**

Example:

```
// Bad design
// Every time you add a new payment type: You must change this class

public class PaymentService {

    public void processPayment(String type) {
        if (type.equals("card")) {
            System.out.println("Processing card payment");
        } else if (type.equals("paypal")) {
            System.out.println("Processing PayPal payment");
        }
    }
}

// Better design
public interface Payment {
    void pay(double amount);
}

public class CardPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Card payment: " + amount);
    }
}
public class PayPalPayment implements Payment {
    public void pay(double amount) {
        System.out.println("PayPal payment: " + amount);
    }
}
public class PaymentService {

    public void processPayment(Payment payment, double amount) {
        payment.pay(amount);
    }
}
```

**OCP = polymorphism + abstraction**

## L — Liskov Substitution Principle (LSP)

> A subclass should be usable wherever its parent class is expected, without breaking correct behavior.

Example:

```
// Bad design
// A penguin cannot fly So the subtype relationship is wrong.

class Bird {
    void fly() {
        System.out.println("Flying");
    }
}
class Penguin extends Bird {
}
```

Ask: Can this child truly behave like the parent in every expected use case?

If the answer is no, inheritance may be wrong.

## I — Interface Segregation Principle

> A class should not be forced to implement methods it does not need.

Example:

```
// Bad design
// It forces BasicPrinter class to implement methods it doesn’t need
public interface Machine {
    void print();
    void scan();
    void fax();
}
public class BasicPrinter implements Machine {
    @Override
    public void print() {
        System.out.println("Printing...");
    }

    @Override
    public void scan() {
        throw new UnsupportedOperationException("Scan not supported");
    }

    @Override
    public void fax() {
        throw new UnsupportedOperationException("Fax not supported");
    }
}

// Better design
public interface Printable {
    void print();
}
public interface Scannable {
    void scan();
}
public interface Faxable {
    void fax();
}

public class BasicPrinter implements Printable {
    @Override
    public void print() {
        System.out.println("Printing...");
    }
}
public class MultiFunctionPrinter implements Printable, Scannable, Faxable {
    @Override
    public void print() {
        System.out.println("Printing...");
    }

    @Override
    public void scan() {
        System.out.println("Scanning...");
    }

    @Override
    public void fax() {
        System.out.println("Faxing...");
    }
}
```

That gives:

- cleaner design
- less fake implementation
- fewer exceptions
- clearer interfaces

## D — Dependency Inversion Principle(DIP)

> High-level modules should not depend on low-level modules.
>
> Both should depend on abstractions.

Example:

```
// Bad design
// NotificationService depends directly on EmailSender
public class NotificationService {

    private EmailSender emailSender = new EmailSender();

    public void notifyUser(String message) {
        emailSender.send(message);
    }
}

// Better design
public interface NotificationSender {
    void send(String message);
}

public class EmailSender implements NotificationSender {
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}

public class SMSSender implements NotificationSender {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

public class NotificationService {

    private NotificationSender sender;

    public NotificationService(NotificationSender sender) {
        this.sender = sender;
    }

    public void notifyUser(String message) {
        sender.send(message);
    }
}
```

Bad design problems:

- cannot easily switch to SMS
- hard to test
- tightly coupled

## Full Picture

| Principle | Meaning                       |
| --------- | ----------------------------- |
| S         | one responsibility            |
| O         | extend without modifying      |
| L         | child must behave like parent |
| I         | small focused interfaces      |
| D         | depend on abstractions        |

OOP = tools

SOLID = rules for using the tools properly

| OOP Concept   | SOLID Principle that guides it |
| ------------- | ------------------------------ |
| Inheritance   | LSP (use it correctly)         |
| Abstraction   | OCP + DIP                      |
| Polymorphism  | OCP                            |
| Interfaces    | ISP                            |
| Encapsulation | SRP                            |
