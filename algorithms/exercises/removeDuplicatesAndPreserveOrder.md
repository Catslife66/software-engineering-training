## Problem:

Given an array:
[1, 2, 2, 3, 4]

Return a new array with duplicates removed,
keeping the original order.

## Solution

```
1. create empty set: seen
2. create empty list: result

3. loop through nums:
    if num not in seen:
        add num to seen
        append num to result

4. return result
```

## Problem

Given a string:
"abcabcbb"

Find the length of the longest substring without repeating characters

## Solution

```
def length_of_longest_substring(s):
    seen = set()
    left = 0
    max_length = 0

    for right in range(len(s)):
        while s[right] in seen:
            seen.remove(s[left])
            left += 1

        seen.add(s[right])
        max_length = max(max_length, right - left + 1)

    return max_length
```

👉🏻 What represents "seen"?

seen = characters inside the CURRENT window

🔑 The cole rule

```
Expand window → add to seen
Shrink window → remove from seen
```

"abba"
