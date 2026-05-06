## Question 1

You are given:

```
nums = [1, 3, 5, 6]
target = 5
```

Question:

```
Return the index if the target is found.
If not, return the index where it would be inserted
to keep the array sorted.
```

🎯 Example

```
target = 5 → return 2
target = 2 → return 1
target = 7 → return 4
```

⚠️ Requirements

```
O(log n) time complexity expected
```

Solution:

```
1. Pattern:
Binary Search

2. Why:
Array is sorted AND we need O(log n)
→ must use binary search

3. Approach:
- left = 0, right = n - 1
- while left <= right:
    mid = (left + right) // 2

    if nums[mid] == target:
        return mid
    elif nums[mid] < target:
        left = mid + 1
    else:
        right = mid - 1

- return left

4. Walkthrough:
left=0, right=3 → mid=1 → 3 < 5 → left=2
left=2, right=3 → mid=2 → 5 == 5 → return 2

```

## Question 2

```
nums = [1, 2, 2, 2, 3, 4]
target = 2
```

Question:

```
Return the FIRST index of target.
If not found, return -1.
```

**Solution**

```
1. Pattern: Binary search (modified)
2. Why: because the array is sorted and the time complexity must be O(log n)
3. Approach:
    - left = 0, right = n - 1
    - result = -1
    - while left <= right:
        mid = (left + right) // 2

        if nums[mid] == target:
            result = mid        ← store it
            right = mid - 1     ← move LEFT to find earlier

        elif nums[mid] < target:
            left = mid + 1

        else:
            right = mid - 1
    - return result

4. Walkthrough:
step 1:
left=0, right=5 → mid=2 → nums[2]=2
→ found → result = 2
→ move right = 1

step 2:
left=0, right=1 → mid=0 → nums[0]=1 < 2
→ move left = 1

step 3:
left=1, right=1 → mid=1 → nums[1]=2
→ found → result = 1
→ move right = 0

stop:
left > right


5. Final answer: 1
```
