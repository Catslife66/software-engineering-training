# Session 3 - `List<Student>` + filtering objects

Suppose we have:

```
List<Student> students = List.of(
    new Student("Amy", 72),
    new Student("Ben", 48),
    new Student("Cara", 81),
    new Student("David", 55)
);
```

Requirement:

> Build a new list containing only students who passed.

Assume a pass mark of 50.

## 1. Choose the state

We need to preserve all students who meet a condition.

So our state is:

```
List<Student> passedStudents = new ArrayList<>();
```

Its meaning is:

`passedStudents` contains every passing student encountered so far.

That gives us the invariant:

```
After each iteration, passedStudents contains all students with marks of at least 50 from the processed prefix.
```

## 2. Algorithm

```
List<Student> passedStudents = new ArrayList<>();

for (Student student : students) {
    if (student.getMark() >= 50) {
        passedStudents.add(student);
    }
}
```

Trace:

```
Amy 72   → add
Ben 48   → skip
Cara 81  → add
David 55 → add
```

Final state:

```
[Amy, Cara, David]
```

Notice how this differs from our earlier minimum problem.

Previously:

```
Need one best object
→ maintain one Student reference
```

Today:

```
Need many qualifying objects
→ maintain a collection
```

That is an important algorithm-design distinction.

## 3. OOP connection

We are not copying the students themselves.

When we write:

```
passedStudents.add(student);
```

the new list stores another reference to the same `Student` object.

Conceptually:

```
students                  passedStudents

Amy  -----------------------> Amy
Ben
Cara -----------------------> Cara
David ----------------------> David
```

So if Student objects are mutable, changing one object through one reference can be visible through the other collection too.

## 4. Engineer language

Instead of:

“I loop through and add the students who passed.”

A stronger explanation is:

> “I iterate through the students and maintain a result list containing the qualifying objects. For each student, I evaluate the pass condition and add that student's reference to the result list when the condition is satisfied.”

And the invariant version is even stronger:

> “After processing each student, the result list contains every passing student from the processed prefix.”

## Exercise

Given:

```
List<Product> products = List.of(
    new Product("Keyboard", 45.0),
    new Product("Monitor", 180.0),
    new Product("Mouse", 25.0),
    new Product("Chair", 220.0)
);
```

Requirement:

> Build a list containing every product costing more than 100.

```
List<Product> expensiveProducts = new ArrayList<>();

for(Product product : products){
    if(product.getPrice() > 100){
        expensiveProducts.add(product);
    }
}

Data structure:
ArrayList

State:
expensiveProducts contains all qualifying products encountered so far.

Invariant:
After examing each product, expensiveProducts contains all products that cost more than 100 in the processed prefix.

Termination:
must process the entire input
```
