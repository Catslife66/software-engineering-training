# Valid Anagram - 242

Given two strings s and t, return true if t is an anagram of s, and false otherwise.

_An anagram is a word or phrase formed by rearranging the letters of a different word or phrase, using all the original letters exactly once._

**Example 1:**

Input: s = "anagram", t = "nagaram"

Output: true

**Example 2:**

Input: s = "rat", t = "car"

Output: false

**Constraints:**

```
1 <= s.length, t.length <= 5 * 10^4
s and t consist of lowercase English letters.
```

```
class Solution:
    def isAnagram(self, s: str, t: str) -> bool:
        if len(s) != len(t):
            return False

        map = {}

        for char in s:
            map[char] = map.get(char, 0) + 1

        for char in t:
            if char not in map or map[char] == 0:
                return False
            map[char] -= 1

        return True
```
