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
