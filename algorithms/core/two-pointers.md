# Two Pointers (sorted arrays)

This pattern works when **You process an array from two positions simultaneously.**

## Example Problem

Array: [1, 2, 3, 4, 6]
Target: 6

Question: Find two numbers that add up to 6.

**Pattern:**

```
1. left = 0, right = n-1
2. while left < right:
    sum = nums[left] + nums[right]

    if sum == target:
        return [left, right]

    elif sum > target:
        right -= 1

    else:
        left += 1
```

**Step by step:**

```
Start:
[1, 2, 3, 4, 6]
 L           R

Sum: 1 + 6 = 7
Too big → move right pointer left.

[1, 2, 3, 4, 6]
 L        R

Sum: 1 + 4 = 5
Too small → move left pointer right.

[1, 2, 3, 4, 6]
    L     R

Sum: 2 + 4 = 6
Found.
```

Time Complexity: O(n)
Space: O(1)

**Common problems solved with this pattern:**

- Two Sum (sorted arrays)
- Removing duplicates
- Palindrome checking
- Pair difference problems
- Container with most water
- Merging arrays

## Code implementation

**Python:**

```
def has_pair_with_sum(arr, target):
    left = 0
    right = len(arr) - 1

    while left < right:
        current_sum = arr[left] + arr[right]
        if current_sum == target:
            return True
        elif current_sum < target:
            left += 1
        else:
            right -= 1

    return False
```

## The key rule you are learning

In a sorted array:

- if sum is too small, move left right
- if sum is too big, move right left

Two pointers usually means:

- you have two positions
- often one starts at the beginning and one at the end
- you are often comparing pairs
- the array is often sorted

Typical use cases:

- find two numbers with target sum in a sorted array
- remove duplicates
- check palindrome
- merge sorted arrays

Two pointers relies on monotonic behavior (ordered values)
