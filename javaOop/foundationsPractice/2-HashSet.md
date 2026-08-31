# Session 2 - `HashSet`

In Session 1, a HashMap stored information associated with each value:

```
number → frequency
```

Today we'll look at a related structure: HashSet.

Suppose an application receives these user IDs:

```
List<Integer> userIds = List.of(
    12, 7, 19, 7, 25, 12
);
```

Our requirement is:

> Determine whether the input contains a duplicate user ID.

## 1. What information do we actually need?

We don't need to know how many times each ID occurs.

We only need to know:

```
Have I already seen this ID?
```

So we can maintain:

```
Set<Integer> seen = new HashSet<>();
```

The state means:

> `seen` contains every unique user ID in the processed prefix.

That's our invariant.

## 2. Build the algorithm

```
Set<Integer> seen = new HashSet<>();

for (Integer userId : userIds) {

    if (seen.contains(userId)) {
        System.out.println("Duplicate: " + userId);
    }

    seen.add(userId);
}
```

## 3. Why not use an ArrayList?

We technically could write:

```
List<Integer> seen = new ArrayList<>();

for (Integer userId : userIds) {
    if (seen.contains(userId)) {
        System.out.println("Duplicate: " + userId);
    }

    seen.add(userId);
}
```

It works.

But the data structure doesn't match the problem as naturally.

An `ArrayList` mainly represents:

```
an ordered sequence of elements
```

A `HashSet` represents:

```
a collection of unique values
```

And a `HashSet` normally provides average **O(1)** membership checks:

```
seen.contains(userId)
```

while searching an `ArrayList` is **O(n)**.

So for the question:

> Have I seen this value before?

a `HashSet` is often the natural first choice.

## 4. OOP connection: what counts as “the same”?

Things become more interesting when our set contains objects:

```
Set<Student> students = new HashSet<>();
```

Imagine:

```
Student a = new Student("Amy", 72);
Student b = new Student("Amy", 72);

students.add(a);
students.add(b);
```

Should the set contain one student or two students?

Java cannot answer that business question for us.

We need to define what makes two `Student` objects logically equal. For our domain, perhaps the student's unique ID determines identity:

```
Student(101, "Amy")
Student(101, "Amy")
```

could represent the same student.

This is where the OOP methods:

```
equals()
hashCode()
```

become important.

You don't need to implement them today. Just remember the connection:

```
HashSet<Student>
       ↓
needs to determine whether
two Student objects are equal
       ↓
equals() + hashCode()
```

This is one reason Java collections and OOP shouldn't be learned as completely separate subjects.

## Exercise

Now suppose we have:

```
List<String> emails = List.of(
    "amy@example.com",
    "ben@example.com",
    "cara@example.com",
    "amy@example.com"
);
```

```
public static boolean containsDuplicate(List<String> emails){
    Set<String> seen = new HashSet<>();

    for(String email:emails){
        if(seen.contains(email)){
            return true;
        }
        seen.add(email);
    }
    return false;
}
```

```
Data Structure:
HashSet

State:
`seen` contains the unique emails encountered so far.

Invariant:
After each completed iteration, `seen` contains all unique emails from the processed prefix.

Why HashSet instead of HashMap:
We only need to track membership — whether an email has already been encountered. We don't need to associate additional data with each email, so a HashSet expresses the requirement more directly than a HashMap.
```
