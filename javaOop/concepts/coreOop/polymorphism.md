# Polymorphism

> One interface, many implementations (Same call, different behavior)

> Polymorphism allows the same method call to behave differently depending on the object type. It enables flexibility and supports OCP.

## Method Overriding

A child class provides its own version of a method from the parent class.

**@Override**
This tells Java: “I am overriding a parent method.”

Benefits:

- catches mistakes
- improves readability
- required in good practice

## Runtime polymorphism

Parent defines default behavior, child can replace it.

**Q: Will it call and why?**

A) Employee version

B) Manager version

```
Employee e = new Manager("Alice", 60000);
e.displayInfo();
```

Key rule:

> Method calls are decided at runtime based on the OBJECT type, not the reference type.

## Important Limitation

Polymorphism works for **methods**, but not for **fields**.

Example 1:

```
// Full codes in 'exercises/person' folder

Employee e = new Manager("Lily", 35000);
e.displayInfo(); // ✅
((Manager) e).manageTeam(); // ✅
e.manageTeam(); // ❌ compile error
```

Reference variable: Employee e

It stores a reference (pointer) to an object.

> The variable type = Employee

Actual object: new Manager(...)

> Object type = Maneger

Visual model:

```
e (type: Employee)
   ↓
Manager object in memory
```

👉🏻 **Why displayInfo() works (polymorphism)**

Java checks:

- Step 1 — Compile time

  Does Employee have displayInfo()?

  ✔ Yes → allowed

- Step 2 — Runtime

  Which version should run?

  👉 Look at the actual object (Manager)

  ✔ Run Manager’s version

👉🏻 **Why ((Manager) e).manageTeam() works**

Java checks:

- Step 1 - Compile time

  You cast to Manager

  👉 Does Manager have manageTeam()?

  ✅

- Step 2 - Runtime

  Which version should run?

  👉 Look at the actual object (Manager)

  ✔ Run Manager’s version

👉🏻 **Why e.manageTeam() does NOT work**

Java checks:

- Step 1 — Compile time

  Does Employee have manageTeam()?

  ❌ No

  So Java stops you immediately.

Example 2:

```
Employee e = new Developer("Alice", 30000);
((Manager) e).manageTeam(); // ❌ runtime error with: ClassCastException
```

👉🏻 **Why casting can crash here**

Java checks:

- Runtime

  Object is Developer?

  You are forcing it to be Manager

  So Java throws ClassCastException.

**KEY RULE:**

- STEP 1- Compile-time (safety check)

  > What methods are allowed? → Based on REFERENCE TYPE

- STEP 2- Runtime(actual execution)

  > Which method version runs? → Based on OBJECT TYPE
