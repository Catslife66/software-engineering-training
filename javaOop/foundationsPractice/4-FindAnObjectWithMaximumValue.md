# Session 4 - Finding an object with the maximum value

You've already practised three useful collection patterns:

```
minimum candidate → keep one object
frequency counting → HashMap
duplicate detection → HashSet
filtering → ArrayList
```

Today we'll revisit the **single-candidate** pattern, but add an important OOP question: what should a method return when the collection might be empty?

Suppose:

```
class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
```

We need:

> Find the most expensive product.

## 1. Identify the state

Unlike Session 3, we don't need to collect many products.

We need exactly one candidate:

```
Product mostExpensive = products.get(0);
```

Its meaning is:

`mostExpensive` references the most expensive product encountered so far.

Our invariant is:

After processing each product, `mostExpensive` references the product with the highest price in the processed prefix.

## 2. Update the candidate

```
for (Product product : products) {
    if (product.getPrice() > mostExpensive.getPrice()) {
        mostExpensive = product;
    }
}
```

Suppose the prices are:

```
Keyboard  £45
Monitor   £180
Mouse     £25
Chair     £220
```

The state changes like this:

```
start     → Keyboard £45
Monitor   → Monitor £180
Mouse     → Monitor £180
Chair     → Chair £220
```

Notice something important: state doesn't have to change during every iteration.

It changes only when the current element provides better information.

## 3. Empty collections and Optional

We saw previously that this is unsafe for an empty list:

```
Product mostExpensive = products.get(0);
```

because index 0 doesn't exist.

One Java design is:

```
public static Optional<Product> findMostExpensive(List<Product> products) {
    if (products.isEmpty()) {
        return Optional.empty();
    }

    Product mostExpensive = products.get(0);

    for (Product product : products) {
        if (product.getPrice() > mostExpensive.getPrice()) {
            mostExpensive = product;
        }
    }

    return Optional.of(mostExpensive);
}
```

Why `Optional<Product>` rather than Product?

Because there are legitimately two possible outcomes:

```
products exist     → a Product
products don't     → no result
```

The method signature communicates this possibility:

```
Optional<Product>
```

That's an OOP/API-design lesson: **a method's return type should communicate its contract to callers**.

## Exercise

Now work with employees:

```
class Employee {
    private String name;
    private int yearsExperience;

    public Employee(String name, int yearsExperience) {
        this.name = name;
        this.yearsExperience = yearsExperience;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }
}
```

Given:

```
List<Employee> employees = List.of(
    new Employee("Amy", 3),
    new Employee("Ben", 7),
    new Employee("Cara", 5),
    new Employee("David", 9)
);
```

Complete a method that finds the most experienced employee:

```
public static Optional<Employee> findMostExperienced(List<Employee> employees){
    if(employees.isEmpty()){
        return Optional.empty();
    }
    Employee mostExperienced = employees.get(0);

    for(Employee employee : employees){
        if(employee.getYearsExperience() > mostExperienced.getYearsExperience()){
            mostExperienced = employee;
        }
    }
    return Optional.of(mostExperienced);
}

State: mostExperienced stores the reference to the most experienced employee so far

Invariant:
After processing each employee, mostExperienced stores the reference to the most experienced employee in the processed prefix

Why one Employee reference instead of an ArrayList:
The requirement asks for a single most experienced employee, so we only need to maintain one candidate reference. An ArrayList would store multiple candidates unnecessarily.

Why Optional<Employee>:
Optional<Employee> represents that the method may or may not produce an employee. An empty input has no most experienced employee, so the method returns Optional.empty() rather than inventing a value or returning null.

```

Now we have:

```
Amy   → 3 years
Ben   → 9 years
Cara  → 5 years
David → 9 years
```

Our current algorithm returns Ben.

But David has the same maximum experience.

So imagine the business requirement changes to:

> Return all employees tied for the greatest number of yea

```
State:
mostExperienceYears stores the maximum years of experience encountered so far.
mostExperiencedEmployees contains all employees in the processed prefix whose experience equals that maximum.

Invariant:
After each iteration, mostExperiencedEmployees contains all employees tied for the maximum experience in the processed prefix.

Update rules:
employee years > maxYears:
    mostExperienceYears = employee.getYearsExperience();
    mostExperiencedEmployees.clear();
    mostExperiencedEmployees.add(employee);

employee years == maxYears:
    mostExperiencedEmployees.add(employee);

employee years < maxYears:
    skip
```
