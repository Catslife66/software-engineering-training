# Classes and Objects

## What is a class?

A class is a blueprint.

It defines:

- what data an object has
- what behavior an object can perform

Example:
A Car class might define:

- data: brand, speed
- behavior: start(), accelerate()

A class is not the real thing itself.
It is the template.

## What is an object?

An object is a real instance created from a class.

If Car is the blueprint, then:

- one object could be a red Toyota
- another object could be a blue BMW

They come from the same class, but hold different values.

**Example:**

```
public class Car {
    // fields(data)
    private String brand;
    private int speed;

    public Car(String brand, int speed) {
        this.brand = brand;
        this.speed = speed;
    }

    // methods(behaviour)
    public void start() {
        System.out.println(brand + " is starting.");
    }

    public void accelerate() {
        speed += 10;
        System.out.println(brand + " speed is now " + speed);
    }

    public int getSpeed() {
        return speed;
    }
}

// create an object

public class Main {
    public static void main(String[] args) {
        Car car1 = new Car();
        car1.brand = "Toyota";
        car1.speed = 0;

        Car car2 = new Car();
        car2.brand = "BMW";
        car2.speed = 20;

        car1.start();
        car1.accelerate();

        car2.start();
        car2.accelerate();
    }
}
```

the class defines structure
the object stores actual values

## Summary

A class is a blueprint that defines fields and methods. An object is a specific instance created from that class. Multiple objects created from the same class share the same structure, but each object has its own separate state in memory.
