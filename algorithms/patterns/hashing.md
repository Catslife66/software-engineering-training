## Question 1

If I give you:

[3, 1, 4, 1, 5]

👉🏻 Find if duplicates exist.

```
Pattern: Hashing (Set)

Approach:
1. Create an empty set: seen
2. Loop through each number:
    if number in seen:
        return True
    else:
        add number to seen
3. Return False
```

## Question 2

Find TWO numbers whose sum is target and return their indeces.

```
nums = [3, 2, 4]
target = 6
```

```
Pattern: Hashing (Map)
Approach:
1. create an empty map: seen
2. loop through nums with indices:
        needed = target - num
        if needed in seen:
            return [seen[needed], currentIndex]
        else:
            seen[num] = currentIndex
```

Answer: [1, 2]

## Question 3

nums = [1, 2, 2, 3]

Return all unique elements (no duplicates, keep order)

```
Pattern: Hashing (Set)
Approach:
1. create empty list: result
2. create empty set: seen

3. loop through nums:
    if num not in seen:
        add num to result
        add num to seen

4. return result
```

Result: [1, 2, 3]

## Question 4

## Problem

You are given an array:

```
nums = [1, 2, 3, 4]
target = 5
```

❓ Question

```
Return all UNIQUE pairs of numbers that sum to target.
```

✅ Expected output

```
[[1, 4], [2, 3]]
```

## Solution

```
1. Pattern: Hashing (Set)

2. Why:
We need to find pairs that sum to target efficiently in an unsorted array.
Hashing allows O(1) lookup for complements.

3. Approach:
- Use a set to store seen numbers
- For each number:
    - compute needed = target - num
    - if needed is in seen → add pair
    - otherwise add num to seen
- ensure uniqueness if needed

4. Walkthrough:
[1,2,3,4]
→ pairs: [2,3], [1,4]
```
