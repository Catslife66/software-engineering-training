# OOP + System Design Interview Training

### Question 1:

> What is the difference between inheritance and composition?
>
> When would you use each?

**Answer:**

Inheritance represents an “is-a” relationship, where a subclass extends a parent class and inherits its properties and behavior. It is useful when there is a clear hierarchical relationship and shared identity.

Composition represents a “has-a” relationship, where a class contains other objects to achieve behavior. It allows more flexibility because behavior can be changed at runtime by swapping components.

I would use inheritance when the relationship is stable and truly hierarchical, such as a Manager being an Employee. I would use composition when behavior may vary or change, such as an OrderService using a Notification interface to support different notification types.

In general, composition is preferred over inheritance because it leads to more flexible and maintainable designs.

### Question 2:

> What is polymorphism in Java?
>
> Can you give a real-world example and explain why it is useful?

**Answer:**

Polymorphism in Java allows the same method call to behave differently depending on the object type. It is typically achieved through method overriding and interfaces.

For example, an OrderService may depend on a Notification interface. Different implementations like EmailNotification or SMSNotification can provide their own version of the send() method. When the service calls notification.send(), the actual behavior is determined at runtime based on the object passed in.

This is useful because it makes the system flexible and extensible. New implementations can be added without changing existing code, which supports the Open/Closed Principle.

### Question 3:

> What is the difference between abstraction and encapsulation?
>
> Can you give examples of each?

**Answer:**

Abstraction focuses on hiding implementation details and exposing only what is necessary. It defines what a class should do without specifying how it does it, typically using interfaces or abstract classes.

Encapsulation focuses on protecting an object’s internal state by restricting direct access to its data and controlling it through methods like getters and setters.

For example, an abstract Animal class may define a method makeSound() without implementation, and subclasses like Dog provide their own implementation — this is abstraction.

In contrast, an Account class may have a private balance field and only allow updates through methods like deposit() or withdraw(), ensuring the state remains valid — this is encapsulation.

### Question 4:

> What is encapsulation and why is it important in real systems?
>
> Can you give a real-world example (not just definition)?

**Answer:**

Encapsulation is the concept of protecting an object’s internal state by restricting direct access to its data and controlling it through methods.

It is important in real systems because it ensures _data integrity_ and prevents invalid or unsafe modifications.

### Question 5:

> Why is it dangerous to make fields public in a class?

**Answer:**

Making fields public is dangerous because it breaks encapsulation and data integrity. External code can modify the object’s internal state directly without enforcing business rules, which can lead to invalid or inconsistent data.

### Qustion 6 - spring boot design:

> Imagine this endpoint:
>
> POST /users/{userId}/wishlist/{propertyId}
>
> Describe the flow step-by-step:
>
> What does the Controller do?
>
> What does the Service do?
>
> What does the Repository do?
>
> What gets returned?

1. Controller

The controller receives the HTTP request, extracts parameters like `userId` and `propertyId`, and delegates the request to the service layer. It does not contain business logic.

2. Service

The service handles the business logic. It validates that the user and property exist, checks for duplicate wishlist entries, and if all conditions are met, creates a new wishlist item and calls the repository to persist it.

3. Repository

The repository is responsible for data access. It communicates with the database to save and retrieve wishlist data.

4. Response

The service returns a structured result, and the controller converts it into an HTTP response. If successful, it returns a success response; otherwise, it returns an appropriate error response.
