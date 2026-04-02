# Composition vs Inheritance

```
**HAS-A** relationship
Object contains behavior instead of inheriting it
```

| Inheritance     | Composition         |
| --------------- | ------------------- |
| fixed behavior  | flexible behavior   |
| rigid hierarchy | dynamic combination |
| can be wrong    | more accurate       |

## Composition

> instead of making an object be another thing, we make it have another thing

**Composition means a class gets behavior by containing another object, instead of inheriting that behavior from a parent class.**

With composition:

```
You can CHANGE behavior at runtime
```

Example 1:

```
public interface Movement {
    void move();
}

public class EngineMovement implements Movement {
    @Override
    public void move(){
        System.out.println("Moving with engine power.");
    }
}

public class PedalMovement implements Movement {
    @Override
    public void move(){
        System.out.println("Moving by pedaling.");
    }
}

public class Vehicle {
    private Movement movement;

    public Vehicle(Movement movement){
        this.movement = movement
    }

    public void move(){
        movement.move()
    }
}

public class Main {
    public static void main(String[] args) {
        Vehicle car = new Vehicle(new EngineMovement())
        Vehicle bike = new Vehicle(new PedalMovement())

        car.move();
        bike.move();
    }
}
```

Example 2:

```
// step 1 - payment behaviour contract
public interface PaymentStrategy{
    void pay(double amount);
}

// step 2 - different implements
public class CardPayment implements PaymentStrategy{
    @Override
    public void pay(double amount){
        System.out.println("Paid £" + amount + " by card.");
    }
}

public class PalpayPayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paid £" + amount + " by PayPal.");
    }
}

public class StripePayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("Paid £" + amount + " by Stripe.");
    }
}

// step 3 - order uses composition
public class Order {
    private double total;
    private PaymentStrategy paymentStrategy;

    public Order(double total, PaymentStrategy paymentStrategy){
        this.total = total;
        this.paymentStrategy = paymentStrategy;
    }

    public void checkout(){
        paymentStrategy.pay(total);
    }
}

// step 4 - use it

public class Main{
    public static void main(String[] args) {
        Order order1 = new Order(100, new CardPayment());
        Order order2 = new Order(50, new PayPalPayment());

        order1.checkout();
        order2.checkout();
    }
}

```

## Rule for naming

Name the abstraction by the common idea shared by all implementations.

Examples:

```
Movement → shared by engine movement and pedal movement
PaymentStrategy → shared by card, PayPal, crypto
NotificationSender → shared by email, SMS, push
```

Then name the method by the shared action:

```
move()
pay()
send()
```

This makes OOP much clearer.

Ask:

```
Am I modeling a real-world thing?
→ use simple name (Payment)

Am I modeling a design pattern / behavior role?
→ use explicit name (PaymentStrategy)
```
