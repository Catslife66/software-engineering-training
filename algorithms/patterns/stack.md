## Problem

Given a string:

"s = (())()"

Return True if the string is valid (balanced parentheses), otherwise return False.

## Solution

```
1. Pattern:
Stack

2. Why:
Balanced parentheses is a matching problem with nested structure.
The most recent opening bracket must match the next closing bracket,
which fits stack (LIFO).

3. Approach:
- Initialize an empty stack
- Traverse the string
- If the character is '(', push it onto the stack
- If the character is ')':
  - if the stack is empty, return False
  - otherwise pop the top opening bracket
- After the loop, return True only if the stack is empty

4. Walkthrough:
( → push → ['(']
( → push → ['(', '(']
) → pop  → ['(']
) → pop  → []
( → push → ['(']
) → pop  → []

End: stack is empty → valid
```

Answer: True
