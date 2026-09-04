# Session 5 - Grouping objects with `HashMap<K, List<V>>`

You've recently worked with:

```
HashMap<K, Integer>  → one count per key
HashSet<T>           → unique values seen
ArrayList<T>         → multiple result objects
Employee reference   → one best candidate
```

Today we'll combine OOP and collections.

Suppose:

```
class Employee {
    private String name;
    private String department;

    public Employee(String name, String department) {
        this.name = name;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }
}
```

And:

```
List<Employee> employees = List.of(
    new Employee("Amy", "Engineering"),
    new Employee("Ben", "Sales"),
    new Employee("Cara", "Engineering"),
    new Employee("David", "Sales"),
    new Employee("Ella", "HR")
);
```

The business requirement is:

> Group employees by department.

The desired result has this shape:

```
Engineering → [Amy, Cara]
Sales       → [Ben, David]
HR          → [Ella]
```

## 1. Start from the required information

Ask:

> What information must my state preserve?

We need:

```
department → employees belonging to that department
```

So neither of these is sufficient:

```
List<Employee>
Set<Employee>
```

We need a mapping:

```
Map<String, List<Employee>>
```

The key represents a department, while the value represents the employees belonging to it.

That's a useful extension of Session 1:

```
Frequency counting:
number → count
HashMap<Integer, Integer>

Grouping:
department → collection of employees
HashMap<String, List<Employee>>
```

The value of a map doesn't have to be a simple number or string. It can itself be a collection.

## 2. State and invariant

Our state could be:

```
Map<String, List<Employee>> employeesByDepartment =
        new HashMap<>();
```

Its meaning is:

`employeesByDepartment` stores the employees encountered so far, grouped by their department.

A good invariant is:

After processing each employee, every employee in the processed prefix appears in the list associated with their department.

Now the algorithm has to preserve that invariant.

## 3. Two cases during each iteration

Suppose the current employee is Amy:

```
Amy → Engineering
```

Initially:

```
{}
```

There isn't an Engineering group yet, so we first need:

```
Engineering → []
```

Then add Amy:

```
Engineering → [Amy]
```

Later we encounter Cara:

```
Cara → Engineering
```

The group already exists:

```
Engineering → [Amy]
```

so we add Cara:

```
Engineering → [Amy, Cara]
```

Conceptually:

```
current employee
      ↓
get department
      ↓
does this department have a list?
      ↓
no  → create list
yes → use existing list
      ↓
add employee
```

Java provides a useful Map method for exactly this situation:

```
putIfAbsent()
```

For example:

```
for (Employee employee : employees) {
    String department = employee.getDepartment();

    employeesByDepartment.putIfAbsent(
        department,
        new ArrayList<>()
    );

    employeesByDepartment.get(department).add(employee);
}
```

Read those operations as:

```
Ensure this department has a collection.

Then:

Retrieve that collection.
Add the current employee.
```

This is a common real-world pattern. You might group:

```
Order    → customer
Product  → category
Student  → course
Ticket   → status
Employee → department
```

## Exercise

Now suppose we have:

```
class Product {
    private String name;
    private String category;

    public Product(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
```

Given:

```
List<Product> products = List.of(
    new Product("Keyboard", "Electronics"),
    new Product("Desk", "Furniture"),
    new Product("Mouse", "Electronics"),
    new Product("Chair", "Furniture"),
    new Product("Lamp", "Lighting")
);
```

Requirement:

Group all products by category.

```
public static Map<String, List<Product>> groupByCategory(
        List<Product> products) {

    Map<String, List<Product>> productsByCategory = new HashMap<>();

    for (Product product : products) {
        String category = product.getCategory();

        productsByCategory.putIfAbsent(
            category,
            new ArrayList<>()
        );

        productsByCategory.get(category).add(product);
    }

    return productsByCategory;
}

Data structure:
HashMap

State:
productsByCategory stores the products encountered so far, grouped by their categories.

Invariant:
After processing each product, productsByCategory contains every product from the processed prefix in the list associated with its category.

Update rules:
category not encountered before → create an entry for the category with an empty list.
category already encountered    → add this product to the existing category list
```
