# Foundation

**Purpose**

Before learning algorithms, learn the language engineers use to think.

**Goal**

By the end of this module, you should be able to look at any simple problem and answer four questions:

1. What information matters?
2. How will I store it?
3. What must always remain true?
4. When should it change?

## Problem vs Algorithm

Before solving a problem, understand what the final answer looks like.

Most beginners immediately think:

```
Problem
↓
Algorithm
↓
Code
```

Engineers don't.

They separate the **problem** from the **solution**.

Example

Problem:

```
Find the largest number in an array.
```

Before thinking about code, ask:

```
What is the final answer supposed to be?
```

Answer:

```
The largest number in the array.
```

That's it.

### Our First Engineering Rule

Whenever you see a new problem, ask:

"If someone handed me the correct answer, what would it look like?"

Examples:

Maximum:

```
One number
```

Sum:

```
One number
```

Longest word:

```
One word
```

Merge intervals:

```
A list of intervals
```

Notice how we're thinking about the **shape of the answer**, not the code.

## Information - "The fact we eventually need to know."

Information is the fact required to solve the problem.

Examples:

| Problem         | Information                |
| --------------- | -------------------------- |
| Maximum         | Largest value              |
| Count positives | Number of positive values  |
| Duplicate       | Whether a duplicate exists |

> "What facts matter?"

Example: Move Zeroes

The important information:

Non-zero values:

```
1
3
12
```

Their order:

```
1 before 3 before 12
```

Everything else (the exact positions of the zeros) is less important.

Example: Insert Interval

The important information:

```
The final merged interval.
```

Information comes from the problem itself.

## State

State is how the program currently represents the information.

Now we have a problem.

Suppose the information is:

```
Largest value = 97
```

Where does that information live?

It has to be stored somewhere.

That "somewhere" is what we call state.

Example:

Information is:

```
"The merged interval is [3,10]"
```

State is:

```
new_start = 3
new_end = 10
```

The variables are the program's representation of that information.

Suppose you're keeping score in a football match.

The information is:

```
Liverpool 2
Arsenal 1
```

How could you represent it?

```
home = 2
away = 1
```

or

```
score = (2,1)
```

or

```
{
    "Liverpool":2,
    "Arsenal":1
}
```

Different states.

Same information.

## Invariant

An invariant is a promise about the state that is true every time we reach the same point in the algorithm.

A:

Problem:

```
Find the minimum value.
```

State:

```
minimum
```

Invariant:

```
minimum always stores the minimum value examined so far
```

B:

Problem:

```
Count even numbers.
```

State:

```
count
```

Invariant:

```
count always stores the number of even numbers examined so far
```

C:

Problem:

```
Return whether a duplicate has been found.
```

State:

```
exists
```

Invariant:

```
exists is True exactly when we have found a duplicate among the elements examined so far.
```

## Transitions

A transition is the condition under which the state changes so that the invariant remains true.

> When should the state change?

Example:

**Maximum**

Invariant:

```
largest = largest value examined so far
```

When does it change?

Only if:

```
current > largest
```

That's the transition condition.

**Positive Count**

Invariant:

```
count = number of positive values examined so far
```

When does it change?

Only if:

```
current is positive
```

**Duplicate Exists**

Invariant:

```
exists = whether a duplicate has been found so far
```

When does it change?

Only when:

```
the current value has already been seen
```

## The entire engineering process

```
Problem

↓

Information
(What fact matters?)

↓

State
(Where do I remember that fact?)

↓

Invariant
(What promise about that state is always true?)

↓

Transition
(When must the state change to keep the promise true?)

↓

Code
```
