## Question 1 - return index of the target

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

## Question 2 - return FIRST index of the target

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
2. Why: because the array is sorted and there might be an earlier occurrence on the left
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

## Question 3 - return index where target should be inserted

```
nums = [1, 3, 5, 7]
target = 6

Return the index where target should be inserted
```

Solution:

```
Step 1: left = 0, right = 3, mid = 1 -> 3 < 6, move left = mid + 1
Step 2: left = 2, right = 3, mid = 2 -> 5 < 6, move left = mid + 1
Step 3: left = 3, right = 3, mid = 3 -> 7 > 6, move right = mid - 1
Step 4: left = 3, right = 2, right < left, stop, return left
Final result: return 3
```

Key insight (VERY important)

```
When target is NOT found → return left
```

Because left is the insertion position

## Key patterns

```
nums[mid] == target:

Find ANY → return immediately
Find FIRST → result = mid, move right = mid - 1
Find LAST → result = mid, move left = mid + 1
```
