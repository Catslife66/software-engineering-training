## Problem 1

You are given:

nums = [2, 3, 1, 2, 4, 3]
target = 7

Question:

```
Find the length of the smallest contiguous subarray
whose sum is greater than or equal to target.
If no such subarray exists, return 0.
```

## Solution

```
1. Pattern:
Variable-size sliding window

2. Why this pattern:
The problem asks for the smallest length of a contiguous subarray with sum at least target.
That suggests using a sliding window that expands until valid, then shrinks to minimize length.

3. Approach:
- Initialize left = 0, window_sum = 0, min_length = infinity
- Iterate right through the array
- Add nums[right] to window_sum
- While window_sum >= target:
    - update min_length with current window size
    - subtract nums[left] from window_sum
    - move left forward to shrink the window
- At the end, return 0 if min_length was never updated, otherwise return min_length

4. Walkthrough:

[2] → sum 2
[2,3] → sum 5
[2,3,1] → sum 6
[2,3,1,2] → sum 8 → valid, min = 4
shrink → [3,1,2] → sum 6
expand → [3,1,2,4] → sum 10 → valid, min = 4
shrink → [1,2,4] → sum 7 → valid, min = 3
shrink → [2,4] → sum 6
expand → [2,4,3] → sum 9 → valid, min = 3
shrink → [4,3] → sum 7 → valid, min = 2
shrink → [3] → sum 3
Answer = 2
```

## Question 2

You are given a string:

s = "eceba"

❓ Question

Find the length of the longest substring that contains at most 2 distinct characters.

## Solution

```
1. Pattern:
   Variable-size sliding window + Hash Map

2. Why:
   We are looking for the longest contiguous substring with a dynamic condition:
   at most 2 distinct characters. That suggests a sliding window that expands
   and shrinks as needed.

3. Approach:

- Use a hash map to store character frequencies in the current window
- Expand the window with the right pointer
- If the window has more than 2 distinct characters, shrink from the left
- While shrinking, decrement the frequency of the left character
- If a character’s count becomes 0, remove it from the map
- Track the maximum valid window length

4. Walkthrough:
   "e" → valid
   "ec" → valid
   "ece" → valid, max = 3
   "eceb" → invalid (3 distinct), shrink to "eb"
   "eba" → invalid (3 distinct), shrink to "ba"
   Final answer = 3
```

## Code implement

```
def length_of_longest_substring_two_distinct(s):
left = 0
max_length = 0
freq = {}

    for right in range(len(s)):
        char = s[right]
        freq[char] = freq.get(char, 0) + 1

        while len(freq) > 2:
            left_char = s[left]
            freq[left_char] -= 1
            if freq[left_char] == 0:
                del freq[left_char]
            left += 1

        max_length = max(max_length, right - left + 1)

    return max_length
```

## Question 3

You are given a string:

```
s = "aabccbb"
```

Return the length of the longest substring without repeating characters.

```
1. Pattern: variable-size sliding window

2. Why: Because the question is asking for the longest substring, so we need to use a variable size sliding window to shrink or expand the substring

3. Approach:
- initialise a hash map to store frequency of the charaters in the substring
- iterate right through string:
    if char is not in seen, keep expanding
    while char is in seen, shrink from the left
      if a char counts 0, remove from seen
- Track the max_length from valid substrings

4. Walkthrough:

"a" - valid, seen: {"a": 1}
"aa" - invalid, shrink to "a"
"ab" - valid
"abc" - valid, max = 3
"abcc" - invalid, shrink to "c"
"cb" - valid
"cbb" - invalid, shrink to "b"

5. Final answer: 3


def longest_substring_with_unique_char(s):
    freq = {}
    left = 0
    max_length = 0

    for right in range(len(s)):
        char = s[right]
        freq[char] = freq.get(char, 0) + 1

        while freq[char] > 1:
            left_char = s[left]
            freq[left_char] -= 1
            if freq[left_char] == 0:
                del freq[left_char]
            left += 1

        max_length = max(max_length, right - left + 1)

    return max_length
```
