# State is Information Flow

Imagine a factory.

Boxes arrive on a conveyor belt.

Some boxes are:

- valuable
- defective
- unfinished
- already packed

Your job is not to move boxes.

Your job is to ensure the valuable information ends up in the correct warehouse.

Now replace boxes with array elements, suddenly you've got a pointer problem.

## Information Flow

Almost every state-based algorithm can be described like this.

```
          Unknown
             │
             ▼
        (examined)
             │
     ┌───────┴────────┐
     ▼                ▼
 Valuable         Not valuable
     │                │
     ▼                ▼
 Builder         Disposable
     │
     ▼
 Finished
```

This is information flow.

Notice something:

There are no pointers.

There are no loops.

Only information.

## Information Questions

Move Zeroes asks:

```
Is this value valuable?
```

Insert Interval asks:

```
Does this interval change the builder?
```

Merge Sorted Array asks:

```
Which remaining value should become finished next?
```

Notice those are all information questions, not pointer questions.

## Builder

> "Which state is still constructing new information?"

A Builder is a piece of state that is gradually constructing new information.

1️⃣ Move Zeroes -> no builder

Move Zeroes goes directly:

```
Unknown
↓
Finished
```

or

```
Unknown
↓
Disposable
```

No intermediate construction.

2️⃣ Insert Interval -> newInterval

```
[4,8]
↓
[3,8]
↓
[3,10]
```

It keeps growing. It gradually absorbs information.

3️⃣ Merge Sorted Array -> the completed suffix of nums1

Each iteration extends it by exactly one element.

This is subtle.

Many people think the builder is the write pointer.

It isn’t.

The builder is the growing suffix.

The pointer simply marks its boundary.

## State Changes

When beginners solve problems, they often think in terms of actions.

For example:

```
Move write.

Increment read.

Swap.

Compare.
```

Experienced engineers think more in terms of **state changes**.

For example:

```
A value became preserved.

The merged interval expanded.

The window became invalid.

The suffix grew.

The search space shrank.
```

The first describes **what the code does**.

The second describes **what happened to the system**.

## Information

> "What facts matter?"

Information is any fact that matters for producing the correct answer.

Example: Move Zeroes

The important information

```
Non-zero values:
1
3
12

Their order:
1 before 3 before 12
```

Everything else (the exact positions of the zeros) is less important.

Example: Insert Interval

The important information

```
The final merged interval.
```

Information exists outside the program.

It comes from the problem itself.

## State

> "How am I storing those facts?"

State is how the program currently represents the information.

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

## Ownership

> "Where is the correct copy of the information?"

Ownership means where does the current correct copy of the information live?

Suppose:

```
1 3 ? ? 12
```

Where does the information:

```
1
```

live?

Answer:

```
The preserved region.
```

That region owns the information.

If I overwrite an old copy elsewhere:

Nothing is lost.

Ownership had already transferred.

## Design Process

```
1. What information matters?

↓

2. How should I represent it? (State)

↓

3. Where does the correct version currently live? (Ownership)

↓

4. Is any of that information still being built? (Builder)

↓

5. What statement must remain true? (Invariant)

↓

6. Write the code.
```

For any piece of state, ask:

- Source – Where does the information currently come from?
- Builder – Is there an object accumulating or combining information?
- Finished – What information is now correct forever?
- Discarded – What information no longer matters?
- Unknown – What hasn't been examined yet?

## Drill

Question:

Find the sum of all positive numbers in an array.

```
1. Information: What fact are we trying to preserve?
The sum of all positive numbers examined so far.

2. State: How would you store that fact?
cur_sum = 0
cur_sum is the program’s representation of that information.

3. Builder: Is any state gradually building new information?
cur_sum is acting as the builder:
It gradually builds the final sum as we examine more input.

4. Invariant: What statement should always be true at the start of each loop iteration?
cur_sum contains the sum of all positive numbers examined so far.
```

## Invariant Drill 1

Problem:

```
Given an array of numbers,
return the first negative number.
```

Example:

```
[3, 8, -2, 5, -7]
```

Answer:

```
-2
```

```
1. What is the fact we are trying to preserve?
The first negative number among the elements examined so far.

2. How would you store that information?

Option A (one variable)

We could use:

result = None

Later:

result = -2

means:

The first negative is -2.

One variable stores both possibilities.

Option B (two variables)

found = False
result = ?

Now the state is split into:

- whether we've found one
- what the value is

3. Transition

Suppose we are examining one number.

Three possibilities:
```

positive

negative (first one)

negative (after we've already found one)

```
How should the state change in each case?

| Current element | State change    |
| --------------- | --------------- |
| Positive        | Nothing changes |
| First negative  | Store it        |
| Later negative  | Nothing changes |


4. Invariant
Before examining the current element:
- if a negative number has already been seen, result stores the first one;
- otherwise, result indicates that none has been found yet.
```
