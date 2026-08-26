# Session 0 - `ArrayList` + objects

Imagine we're building a small university system. We have a `Student` class:

```
public class Student{
    private String name;
    private int mark;

    public Student(String name, int mark){
        this.name = name;
        this.mark = mark;
    }

    public String getName(){
        return name;
    }
    public int getMark(){
        return mark;
    }
}
```

And we store students in an ArrayList:

```
ArrayList<Student> students = new ArrayList<>();

students.add(new Student("Amy", 72));
students.add(new Student("Ben", 48));
students.add(new Student("Cara", 81));
students.add(new Student("David", 63));
```

## 1. Mental model

An `ArrayList<Student>` means:

> A resizable collection whose elements are references to Student objects.

Conceptually:

```
students
   |
   v
+-------+-------+-------+
| ref   | ref   | ref   |
+---|---+---|---+---|---+
    |       |       |
    v       v       v
   Amy     Ben     Cara
   72      48       81
```

This combines two important Java foundations:

**OOP**: each Student object owns its own state.

**Data structures**: the `ArrayList` organises multiple students so our program can process them.

## 2. Reading Java like an engineer

Suppose we want to find the student with the highest mark:

```
Student bestStudent = students.get(0);

for (Student student : students) {
    if (student.getMark() > bestStudent.getMark()) {
        bestStudent = student;
    }
}
```

Don't just read this as Java syntax.

Think about the state being maintained:

```
bestStudent
```

Invariant:

`bestStudent` represents the highest-scoring student we have seen so far.

## 3. Engineer language

Instead of saying:

> "The loop checks everyone and gets the biggest."

A stronger explanation would be:

> "We iterate through the students while maintaining a reference to the highest-scoring student seen so far. Whenever the current student's mark exceeds the stored student's mark, we update that reference."

## Exercise

Find the lowest-scoring student.

```
Student lowestStudent = students.get(0);

for(Student student : students){
    if(student.getMark() < lowestStudent.getMark()){
        lowestStudent = student;
    }
}
```

Questions:

```
1. What should lowestStudent represent throughout the loop?
lowestStudent represents the student with the lowest mark in the processed prefix.

2. Why would initialising it with new Student("", 0) be a bad idea?
We aren't actually looking for the lowest mark; we're looking for the Student who has the lowest mark. Creating a fake Student merely to initialise the algorithm introduces an object that doesn't represent anything in the domain.

3. In engineer language, explain how your algorithm finds the lowest-scoring student.
We initialise lowestStudent with the first student in the collection. We then iterate through the students, comparing each student's mark with the current lowest-scoring student's mark. If the current student's mark is lower, we update lowestStudent to reference that student. Therefore, after processing the entire collection, lowestStudent references the student with the minimum mark.

4. One important edge case, what happens if students.isEmpty()?
The algorithm assumes that the collection contains at least one student. If the list is empty, get(0) attempts to access an invalid index and throws an IndexOutOfBoundsException.
```

## The engineering lesson

This is more important than remembering the exception name.

Our algorithm has a **precondition**:

> The input collection must contain at least one student.

So before writing:

```
Student lowestStudent = students.get(0);
```

an engineer should ask:

```
What assumption am I making?

        ↓

students is non-empty

        ↓

What happens if that assumption is violated?

        ↓

get(0) is invalid

        ↓

How should my application represent
"there is no lowest student"?
```

And that final question is actually a design decision. Depending on the application, we might return null, return Optional<Student>, or reject an empty input with an exception.
