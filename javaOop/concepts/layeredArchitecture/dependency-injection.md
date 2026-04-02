# Dependency Injection (DI)

Without DI: → class decides its dependencies (rigid)

```
public class UserService {
    private Notification notification = new EmailNotification(); // ❌ fixed
}
```

With DI: → dependencies are provided from outside (flexible)

```
public class UserService {
    private Notification notification;

    public UserService(Notification notification) {
        this.notification = notification;
    }
}
```

Benefit:

- flexible
- testable
- loosely coupled
- follows SOLID

## Question practice

Question: Why is composition + interfaces + dependency injection the foundation of modern backend systems?

Break it into 3 clean roles

**1. Composition → flexibility**

> HAS-A relationship

- swap behaviors easily
- avoid wrong inheritance (Penguin problem)
- build systems from smaller pieces

**2. Interfaces → abstraction**

> Contract, not implementation

- define WHAT should happen
- allow multiple implementations
- enable polymorphism

**3. Dependency Injection → connection**

> Who provides the implementation

- inject behavior from outside
- reduce coupling
- make testing easy (mock implementations)

**Why this is the foundation of backend systems**

Because real systems need:

Change without breaking code

And this combination enables:

- add new features without modifying existing code (OCP)
- swap implementations easily (DIP)
- isolate responsibilities (SRP)
- avoid incorrect inheritance (LSP)

Refined answer:

Composition allows objects to gain behavior flexibly through “has-a” relationships instead of rigid inheritance. Interfaces define contracts that enable interchangeable implementations. Dependency injection connects these pieces by supplying implementations at runtime, reducing coupling. Together, they enable SOLID principles like OCP and DIP, making systems flexible, testable, and scalable.

## Key insight

```
Interfaces define the rules
Composition builds the structure
Dependency Injection wires everything together
```
