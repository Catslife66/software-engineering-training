## Problem

You are given:

```
nums = [1, 2, 3, 4, 5]
target = 9
```

Return whether there are two numbers in the array that add up to target.

\*Important detail: The array is sorted.

## Solution

```
1. Pattern:
Two pointers

2. Why:
Because the array is sorted, moving pointers changes the sum predictably:
- moving left increases sum
- moving right decreases sum

3. Approach:
- Initialize left = 0 and right = n-1
- While left < right:
    - If nums[left] + nums[right] == target → return True
    - If sum < target → move left pointer right
    - If sum > target → move right pointer left
- Return False if no pair is found

4. Walkthrough:
[1,2,3,4,5], target = 9
→ 1+5=6 → move left
→ 2+5=7 → move left
→ 3+5=8 → move left
→ 4+5=9 → found → True
```
