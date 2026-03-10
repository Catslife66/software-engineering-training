# Hash Lookup

Structure:

```
for each number:
    needed = target - number
    if needed already seen
        solution found
    store number
```

**Python Implementation:**

```
def two_sum(nums, target):
    seen = set()

    for num in nums:
        needed = target - num
        if needed in seen:
            return True
        seen.add(num)

    return False
```

**Java Implementation:**

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
Notice the algorithm structure:

```
scan array once
remember past values
use memory to avoid repeated work
```

This pattern is **one of the most powerful ideas in algorithms**.
You will see it again in:

- Two Sum
- Subarray Sum
- Frequency counting
- Duplicate detection
- Graph algorithms
- Caching

## Hash-based lookup\*\*

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

So a set is basically: hash map where the value doesn't matter
