# Constructors

## What is a constructor?

A constructor is a special method used when an object is created.

Its job is to initialise objects into a valid state.

A constructor is a special method used to initialize an object when it is created, while methods define behaviors that objects can perform after creation.

Key ideas:

- same name as class
- no return type
- can overload

## Why do constructors matter?

Without a constructor, you might do this:

```
Book book = new Book();
book.title = "Harry Potter";
book.author = "J.K. Rowling";
book.price = 12.99;
```

Constructors are used to initialise an object when it is created. They help ensure that required fields are set early and that the object starts in a valid state. This improves reliability and makes object creation cleaner.

## Constructor syntax

```
public class Book {
    private String title;
    private String author;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }
}
```

## Constructor overloading

```
public class Book {
    private String title;
    private String author;
    private double price;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.price = 0.0;
    }

    public Book(String title, String author, double price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }
}
```

It allows different ways to create an object.
