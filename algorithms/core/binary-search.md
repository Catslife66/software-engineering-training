# Binary Search

## Why Binary Search Is Powerful

Instead of scanning every element: **O(n)**

Binary search cuts the problem in half each step: **O(log n)**

Example comparison:
| n elements | linear search | binary search |
| ------------- | ----------------- | ------------- |
| 1,000 | up to 1000 checks | ~10 checks |
| 1,000,000 | up to 1M checks | ~20 checks |
| 1,000,000,000 | up to 1B checks | ~30 checks |

That’s a massive difference.

**Python Implementation**

```
def binary_search(arr, target):

    left = 0
    right = len(arr) - 1

    while left <= right:

        mid = (left + right) // 2

        if arr[mid] == target:
            return mid

        elif arr[mid] < target:
            left = mid + 1

        else:
            right = mid - 1

    return -1
```

## The Three Key Rules of Binary Search

1️⃣ The array must be sorted

2️⃣ Maintain a search range [left, right]

3️⃣ Every iteration cuts the range in half

## The Real Skill of Binary Search

The hard part is not the basic algorithm.

The hard part is learning how to use binary search on answer spaces, not just arrays.

Examples later will include problems like:

- find the first occurrence
- find the last occurrence
- find the minimum in rotated array
- search in infinite array
- find square root

These are very common interview problems.
