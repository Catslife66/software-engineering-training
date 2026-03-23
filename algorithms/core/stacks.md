## Stacks

A stack follows the rule:

> Last In, First Out (LIFO)

The most recently added element is the first one removed.

You should think stack when a problem involves:

- nested structures
- matching pairs
- undo operations
- evaluating expressions
- backtracking
- depth-first processing

Examples in real systems:

- code editors checking brackets
- HTML/XML parsing
- browser history
- function call stacks

## Example Problem:

Check whether a string of brackets is valid:

```
"{[()]}"
```

Rules:

- ( matches )
- { matches }
- [ matches ]

Example:

```
"{[()]}" → valid
"{[(])}" → invalid
```

**Python Implementation**

```
def is_valid_parenthesses(s):
    stack = []
    pairs = {
        ")" : "(",
        "}" : "{",
        "]" : "["
    }

    for char in s:
        if char in pairs.values():
            stack.append(char)
        else:
            // if the stack is empty then return false
            if not in stack:
                return False
            top = stack.pop()
            if pairs[char] != top:
                return False

    return len(stack) == 0
```

Time complexity: O(n)
Space complexity: O(n)

**Final rule for valid parentheses**

A string is valid only if:
1️⃣ Every closing bracket matches the correct opening bracket -> mismatch
2️⃣ No closing bracket appears without a matching opening bracket -> unclosed brackets
3️⃣ The stack is empty at the end
