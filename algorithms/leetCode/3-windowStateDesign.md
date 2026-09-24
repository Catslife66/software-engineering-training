# Window State Design

**What information describes the current window, and how can I update that information without recomputing everything?**

## What Is a Window?

Suppose:

```
nums = [4, 2, 7, 1, 8, 3]
```

and we're examining three consecutive elements at a time.

We might have:

```
[4, 2, 7] 1  8  3
```

then:

```
 4 [2, 7, 1] 8  3
```

then:

```
 4  2 [7, 1, 8] 3
```

The window is simply the contiguous region we're currently interested in.

We can describe it with boundaries:

```
left                 right
 ↓                     ↓
[2,       7,           1]
```

But here's where engineering thinking matters.

Suppose the problem asks:

> Find the maximum sum of any 3 consecutive numbers.

A beginner solution could calculate every window from scratch:

```
4 + 2 + 7 = 13
2 + 7 + 1 = 10
7 + 1 + 8 = 16
1 + 8 + 3 = 12
```

That works.

But notice what happens when:

```
[4, 2, 7]
```

becomes:

```
[2, 7, 1]
```

Most information hasn't changed.

We lost:

```
4
```

and gained:

```
1
```

So if the old window sum was 13:

```
new_sum = 13 - 4 + 1
         = 10
```

We updated the state instead of rebuilding it.

That is the heart of sliding-window thinking:

> **When a contiguous region moves slightly, preserve useful information about the old region and update only what changed.**

Example:

Given:

```
nums = [5, 2, 8, 4, 1, 7]
k = 3
```

We want the maximum sum of any 3 consecutive elements.

```
Information:
maximum sum of any 3 consecutive elements

Window state:
win_sum = sum of current 3-element window

Best-answer state:
max_sum = largest window sum seen so far

Transition:
subtract the value leaving the window add the value entering the window

Invariant:
win_sum equals the sum of the current 3 consecutive elements
max_sum equals the largest window sum seen so far
```

## Fixed-Size Sliding Window

Problem:

```
nums = [5, 2, 8, 4, 1, 7]
k = 3
```

Find the maximum sum of any k consecutive elements.

The key idea is:

```
build first window once
↓
slide one step at a time
↓
subtract what leaves
add what enters
↓
update best answer
```

Step 1 — Build the first window

First window:

```
[5, 2, 8]
```

So:

```
win_sum = 15
max_sum = 15
```

At this moment:

> win_sum is the sum of the current size-3 window.

Step 2 — Slide right

Next window:

```
[2, 8, 4]
```

Instead of recalculating:

```
2 + 8 + 4
```

we update:

```
win_sum = 15 - 5 + 4
        = 14
```

Then:

```
max_sum = max(15, 14)
        = 15
```

Next:

```
[8, 4, 1]
```

Update:

```
win_sum = 14 - 2 + 1
        = 13
```

Next:

```
[4, 1, 7]
```

Update:

```
win_sum = 13 - 8 + 7
        = 12
```

Final answer:

```
15
```

### The fixed-window invariant

A useful invariant is:

> Before evaluating each completed window, win_sum equals the sum of exactly the k elements in that window.

And separately:

> max_sum equals the largest completed window sum seen so far.

Notice again: there are two kinds of state.

```
current-window state
+
best-answer state
```

That distinction will keep appearing.

### Standard shape

In Python, the pattern often looks like:

```
win_sum = sum(nums[:k])
max_sum = win_sum

for right in range(k, len(nums)):
    left = right - k

    win_sum -= nums[left]
    win_sum += nums[right]

    max_sum = max(max_sum, win_sum)
```

Don't memorise this yet.

Read it structurally:

```
right
= new element entering

right - k
= old element leaving
```

So:

```
current window
        ↓
| ... | leaving | ...window... | entering |
        ↑                       ↑
      right-k                 right
```

The formula comes from the window size, not magic.

Why is the leaving index right - k?

When `right` represents the new element entering a fixed-size window of size k, `right - k` identifies the element that has just fallen outside that window.

This is another example of an engineering principle we've already encountered:

> **Don't maintain independent state when it can be derived reliably from existing state.**

## Incremental Window State

So far our window state has been:

```
win_sum
```

But a window can preserve many kinds of information.

Suppose:

```
nums = [2, 7, 4, 8, 6, 3]
k = 3
```

The problem is:

> Find the size-3 window containing the most even numbers.

For example:

```
[2, 7, 4] → 2 evens
[7, 4, 8] → 2 evens
[4, 8, 6] → 3 evens   ← best
[8, 6, 3] → 2 evens
```

We could recount every window from scratch. But we shouldn't need to.

```
Window invariant:
even_count equals the number of even values in the current size-k window.

Best-answer invariant:
max_count equals the greatest even_count among all completed windows seen so far.

Transition:
value leaves
    ↓
was it contributing to even_count?
    ↓ yes
even_count -= 1

value enters
    ↓
does it contribute to even_count?
    ↓ yes
even_count += 1
```

## Variable-Size Windows

So far:

```
Fixed window:
size is predetermined

[left ........ right]
       size = k
```

Now imagine:

> Find the longest contiguous subarray whose sum is at most 10.

Assume all numbers are positive.

```
nums = [2, 3, 7, 1, 2, 4]
```

There is no fixed k.

The window might be:

```
[2]
[2, 3]
[2, 3, 7]   ← invalid: sum = 12
```

Something different must happen when the window becomes invalid.

Variable sliding window has two movements:

```
right expands the window
left shrinks the window
```

Think:

```
                right →
        EXPAND

        [ current window ]

             SHRINK
        ← left
```

`right` asks:

> Can I include more information?

`left` asks:

> If the window violates the requirement, what can I remove to restore validity?

Walkthrough:

```
Start:
[2]
sum = 2

Expand:
[2, 3]
sum = 5

Expand again:
[2, 3, 7]
sum = 12

But our requirement is:
sum <= 10

So the window is invalid.

Remove from the left:
[3, 7]
sum = 10

Valid again.

Now right can continue expanding:
[3, 7, 1]
sum = 11

Invalid again.

Shrink:
[7, 1]
sum = 8

Valid.
```

```
Information:
the longest contiguous subarray whose sum is at most 10

Window state:
win_sum = sum of current window

Best-answer state:
max_length = longest valid window length seen so far

Pointer meanings:
left = first index of current window
right = last index of current window

Validity condition:
win_sum <= 10

Expand transition:
win_sum += nums[right]

Shrink transition:
while win_sum > 10:
    win_sum -= nums[left]
    left += 1

Invariant:
after shrinking finishes, the current window [left, right] is valid: win_sum <= 10
```

Code Skeleton:

```
left = 0
win_sum = 0
max_length = 0

for right in range(len(nums)):
    win_sum += nums[right]

    while win_sum > 10:
        win_sum -= nums[left]
        left += 1

    max_length = max(max_length, right - left + 1)
```

## Expand / Shrink Reasoning

The central structure is:

```
expand right
    ↓
update window state
    ↓
window invalid?
    ↓ yes
shrink left until valid
    ↓
evaluate valid window
```

The important thing is that **expansion and shrinking have different jobs**.

`right` explores new possibilities.
`left` repairs the window when the constraint has been violated.

## Window Invariants

```
1. Boundary invariant
   [left, right] defines the current contiguous window

2. Window-state invariant
   win_sum equals exactly the sum of nums[left:right+1]

3. Answer invariant
   best stores the best valid candidate examined so far
```

## Frequency State Inside Windows

Consider:

> Find the longest substring containing at most 2 distinct characters.

Example:

```
s = "eceba"
```

```
Information:
longest substring containing at most 2 distinct characters

Window state:
freq = frequency of each character in the current window

Best-answer state:
max_len = longest valid window seen so far

Validity:
len(freq) <= 2

Entering:
increment frequency of s[right]

Leaving:
decrement frequency of s[left]
delete character when its frequency reaches 0

Window invariant:
freq represents exactly the character frequencies inside s[left:right+1]
```

## When Sliding Window Does NOT Work

Consider what happens if negatives are allowed:

```
nums = [8, 5, -6, 2]
target = 10
```

At:

```
[8, 5]
sum = 13
```

our previous reasoning would say:

Invalid. Move left because future expansion cannot make this valid.

But the next value is -6:

```
[8, 5, -6]
sum = 7
```

The same left boundary became valid again!

So the proof we used earlier has collapsed.

With positive values:

```
expand → sum cannot decrease
```

With negative values:

```
expand → sum might increase OR decrease
```

Therefore we can no longer safely conclude:

> “This left boundary can never work again.”

And that's the deeper requirement behind many variable sliding-window algorithms:

> Moving a boundary must change the relevant condition predictably enough that we can safely discard possibilities.
