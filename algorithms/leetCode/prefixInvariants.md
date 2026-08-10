# Prefix Invariants

**Purpose**

Learn the simplest and most common invariant family.

This family appears everywhere:

- Running maximum
- Running minimum
- Running sum
- Running count
- Running average
- First occurrence
- Last occurrence
- Any?
- All?

Later, you'll discover that many "easy" LeetCode problems are actually the same idea.

```
Invariant:
State stores the answer for the processed prefix.

Transition:
Ask: "What event changes that answer?"

Code:
Update the state only when that event occurs.
```

## What is a Prefix?

Imagine the array is a ruler.

Initially:

```
|------------------------|
^
processed
```

After one element:

```
[7 | 2 9 5 1]
```

After three:

```
[7 2 9 | 5 1]
```

After five:

```
[7 2 9 5 1 | ]
```

The processed region always grows from the left.

Engineers call this region:

**The processed prefix**

The remaining part is:

**The unprocessed suffix**

Why "Prefix"?

Because in computer science:

```
abcdef
```

Prefixes are:

```
a
ab
abc
abcd
abcde
abcdef
```

Always from the beginning.

Arrays are the same.

## The first invariant family

Now something beautiful happens.

Suppose the problem is:

```
Find the maximum.
```

We already know the invariant:

```
largest stores the largest value examined so far.
```

Let's replace:

```
examined so far
```

with

```
processed prefix
```

Now it becomes:

```
largest stores the largest value in the processed prefix.
```

That's exactly the same idea—but with a more precise term.

## The magic sentence

I want you to remember this sentence for the entire module:

> Answer state = correct answer for the processed prefix.

This is the defining characteristic of a Prefix Invariant.

Examples:

Maximum:

```
largest = maximum of processed prefix
```

Sum:

```
sum = sum of processed prefix
```

Count positives:

```
count = number of positive values in processed prefix
```

Contains duplicate:

```
exists = whether processed prefix contains a duplicate
```

## Running Extremum

Used for:

- Maximum
- Minimum

Template:

```
Problem
Find an extremum
↓
Information
Current extremum
↓
State
best
↓
Invariant
best stores the extremum of the processed prefix
↓
Transition
If the current element is a better extremum, update best.
```

| Step         | Minimum                      | Maximum                      |
| ------------ | ---------------------------- | ---------------------------- |
| Final Answer | Minimum value                | Maximum value                |
| Information  | Minimum value                | Maximum value                |
| State        | Current extremum             | Current extremum             |
| Invariant    | Extremum of processed prefix | Extremum of processed prefix |
| Transition   | Current < minimum            | Current > maximum            |

## Running Accumulation

Examples:

- Sum
- Count
- Average (later)

Template:

```
Problem:
ccount the number of positive numbers
↓
Information:
the number of positive numbers in an array
↓
State:
count
↓
Invariant:
count stores the number of positive numbers of the processed prefix
↓
Transition:
count +1 if current element is a positive number, otherwise do nothing.
```

## First / Last Occurrence

Examples:

- First negative
- Last negative
- First duplicate

## Boolean Properties

Examples:

- Any positive?
- All positive?
- Contains duplicate?

## First Occurrence

An answer may not exist yet.

Examples:

- First negative
- First duplicate
- First even number
- First repeated character

Invariant:

State either contains the first occurrence in the processed prefix or records that none exists yet.

Sometimes the state must represent the absence of information, not just the information itself.

That's why values like:

```
None
null
Optional<T>
```

exist.

## Existence invariant

state = whether the processed prefix contains at least one matching element

Examples include:

- whether any negative exists
- whether any duplicate exists
- whether a target has appeared
- whether an error has occurred

The key engineering question is:

Can later input invalidate what we already know?

For an existence question, the answer is no. Once existence is proven, it stays proven.

### Drill

Count how many numbers are greater than the maximum value seen before them.

Example:

```
[3, 5, 4, 8, 8, 10]
```

The qualifying values are:

```
3, 5, 8, 10
```

So the answer is:

```
4
```

```
1. information
How many examined elements were greater than every element before them.

2. state
maximum = largest value examined so far
count = number of examined values that became a new maximum

3. transition
When the current value is greater than the previous maximum:
- count increases
- maximum becomes the current value

So both pieces of state change.

Conceptually:

current > maximum
        ↓
current establishes a new record
        ↓
count grows
maximum is replaced

When the current value is not greater:

- count stays
- maximum stays

4. Invariant
Before examining the current element:
- maximum is the largest value among the previously examined elements.
- count is the number of previously examined elements that were greater than all elements before them.
```

## Historical state & Position state

Problem:

Return the largest increase between consecutive numbers.

Example:

```
[4, 7, 3, 10, 8]
```

Consecutive changes:

```
4 → 7   increase = 3
7 → 3   decrease
3 → 10  increase = 7
10 → 8  decrease
```

Answer:

```
7
```

Information: What facts must be preserved while scanning?

- the previous examined number;
- the largest positive consecutive increase found so far.

State: Which variables could store those facts?

- previous = most recently examined number
- largest = largest positive consecutive increase found so far

Transition: When examining the current number, what comparison must happen?

- compare previous → current
- update largest if necessary
- current becomes previous for the next iteration

Invariant: Before examining the current number, what must each state variable mean?

Before examining the current number:

- previous stores the immediately preceding number;
- largest stores the largest positive increase between any consecutive pair examined so far.

This problem contains two different state roles:

```
previous
= local context needed for the next step

largest
= accumulated answer from the full processing history
```

This distinction appears frequently:

- previous/current values;
- previous/current intervals;
- parent/current nodes;
- last seen/current character;
- previous transaction/current transaction.

Problem:

Count how many times the direction changes between increasing and decreasing.

Example:

```
[1, 4, 7, 5, 3, 6]
```

The direction changes twice:

```
increasing → decreasing
decreasing → increasing
```

```
1. What historical answer must be maintained?
- The immediately preceding value.
- The most recent established direction.
- The number of direction changes detected so far.


2. Which state variables might represent those facts?

For each current number, compare it with previous.

- current > previous  → current direction is increasing
- current < previous  → current direction is decreasing

Then:

- If no previous direction exists, store the current direction.
- If the current direction matches the previous direction, do not increase count.
- If they differ, increase count and replace previous_direction.
- Finally, the current number becomes previous.

3. Invariant: What must be true before examining the current number?
Before examining the current number:

- previous stores the immediately preceding number;
- previous_direction stores the most recent non-equal direction established among the processed consecutive pairs, or indicates that no direction exists yet;
- count stores the number of times that established direction has changed among the processed consecutive pairs.

```

## Answer state & Context state

**Answer State**

This gradually becomes the final answer.

Examples:

```
largest
count
sum
result
```

These all eventually become the output.

> Does this represent part or all of the answer built so far?

If yes, it is answer state.

**Context State**

This is not part of the final answer.

It only exists to help us process the next input.

Examples:

```
previous
previous_direction
last_seen_character
parent
current_node
```

These variables are like short-term memory.

> Does this only help me decide what to do next?

If yes, it is context state.

### Drill

A: Find the minimum value.

```
Answer State:
minimum_so_far

Context State:
none required
```

B: Reverse an array.

```
Answer state:
the portions already placed in their final reversed positions

Context state:
left pointer
right pointer
temporary value during a swap
```

C: Find the longest word in a sentence.

```
Answer state:
longest_word_so_far

Context state:
current word being examined

Why is current_word context?

Because we temporarily need it to compare:

length of current_word
vs
length of longest_word_so_far

After the comparison, the old current word may no longer matter.
```

D: Merge two sorted arrays.

```
Answer state:
the correctly merged prefix built so far

Context state:
the positions of the next unread values in both arrays
```
