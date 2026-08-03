# Prefix Invariants

The invariant always describes the answer for the processed prefix.

General form:

```
state

=

correct answer for everything examined so far
```

This family includes:

- Maximum
- Minimum
- Sum
- Count
- Product
- XOR
- Average (with more than one state)
- Running frequency

This is actually one of the biggest families of algorithms.

## Running Value

There is always an answer for the processed part.

Examples:

- Maximum
- Minimum
- Sum
- Count

Invariant:

State equals the answer for the processed prefix.

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
