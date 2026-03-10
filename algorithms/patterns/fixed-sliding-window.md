# Sliding Window Pattern

Problem: Find the maximum sum of any 3 consecutive elements.
Array: [2, 1, 5, 1, 3, 2]
We want the best sum of a window of size 3.

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

## The key rule you are learning

For a fixed window of size k:
new_window_sum = old_window_sum - outgoing + incoming
