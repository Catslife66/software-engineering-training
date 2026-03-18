# Inheritance

> Define common behavior once, reuse it

Inheritance allows classed to reuse and extend behaviour.
This models an **"is-a"** relationship.

Example:

```
// Parent class:
public class Vehicle {

    protected String brand;
    protected int speed;

    public Vehicle(String brand, int speed){
        this.brand = brand;
        this.speed = speed;
    }

    public void displayInfo(){
        System.out.println("Brand: " + brand + ", Speed: " + speed);
    }
}

// Child class:
public class Car extends Vehicle {

    public Car(String brand, int speed){
        super(brand, speed);
    }

    public void drive(){
        System.out.println("Car is driving");
    }
}

// Second child class:
public class Bike extends Vehicle {

    public Bike(String brand, int speed){
        super(brand, speed);
    }

    public void pedal(){
        System.out.println("Bike is pedaling");
    }
}
```

## Method Overriding

A child class provides its own version of a method from the parent class.

**@Override**
This tells Java: “I am overriding a parent method.”

Benefits:

- catches mistakes
- improves readability
- required in good practice
