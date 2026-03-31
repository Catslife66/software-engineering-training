# Pattern 2 - House Robber / Decision DP (choose or not choose)

Optimisation DP

> maximize value → use max()

## House Robber

> choose the better possibility

You are given a list:

```
[2, 1, 1, 2]
```

Each number = money in a house.

Rule:

```
You cannot rob two adjacent houses
```

Goal:

Maximise total money

**f(i) = maximum money we can rob from houses 0..i**

```
step 1 - only rob one house
f(0) = 2

step 2 - choose best of first two houses
rob house 0 -> 2
rob house 1 -> 1
f(1) = 2 because max(2, 1) = 2

step 3 - 2 options
option 1 -> rob house 2 then we cannot rob house 1 so
money = f(0) + houses[2] = 3

option 2 -> skip house 2
money = f(1) = 2

f(2) = 3 because max(3, 2) = 3

step 4 - 2 options
option 1 -> rob house 3 then we cannot rob house 2 so
money = f(1) + houses[3] = 2 + 2 = 4

option 2 -> skip house 3
money = f(2) = 3

f(4) = 4 because max(4, 3) = 4
```

**Key DP transition**

The recurrence is:

```
f(i) = max(f(i-1), f(i-2) + houses[i])
```

Read it like this:

**f(i-1) 👉🏻 Skip current house.**

**f(i-2) + houses[i] 👉🏻 Rob current house, so add its money to the best answer up to i-2.**

```
f(i) = max(
    nums[i] + f(i-2),   ← rob
    f(i-1)              ← skip
)
```

```
step 1 - rob only house 0
f(0) = 1

step 2 - 2 choices
rob house 0 -> 1
rob house 1 -> 2
f(1) = 2 because max(1, 2) = 2

step 3 - 2 options
option 1: rob house 2
money = f(0) + houses[2] = 4

option 2: skip house 2
money = f(1) = 2

f(2) = 4 because max(4, 2) = 4

step 4 - 2 options
option 1: rob house 3
money = f(1) + houses[3] = 3

option 2: skip house 3
meney = f(2) = 4

f(3) = 4 because max(3, 4) = 4
```

## The reusable DP checklist

For every DP problem, use this:

```
1. State: what does f(i) mean?
2. Choices: what can I do at i?
3. Transition: how do I compute f(i)?
4. Base cases: what are the smallest known answers?
```

For House Robber:

```
State:
f(i) = max money from houses 0..i

Choices:
rob house i
skip house i

Transition:
f(i) = max(f(i-1), f(i-2) + houses[i])

Base cases:
f(0) = houses[0]
f(1) = max(houses[0], houses[1])
```

```
step 0 - rob only house 0
f(0) = 2

step 1 - the best choice of the first 2 houses
rob house 0 -> 2
rob house 1 -> 7
f(1) = 7 because max(2, 7) = 7

step 2 - 2 options
option 1: rob house 2
money = f(0) + houses[2] = 11
option 2: skip house 2
money = f(1) = 7

f(2) = 11 because max(11, 7) = 11

step 3 - 2 options
option 1: rob house 3
money = f(1) + houses[3] = 10
option 2: skip house 3
money = f(2) = 11

f(3) = 11 because max(11, 10) = 11

step 4 - 2 options
option 1: rob house 4
money = f(2) + houses[4] = 12
option 2: skip house 4
money = f(3) = 11

f(4) = 12 because max(12, 11) = 12
```

## Code idea

```
prev2 = houses[0]
prev1 = max(houses[0], houses[1])

for i in range(2, len(houses)):
    curr = max(prev2, prev1)
    prev2 = prev1
    prev1 = curr

return prev1
```

🤔 So why return prev1 instead of curr?

Because:

prev1 is guaranteed to hold the final result

while:

curr only exists inside the loop

So:

return prev1 is safer in case the loop doesn't run, curr may not exist
