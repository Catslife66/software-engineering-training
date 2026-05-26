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
    return

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

Problem

Generate all binary strings of length 2.

Expected output:

["00", "01", "10", "11"]

Solution:

```
1. What does f(path) represent?
generate all possible path combinations starting from current point

2. Base case?
if len(path) == 2:
    result.append("".join(path))
    return

3. Recursive choices?
choice 1 - take 0
path.append("0")
f(path)
path.pop()

choice 2 - take 1
path.append("1")
f(path)
path.pop()

4. Where does path.pop() happen?
after each child function call pop() to clean up

5. Why is backtracking needed this time?
because path is a shared mutable state.
```

## Drill 4 — Generate Parentheses (simplified)

Problem

Generate all valid parentheses with: n = 2

Expected output:

```
(())
()()
```

Solution:

```
1. What does f(path, openCount, closeCount) represent?
build valid parentheses from the current partial path

2. Base case?
if openCount == n and closeCount == n:
    result.append(path)
    return

3. When can we add "(" ?
if openCount < n:
    f(path + "(", openCount + 1, closeCount)

4. When can we add ")" ?
if closeCount < openCount:
    f(path + ")", openCount, closeCount + 1)

5. Do we need backtracking if path is a string?
no, we dont need backtracking for strings.

6. Is this top-down, bottom-up, or backtracking?
Backtracking / DFS with constraints. Even though string path does not need pop(), it is still backtracking because we are exploring choices add "(" or add ")" and pruning invalid choices.
```

Walkthrough:

```
start f("", 0, 0)
    add "(" -> f("(", 1, 0)
        add "(" -> f("((", 2, 0)
            add ")" -> f("(()", 2, 1)
                add ")" -> f("(())", 2, 2)
                    -> "(())" add to result

        add ")" -> f("()", 1, 1)
            add "(" -> f("()(", 2, 1)
                add" ")" -> f("()()", 2, 2)
                    -> "()()" add to result
```

## Drill 5 - Generate subsets using start

nums = [1, 2, 3]

Generate all subsets, but using this structure:

```
def f(start, path):
    result.append(path.copy())

    for i in range(start, len(nums)):
        path.append(nums[i])
        f(i + 1, path)
        path.pop()
```

Solution:

```
1. What does f(start, path) represent?
Explore all subsets that can be built starting from index start,
while path stores the current subset.

2. Why do we append path to result at the START of the function?
EVERY path is already a valid subset. So whenever we enter a function call: current path = one complete valid subset. That’s why we append immediately.

3. What does the for loop represent?
Try every possible next element starting from index start.

4. Why do we call f(i + 1, path)?
avoid reusing previous elements and ensure subsets grow forward only.

5. Why do we pop?
undo current choice before trying the next choice
```

### Compare the two subset styles

**Style A — binary decisions**

```
take
skip
```

Recursion tree explicitly binary.

**Style B — iterative DFS/backtracking**

```
for i in range(start, ...):
```

Choose next element dynamically.

## Drill 6 - Combination Sum (simplified)

Problem

```
nums = [2,3]
target = 6
```

We want combinations whose sum equals target.

Valid results:

```
[2,2,2]
[3,3]
```

Notice: we CAN reuse numbers

Solution:

```
1. What does f(start, remaining, path) represent?
Explore all combinations starting from index start,
while path stores the current combination
and remaining stores the remaining target sum needed.

2. Base case for success?
if remaining == 0:
    result.append(path.copy())
    return

3. Base case for failure?
if remaining < 0:
    return

4. What does the for loop represent?
Try every possible next number choice starting from start.

5. Why do we recurse with f(i, ...) instead of f(i + 1, ...)?
because we can reuse numbers
```

Code implementation:

```
def f(start, remaining, path):
    # success
    if remaining == 0:
        result.append(path.copy())
        return

    # failure
    if remaining < 0:
        return

    # choices
    for i in range(start, len(nums)):
        path.append(nums[i])
        f(i, remaining - nums[i], path)
        path.pop()
```

Visual model:

```
f(...)
    loop(for each possible choice):
        choose
        recurse deeper
        undo
        continue next choice

loop controls sibling choices
recursion controls depth

```

## Drill 7 - Choose TWO numbers

Generate all combinations of length 2

nums = [1,2,3]

Exepected:

```
[1,2]
[1,3]
[2,3]
```

Walkthrough:

```
start f(0, [])

loop i = 0:
    append 1 -> [1]
    call f(1, [1])

        loop i = 1:
            append 2 -> [1,2]
            f(2, [1,2]) -> add [1,2]
            pop -> [1]

        loop i = 2:
            append 3 -> [1,3]
            f(3, [1,3]) -> add [1,3]
            pop -> [1]

    return to f(0)
    pop -> []

loop i = 1:
    append 2 -> [2]
    call f(2, [2])

        loop i = 2:
            append 3 -> [2,3]
            f(3, [2,3]) -> add [2,3]
            pop -> [2]

    return to f(0)
    pop -> []

loop i = 2:
    append 3 -> [3]
    call f(3, [3])
    no more choices
    pop -> []
```

## Drill 8 - Permutations

Problem:

For permutations, we need to choose from any unused number each time.

nums = [1, 2]

Return all permutations:

```
[1,2]
[2,1]
```

Solution:

```
1. What does f(path, used) represent?
Generate all permutations from the current state,
while path stores the current permutation being built
and used stores which numbers are already chosen.

2. Base case?
if len(path) == len(nums):
    result.append(path.copy())
    return

3. What does the for loop represent?
try every possible unused number as the next choice

4. When do we add a number to used?
After choosing a number:
    path.append(num)
    used.add(num)

5. When do we remove it from used?
After recursion finishes:
    used.remove(num)
    path.pop()

6. Why do we need used here instead of start?
permutations allow choosing numbers in ANY order
```

Code implementation:

```
def f(path, used):

    if len(path) == len(nums):
        result.append(path.copy())
        return

    for num in nums:
        if num in used:
            continue

        path.append(num)
        used.add(num)

        f(path, used)

        used.remove(num)
        path.pop()
```

Key points:

```
Combinations:
future choices controlled by start index

Permutations:
future choices controlled by used set
```

Walkthrough:

```
start f([], {})
loop num = 1:
    num not in used
    path.append(1) ->[1]
    used.add(1) -> {1}
    f([1], {1})
        loop num = 1:
            num i used, continue
        loop num = 2:
            num not in used
            path.append(2) -> [1, 2]
            used.add(2) -> {1, 2}
            f([1, 2], {1, 2}) -> len(path) == len(nums): add [1, 2] to result
            used.remove(2) -> {1}
            path.pop() -> [1]
    used.remove(1) -> {}
    path.pop() -> []
loop num = 2:
    num not in used
    path.append(2) -> [2]
    used.add(2) -> {2}
    f([2], {2})
        loop num = 1:
            num not in used
            path.append(1) -> [2, 1]
            used.add(1) -> {2, 1}
            f([2, 1], {2, 1}) -> len(path) == len(nums): add [2, 1] to result
            used.remove(1) -> {2}
            path.pop() -> [2]
        loop num = 2:
            num in used, continue
```

## Drill 9 - Combination Sum with pruning

nums = [2,3,5]

target = 8

nums is sorted.

Code implementation:

```
def f(start, remaining, path):

    if remaining == 0:
        result.append(path.copy())
        return

    for i in range(start, len(nums)):
        if nums[i] > remaining:
            break
        path.append(nums[i])
        f(i, remaining - nums[i], path)
        path.pop()
```

## Drill 10 - Backtracking with duplicates

We want

```
skip duplicate sibling choices
```

But still allow

```
duplicate values deeper in recursion
```

Walkthrough:

```
Step 1 — Sort first

nums.sort()

Step 2 — Skip duplicate siblings

inside loop:

if i > start and nums[i] == nums[i - 1]:
    continue


nums.sort()

def f(start, path):

    result.append(path.copy())

    for i in range(start, len(nums)):
        if i > start and nums[i] == nums[i - 1]:
            continue

        path.append(nums[i])
        f(i + 1, path)
        path.pop()
```

## Drill 11 - Combination Sum II style

```
nums = [1, 1, 2, 5]

target = 3

Each number can be used once.

Expected result:
[1,2]
```

Code implementation:

```
def f(start, remaining, path):
    if remaining == 0:
        result.append(path.copy())
        return

    for i in range(start, len(nums)):

        if i > start and nums[i] == nums[i - 1]:
            continue

        if nums[i] > remaining:
            break

        path.append(nums[i])
        f(i + 1, remaining - nums[i], path)
        path.pop()
```

Walkthrough:

```
1. At root level, which elements are skipped and why?
At f(0, 3, [])
i = 0 -> take first 1, [1]
i = 1 -> i > start and nums[i] == nums[i-1], so second 1 is skipped
i = 2 -> take 2, [2]
i = 3 -> 5 > remaining, break

2. Walk through the branch that creates [1,2].
start f(0, 3, [])
    loop i = 0:
        f(1, 2, [1])
            loop i = 1:
                f(2, 1, [1, 1]) -> nums[2]= 2 > remaining = 1, break
                path.pop() -> [1]
            loop i = 2:
                f(3, 0, [1, 2]) -> remaining == 0, add [1, 2] to result
                path.pop() -> [1]
            loop i = 3:
                nums[3] > remaining, break
            path.pop() -> []
    loop i = 1:
        continue
    loop i = 2:
        f(3, 1, [2])
            loop i = 3: -> nums[2] > remaining, break
    loop i = 2:
        nums[2] > remaining, break

3. Where does [1,1,2] fail or get pruned?
at f(2, 1, [1, 1]), because nums[2] = 2 > remaining = 1

4. Where does value 5 get pruned?
at f(0, 3, []), because 5 > remaining, so break
```
