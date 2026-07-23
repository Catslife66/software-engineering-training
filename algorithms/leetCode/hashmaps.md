# State Design with HashMaps

## 290 — Word Pattern (Easy)

Pattern Family: Hash Map / Bidirectional Mapping

What does my dictionary represent?
mapping characters with words

Why do I probably need two dictionaries?
To guarantee the mapping is one-to-one.

What invariant am I maintaining?
Every character always maps to the same word, and every word always maps to the same character.

## 219 — Contains Duplicate II (Easy)

Pattern Family: map lookup

What information is stored?
the num is seen and its index

Why is the index useful?
because we need to find if there are two distinct indices i and j in the array such that nums[i] == nums[j] and abs(i - j) <= k.

When do I update it?
if the num is in seen update its value to the latest index

## 451 — Sort Characters By Frequency (Medium)

```
class Solution:
    def frequencySort(self, s: str) -> str:
        s_map = {}
        for char in s:
            s_map[char] = s_map.get(char, 0) + 1

        sorted_s = sorted(s_map.items(), key=lambda x: x[1], reverse=True)

        str = ""
        for char, freq in sorted_s:
            str += char * freq

        return str
```

## 560 — Subarray Sum Equals K (Medium)

Pattern Family:

What state am I keeping while scanning?

Why isn't frequency counting enough?

What question am I asking the hash map?

## 525 — Contiguous Array (Medium)

Given a binary array nums, return the maximum length of a contiguous subarray with an equal number of 0 and 1.

Example 1:

```
Input: nums = [0,1]
Output: 2
Explanation: [0, 1] is the longest contiguous subarray with an equal number of 0 and 1.
```

Example 2:

```
Input: nums = [0,1,0]
Output: 2
Explanation: [0, 1] (or [1, 0]) is a longest contiguous subarray with equal number of 0 and 1.
```

Example 3:

```
Input: nums = [0,1,1,1,1,1,0,0,0]
Output: 6
Explanation: [1,1,1,0,0,0] is the longest contiguous subarray with equal number of 0 and 1.
```

```
class Solution:
    def findMaxLength(self, nums: List[int]) -> int:
        first_index = {0: -1}
        longest = 0
        running_total = 0

        for i, num in enumerate(nums):
            if num == 0:
                running_total -= 1
            else:
                running_total += 1

            if running_total in first_index:
                longest = max(
                    longest,
                    i - first_index[running_total]
                )
            else:
                first_index[running_total] = i

        return longest
```

## 599 — Minimum Index Sum of Two Lists (Easy)

```
class Solution:
    def findRestaurant(self, list1: List[str], list2: List[str]) -> List[str]:
        idx_min = float('inf')
        list1_map = {}

        for i in range(len(list1)):
            list1_map[list1[i]] = i

        string_list = []
        for i in range(len(list2)):
            if list2[i] in list1_map:
                idx_sum = i + list1_map[list2[i]]
                if idx_min > idx_sum:
                    idx_min = idx_sum
                    # clear out old result
                    string_list = [list2[i]]
                elif idx_sum == idx_min:
                    string_list.append(list2[i])

        return string_list
```

## 594 — Longest Harmonious Subsequence

Sliding window

```
class Solution:
    def findLHS(self, nums: List[int]) -> int:
        sorted_nums = sorted(nums)

        left = 0
        longest = 0

        for right in range(len(sorted_nums)):

            while sorted_nums[right] - sorted_nums[left] > 1:
                left += 1
            if sorted_nums[right] - sorted_nums[left] == 1:
                longest = max(longest, right - left + 1)

        return longest
```

Hashmap

```
class Solution:
    def findLHS(self, nums: List[int]) -> int:
        freq = {}
        for num in nums:
            freq[num] = freq.get(num, 0) + 1

        longest = 0
        for num in nums:
            if (num + 1) in freq:
                longest = max(longest, freq[num] + freq[num+1])

        return longest
```
