# general DFS / backtracking

which includes:

- subsets
- permutations
- combination sum
- generate parentheses
- grid DFS

## Drill 1 - Generate all subsets

input: nums = [1, 2]

expected output:

```
[]
[1]
[2]
[1,2]
```

**Key idea**

For EACH number:

```
2 choices:
- take it
- skip it
```

Solution:

```
1. What does f(index, path) represent?
Explore all subset decisions starting from index,
while path stores the current subset being built.

2. Base case?
if index == len(nums):
    result.append(path.copy())
    return

3. Recursive choices?
    - Choice 1 — take element
    path.append(nums[index])
    f(index + 1, path)
    path.pop()

    - Choice 2 — skip element
    f(index + 1, path)

4. Cleanup?
path.pop()

5. Why is this true backtracking?
Because we are exploring multiple choices from the same state, and must undo one choice before trying another.
This is the purest form of: choose → explore → undo
```

Walkthrough:

```
start: f(0, [])
Choice 1: TAKE 1
    path.append(1)
    now path = [1]
    call f(1, [1])
        -> Choice 1A: TAKE 2
            path.append(2)
            now path = [1,2]
            call f(2, [1, 2])
                -> index == len(nums):
                        result.append([1, 2])
                    Backtracking step - undo TAKE 2 choice
                    path.pop()
                    now path = [1]
        -> Choice 1B: SKIP 2
            path = [1]
            call f(2, [1])
                -> index == len(nums):
                    result.append([1])
                Backtracking step - undo SKIP 2 choice
                path.pop()
                now path = []
Choice 2: SKIP 1
    path = []
    call f(1, [])
        -> Choice 2A: TAKE 2
        path.append(2)
        now path = [2]
        call f(2, [2])
            -> index == len(nums):
                result.append([2])
            Backtracking step - undo TAKE 2 choice
                path.pop()
                now path = []
        -> Choice 2B: SKIP 2
        now path = []
        call f(2, [])
            -> index == len(nums):
                result.append([])
```

Code implementation:

```
def f(index, path):

    if index == len(nums):
        result.append(path.copy())
        return

    # CHOICE 1: take nums[index]
    path.append(nums[index])
    f(index + 1, path)

    # undo
    path.pop()

    # CHOICE 2: skip nums[index]
    f(index + 1, path)
```

## Drill 2 — Generate Binary Strings

Problem

Generate all binary strings of length 2.

Expected output:

```
00
01
10
11
```

Solution:

```
1. What does f(path) represent?
generate all binary strings starting from the current partial string path

2. Base case?
if len(path) == 2:
    result.append(path)

3. What are the recursive choices?
choice 1 - take "0"
f(path + "0")

choice 2 = take "1"
f(path + "1")

4. Do we need backtracking? Why?
string is immutable → no pop needed

5. Walk through ONLY the branch that generates: "10"
start f("")
    -> take "0"
        path = "0"
        -> take "0"
            path = "00" -> len(path) == 2: add "00" to result
        -> take "1"
            path = "01" -> len(path) == 2: add "01" to result
    -> take "1"
        path = "1"
        -> take "0"
            path = "10" -> len(path) == 2: add "10" to result
        -> take "1"
            path = "11" -> -> len(path) == 2: add "11" to result
```

## Drill 3 — Binary strings using a list path
