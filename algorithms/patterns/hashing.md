## Question 1 - return True if duplicates exist

If I give you:

[3, 1, 4, 1, 5]

👉🏻 Find if duplicates exist.

Solution:

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

## Question 2 - find sum and return indeces

Find TWO numbers whose sum is target and return their indeces.

```
nums = [3, 2, 4]
target = 6
```

Solution:

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

## Question 3 - return unique elements

nums = [1, 2, 2, 3]

Return all unique elements (no duplicates, keep order)

Solution:

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

## Question 4 - return all unique pairs

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

**Solution**

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

## Question 5

```
nums = [1, 2, 3, 1]
k = 3
```

Question

```
Return True if there are two distinct indices i and j such that:
nums[i] == nums[j] AND |i - j| <= k
```

Solution:

```
1. Pattern:
Hash Map

2. Why:
We need to check both value equality and index distance.
A map allows O(1) lookup of previous indices.

3. Approach:
- Use a map to store the last seen index of each number
- Iterate through the array:
    - If number exists in map:
        - check if current index - last index <= k
        - if yes → return True
    - Update the index in the map ❗️important
- Return False if no valid pair is found

4. Walkthrough:
[1,2,3,1], k=3
→ 1 at 0 → store
→ 2 at 1 → store
→ 3 at 2 → store
→ 1 at 3 → 3-0 = 3 ≤ k → True
```

Code implementation:

```
class Solution:
    def containsNearbyDuplicate(self, nums: List[int], k: int) -> bool:
        seen = {}

        for i, num in enumerate(nums):
            if num in seen and i - seen[num] <= k:
                return True

            # Always update to the latest index to maximize the chance of a future match being within distance k
            seen[num] = i

        return False
```

**Correct idea**

Instead of:

```
store first occurrence only
```

We should:

```
update the index every time
```
