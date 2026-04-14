# Hash Map / Set (Core Array Pattern)

**Problem:**

Given an array: [1, 2, 3, 1]

**Question:**

Does the array contain any duplicates?

**Final pattern**

```
1. create empty set: seen
2. loop through array:
    if element in seen:
        return True
    else:
        add element to seen
3. return False
```

**Code:**

```
def has_duplicate(nums):
    seen = set()

    for num in nums:
        if num in seen:
            return True
        seen.add(num)

    return False
```

**Optimized approach:**

```
store seen values → detect repeats instantly
→ O(n)
```

Set = fast memory of past elements

## Two Sum

### Set pattern

> value → have I seen it?

**Pattern:**

```
1. seen = empty set
2. loop through nums:
    needed = target - num
    if needed in seen:
        return True
    else:
        seen.add(num)
3. return False
```

**Key insight:**

```
We store what we've SEEN, // store num not needed!
and check if the complement already exists
use memory to avoid repeated work
```

**Big learning**

This pattern appears everywhere:

```
store past → check current
```

Not:

```
❌ store future → hope it appears ❌
```

### Map pattern

> value → where / how many / extra info

Example:

- index
- frequency
- last position
- grouped values

**Pattern:**

```
1. create empty map: seen
2. loop through array with index
3. needed = target - num
4. if needed in seen:
       return [seen[needed], current_index]
5. else:
       seen[num] = current_index
```

### Code implementation

**Python:**

```
// use Set
def two_sum(nums, target):
    seen = set()

    for num in nums:
        needed = target - num
        if needed in seen:
            return True
        seen.add(num)

    return False

// use Map
def two_sum(nums, target):
    seen = {}

    for i, num in enumerate(nums):
        needed = target - num

        if needed in seen:
            return [seen[needed], i]

        seen[num] = i

    return []
```

**Java:**

```
import java.util.HashSet;
import java.util.Set;

public class TwoSum {
    public static boolean twoSum(int[] nums, int target) {
        Set<Integer> seen = new HashSet<>();
        for (int num : nums) {
            int needed = target - num;
            if (seen.contains(needed)) {
                return true;
            }
            seen.add(num);
        }
        return false;
    }
}
```

Time complexity: O(n)

**Important Insight**

This pattern is **one of the most powerful ideas in algorithms**.
You will see it again in:

- Two Sum
- Subarray Sum
- Frequency counting
- Duplicate detection
- Graph algorithms
- Caching

## Hash-based lookup

**Hash Map**: key -> value
Used for:

- counting
- mapping
- storing extra information

**Hash Set**: unique values only
Used for:

- membership checking
- duplicates detection
- remembering visited elements
  Both give **average O(1) lookup time**, which is why they are so powerful.

A **hash map** is a data structure that stores **key → value pairs**.
A **set is actually a special case of a hash map**.
A **set** is a collection that stores **only unique values**.

Internally, most languages implement **sets using hash maps**.
Conceptually:

```
Set:
{7, 11, 15}

Internally like:

7  → true
11 → true
15 → true
```

So a set is basically: hash map where the value doesn't matter.

Set lookup = O(1)
Map lookup = O(1)

## General rule

```
Set  → “Have I seen this?”
Map  → “What do I know about this?”
```
