# Sliding Window Pattern

Recognition signal:

- subarray
- window size k
- continuous elements

Think:

```
Sliding window
```

## Fixed sliding window

Problem: Find the maximum sum of any 3 consecutive elements.
Array: [2, 1, 5, 1, 3, 2]
We want the best sum of a window of size 3.

**Key idea:**

```
new_sum = old_sum - outgoing + incoming
```

Instead of recompute sum in each loop

**Efficient approach:**

```
1. compute first window sum
2. slide the window:
    subtract left element
    add new right element
3. update max
```

**Step by step:**

```
Start:
[2, 1, 5, 1, 3, 2]
[2, 1, 5]
sum = 8
move a step

[2, 1, 5, 1, 3, 2]
    [1, 5, 1]
sum = 7
move a step

[2, 1, 5, 1, 3, 2]
      [5, 1, 3]
sum = 9
move a step

[2, 1, 5, 1, 3, 2]
         [1, 3, 2]
sum = 6
end of the array

Answer: 9

```

Time Complexity: O(n)
Space: O(1)

**Python Implementation**

```
def max_sum_subarray_k(arr, k):
    window_sum = sum(arr[:k])
    max_sum = window_sum

    for right in range(k, len(arr)):
        window_sum += arr[right] - arr[right - k]
        max_sum = max(max_sum, window_sum)

    return max_sum
```

**Common problems solved with this pattern:**

- maximum sum subarray of size k
- longest substring without repeating characters
- smallest subarray with sum at least S
- frequency-based substring problems
- real-time moving average systems

### The key rule you are learning

For a fixed window of size k:
new_window_sum = old_window_sum - outgoing + incoming

## Variable sliding window

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
[2] sum = 2, less than target, expand
[2, 1] sum = 3, less than target, expand
[2, 1, 5] sum = 8, valid

shrink from left:
length = 3 → min = 3, remove 2, window becomes
[1, 5] sum = 6, less than target, expand

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
