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

## Static vs Instance Members

Instance members belong to each object separately.
Static members belong to the class itself, not individual objects.
Example:

```
public class BankAccount {

    private String accountHolder;
    private double balance;

    private static int totalAccounts = 0;

    public BankAccount(String accountHolder){
        this.accountHolder = accountHolder;
        totalAccounts++;
    }

    public static int getTotalAccounts(){
        return totalAccounts;
    }
}

BankAccount a = new BankAccount("Alice");
BankAccount b = new BankAccount("David");

System.out.println(BankAccount.getTotalAccounts());

```

We access it through the class name, not an object.

## Static Methods

Static methods belong to the class.

Example:

```
public class MathUtils {

    public static int add(int a, int b){
        return a + b;
    }
}


int result = MathUtils.add(3,5);

```

**Instance**
belongs to object
each object has its own copy
accessed via object

**Static**
belongs to class
shared across all objects
accessed via class
