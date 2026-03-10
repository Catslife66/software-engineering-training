# Variable-size Sliding Window

Problem: Find the smallest length of a contiguous subarray whose sum is greater than or equal to S.
Example:

```
arr = [2, 1, 5, 2, 3, 2]
S = 7
```

Possible subarrays with sum at least 7:
[2, 1, 5] → sum 8, length 3
[5, 2] → sum 7, length 2
[2, 3, 2] → sum 7, length 3

Best answer: 2

Walkthrough

```
[2] sum = 2, too small, expand
[2, 1] sum = 3, too small, expand
[2, 1, 5] sum = 8, valid

shrink from left:
length = 3 → min = 3, remove 2, window becomes
[1, 5] sum = 6, too small, expand

[1, 5, 2] sum = 8, valid
shrink from left, length = 3 → min = 3, remove 1, window becomes
[5, 2] sum = 7, valid
shrink from left, length = 2 → min = 2, remove 5, window becomes
[2] sum = 2

```

Time Complexity: O(n)
Space: O(1)

**Python implementation**

```
def smallest_subarray_with_given_sum(arr, s):
    left = 0
    window_sum = 0
    min_length = float("inf")

    for right in range(len(arr)):
        window_sum += arr[right]

        while window_sum >= s:
            min_length = min(min_length, right - left + 1)
            window_sum -= arr[left]
            left += 1

    return 0 if min_length == float("inf") else min_length
```

**Why this works**

The key idea is:

- each element enters the window once
- each element leaves the window once
