# Methods

A method defines behaviour of an object.

## Types of methods

1. Methods that return values

```
public double getBalance() {
    return balance;
}
```

2. Methods that perform actions

```
public void deposit(double amount) {
    balance += amount;
}
```

## Method structure

Example:

```
public double calculateArea(double width, double height){
    return width * height;
}
```

Breakdown:

```
public      -> access modifier
double      -> return type
calculateArea -> method name
(double width, double height) -> parameters
```

## Method overloading

```
public void deposit(double amount){
    balance += amount;
}

public void deposit(double amount, String note){
    balance += amount;
    System.out.println(note);
}
```

Java decides which one to call based on the arguments.

## Question

Why is this better design?

```
double area = rectangle.getArea();
System.out.println(area);
```

instead of

```
rectangle.printArea();
```

**Reason 1 — Separation of responsibilities**
The rectangle object should calculate geometry, not handle display logic.

**Reason 2 — Reusability**
If getArea() returns a value, we can use it anywhere:

```
double totalArea = rect1.getArea() + rect2.getArea();
```

**Reason 3 — Testing**
In real systems we write unit tests.

```
assertEquals(15, rectangle.getArea());
```
