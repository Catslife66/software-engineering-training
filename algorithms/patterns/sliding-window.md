## Problem

You are given:

nums = [2, 3, 1, 2, 4, 3]
target = 7

Question:

```
Find the length of the smallest contiguous subarray
whose sum is greater than or equal to target.
If no such subarray exists, return 0.
```

## Solution

```
1. Pattern:
Variable-size sliding window

2. Why this pattern:
The problem asks for the smallest length of a contiguous subarray with sum at least target.
That suggests using a sliding window that expands until valid, then shrinks to minimize length.

3. Approach:
- Initialize left = 0, window_sum = 0, min_length = infinity
- Iterate right through the array
- Add nums[right] to window_sum
- While window_sum >= target:
    - update min_length with current window size
    - subtract nums[left] from window_sum
    - move left forward to shrink the window
- At the end, return 0 if min_length was never updated, otherwise return min_length

4. Walkthrough:

[2] → sum 2
[2,3] → sum 5
[2,3,1] → sum 6
[2,3,1,2] → sum 8 → valid, min = 4
shrink → [3,1,2] → sum 6
expand → [3,1,2,4] → sum 10 → valid, min = 4
shrink → [1,2,4] → sum 7 → valid, min = 3
shrink → [2,4] → sum 6
expand → [2,4,3] → sum 9 → valid, min = 3
shrink → [4,3] → sum 7 → valid, min = 2
shrink → [3] → sum 3
Answer = 2
```
