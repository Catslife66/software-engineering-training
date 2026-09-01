# Pointer State Design

**Purpose**

In Module 1, the processed prefix had one simple answer state:

```
best
count
result
exists
```

Now we move to problems where the array itself contains different regions of meaning.

That is where pointers become useful.

The key rule for this module is:

> A pointer is not the idea. A pointer marks a boundary between regions of state.

So we will not begin with:

“Which pointer moves?”

We will begin with:

“What regions exist, and what does each region mean?”

## Pattern 1 - Read / Write

```
read ---> scans information
write --> records the answer
```

Examples:

- Remove Element
- Remove Duplicates
- Move Zeroes
- Remove Duplicates II

Imagine an array containing some values we want to keep and some we want to remove.

Example:

```
[3, 2, 2, 3]
```

Problem:

```
Remove every 3 in place.
```

The final useful values should be:

```
[2, 2]
```

Ignore code for now.

Ask:

While scanning the array, what kinds of regions might naturally appear?

Suppose we have processed part of it:

```
[2, ? | 2, 3]
```

There are really three kinds of information:

```
| kept values | disposable processed space | unread values |
```

This is the foundation of Read / Write.

```
Information: What information must survive?
All non-3 values

Accepted Region: What should nums[0:write] contain?
`nums[0:write]` contains exactly the accepted values from the processed prefix, in their original order.

Disposable region:
`nums[write:read]` is disposable because every accepted value discovered so far has already been preserved in `nums[0:write]`

Unread region:
`nums[read:]` has not been processed yet.

Invariant:
Before each read, `nums[0:write]` contains exactly the accepted values from the processed prefix.

Transition:
accepted value:
    preserve it
    accepted region grows
    read moves
    write moves

rejected value:
    preserve nothing
    read moves
    write stays
```

Visually:

```
| accepted | disposable | unread |
0         write        read
```

---

**Why two pointers appear**

We need one boundary to answer:

```
Where is the next unread value?
```

That becomes:

```
read
```

And another boundary to answer:

```
Where should the next kept value be placed?
```

That becomes:

```
write
```

So their meanings are:

```
read = current input position being examined.

write = next position where an accepted value belongs.
```

Notice that these meanings come from the regions. We did not invent the pointers first.

### Drill - Remove Duplicates from Sorted Array

Given a sorted array, keep only one copy of each value.

Example:

```
[1, 1, 2, 2, 3]
```

Useful result:

```
[1, 2, 3]
```

```
Information: What values must survive?
All unique values must survive

Accepted region: What should nums[0:write] contain?
nums[0:write] contains all unique values in the processed prefix

Comparison: What should the current nums[read] be compared with to decide whether it is a duplicate?
nums[read] == nums[write - 1]

Transition: When should write move? When should only read move?
if nums[read] == nums[write - 1]:
    read moves
    write stays

otherwise:
    copy nums[read] to nums[write]
    read moves
    write moves

Invariant:
Before examining nums[read], nums[0:write] contains all unique values from the processed prefix, in original order.
```

### Drill - Move Zeros

Problem:

Move all zeroes to the end while keeping the relative order of non-zero values.

Example:

```
[0, 1, 0, 3, 12]
```

Useful front part becomes:

```
[1, 3, 12, ...]
```

```
Information: What values must survive, and in what order?
All non-zero values must survive while keeping their relative order

Accepted region: What should nums[0:write] contain?
nums[0:write] contains exactly all non-zero values from the processed prefix, in their original order.

Disposable region: Why can nums[write:read] be overwritten?
nums[write:read] is disposable because all the accepted values have been moved to nums[0:write]

Transition: What happens when nums[read] is non-zero? What happens when it is zero?
if nums[read] != 0:
    copy nums[read] to nums[write]
    read moves
    write moves
otherwise:
    read moves
    write stays

Invariant:
Before examining nums[read], nums[0:write] contains all non-zero values from the processed prefix, in their original order.
```

### Drill - Remove Duplicates II

Problem:

Given a sorted array, allow each value to appear at most twice.

Example:

```
[1, 1, 1, 2, 2, 3]
```

Useful front part should become:

```
[1, 1, 2, 2, 3]
```

```
Information: What values must survive?
each value that appears at most twice must survive

Accepted region: What should nums[0:write] contain?
nums[0:write] contains the processed prefix with each distinct value kept at most twice, in original order

Comparison: Which already-preserved value should nums[read] be compared with to decide whether keeping it would create a third duplicate?
nums[read] == nums[write - 2]

Transition: When should write move? When should only read move?
if nums[read] == nums[write - 2]:
    reject current
    read moves
    write stays

otherwise:
    preserve current
    read moves
    write moves

Invariant:
Before examining nums[read], nums[0:write] contains the processed prefix with each distinct value kept at most twice, in original order.
```

### Summary

Three Read/Write problems we've rebuilt:

```
Remove Element
accept if current != target

Remove Duplicates
accept if current != last accepted value

Move Zeroes
accept if current != 0
```

The pointer mechanics are almost identical. What changes is only:

**What condition makes the current value worth preserving?**

The common invariant is:

nums[0:write] contains exactly the values from the processed prefix that satisfy the preservation rule.

And the transition rule is:

```
accepted → copy and grow write;
rejected → only advance read.
```

## Pattern 2 - Start / Read

The key question becomes:

> Where did the current group start, and how far does it extend?

That is why the two positions have different meanings:

```
start = beginning of the current group
read  = current element being examined
```

We are **tracking a group or range**.

---

**Summary Ranges**

Problem:

[0, 1, 2, 4, 5, 7]

Return:

["0->2", "4->5", "7"]

The groups are:

```
0,1,2
4,5
7
```

The important question is:

How do we know whether the current range continues or ends?

Suppose we are here:

```
[0, 1, 2, 4, 5, 7]
 ↑        ↑
start    read
```

If the current value continues the sequence, start stays where it is and read moves.

If the sequence breaks, the range from start to the previous element is finished.

So the mental model is:

```
| completed ranges | current range being tracked | unread |
```

The current range is not yet finished because we do not know where it ends until we encounter a break.

```
Information: What information must we preserve while scanning?
We need to preserve the start of the current consecutive range and determine where that range ends.

Start: What does start mean?
start marks the first element of the current consecutive range.

Read: What does read mean?
the current element we are examining to see whether the range continues.

Invariant: Complete:
Before examining nums[read], the elements from start through read - 1 form one consecutive range.

Transition: What event tells us the current range has ended?
if nums[read] != nums[read - 1] + 1:
    current completed range = nums[start] ... nums[read - 1]
otherwise:
    the range continues
    start stays
    read moves
```

Visually:

```
| completed ranges | confirmed current range | candidate/unread |
                   start                read
```

**The important Start / Read pattern**

During the scan:

```
start
↓
[ current confirmed range ] [ candidate ]
                             ↑
                            read
```

If the candidate continues the group:

```
start stays
read moves
```

If it breaks the group:

```
finish start ... read - 1
start = read
read moves
```

And after the loop:

Finish the last group.

That **final cleanup step** is common in grouping problems because a group often finishes either when we see a boundary or when input ends.

### Drill - Merge Intervals

Problem:

```
[[1,3], [2,6], [8,10], [15,18]]
```

Output:

```
[[1,6], [8,10], [15,18]]
```

The mental model is similar to Summary Ranges:

> We are tracking one current group and deciding whether the next item belongs to it.

```
1. Information: What information must be preserved while scanning?
The merged interval for the current overlapping group.

2. What does current_start mean?
current_start is the start of the current merged interval.

3. What does current_end mean?
current_end is the furthest end reached by the current merged interval.

4. What exact condition tells us the next interval belongs to the current group?
if next_start <= current_end:
    same group
else:
    current group is finished

5. Transition:
Case 1 — Same group
next_start <= current_end
Then expand current group
current_end = max(current_end, next_end)

Case 2 — New group
next_start > current_end

Then finish current group
        ↓
append it

next interval becomes
the new current group

6. Invariant:
Before examining intervals[read], [current_start, current_end] represents the merged result of the current overlapping group from the processed prefix.

Current group = valid but still open to change.
Finished group = safe to append and never change again.
```

### Drill - Insert Interval

Problem:

```
intervals = [[1,2], [3,5], [6,7], [8,10], [12,16]]
newInterval = [4,8]
```

Expected result:

```
[[1,2], [3,10], [12,16]]
```

The important difference from Merge Intervals is this:

> We are not merging all intervals together. We are inserting one special interval into an already sorted, non-overlapping list.

So there are three phases.

```
1. Preserve intervals before the evolving interval
2. Merge overlapping intervals into the evolving interval
3. Preserve intervals after it
```

That means the evolving state is not a write pointer.

It is the interval we are currently building:

```
new_start
new_end
```

The invariant during the merge phase is:

`[new_start, new_end]` represents `newInterval` merged with every overlapping interval processed so far.

Now look at the three possible cases for the current interval [start, end].

If:

```
end < new_start
```

then the current interval is completely before the new interval. It is already finished, so we append it.

If:

```
start <= new_end
```

and we are no longer in the “before” case, then it overlaps the evolving new interval. So we expand:

```
new_start = min(new_start, start)
new_end = max(new_end, end)
```

If:

```
start > new_end
```

then the current interval is completely after the evolving interval. At that moment, the evolving interval is finished and can be appended.

The mental model is therefore:

```
| finished intervals | evolving inserted interval | unread intervals |
```

Trace:

```
[1,2]
2 < 4
→ before
→ append [1,2]

[3,5]
3 <= 8
→ overlaps
→ evolving interval becomes [3,8]

[6,7]
6 <= 8
→ overlaps
→ evolving interval stays [3,8]

[8,10]
8 <= 8
→ overlaps
→ evolving interval becomes [3,10]

[12,16]
12 > 10
→ after
→ append finished [3,10]
→ append [12,16]
```

Code skeleton:

```
res = []
read = 0
new_start, new_end = newInterval

# before
while (
    read < len(intervals)
    and intervals[read][1] < new_start
):
    res.append(intervals[read])
    read += 1

# overlap
while (
    read < len(intervals)
    and intervals[read][0] <= new_end
):
    new_start = min(new_start, intervals[read][0])
    new_end = max(new_end, intervals[read][1])
    read += 1

# evolving interval is now finished
res.append([new_start, new_end])

# after
while read < len(intervals):
    res.append(intervals[read])
    read += 1

return res
```

## Pattern 3 - Two Readers + One Writer

The classic problem here is:

**Merge Sorted Array**

Example:

```
nums1 = [1, 3, 5, 0, 0, 0]
nums2 = [2, 4, 6]
```

The final nums1 should become:

```
[1, 2, 3, 4, 5, 6]
```

This family is different from Read/Write.

In Read/Write, one reader scanned one source:

```
source → read
destination → write
```

Here we have two sources:

```
nums1
nums2
```

So we need to know:

Which source currently has the next value that belongs in the output?

That naturally gives us two readers.

---

**Why merge from the back?**

Suppose we tried to build from the front.

Initially:

```
nums1 = [1, 3, 5, _, _, _]
nums2 = [2, 4, 6]
```

If we write 2 into index 1 of nums1, we destroy the original 3 before we've processed it.

That's an information-preservation problem.

But the unused space is at the **back**:

```
[1, 3, 5, _, _, _]
                  ↑
               free space
```

So we work backwards.

We compare the **largest unread values** and place the larger one into the final free position.

---

**The three positions**

Suppose:

```
nums1 = [1, 3, 5, _, _, _]
                ↑        ↑
               p1      write

nums2 = [2, 4, 6]
             ↑
            p2
```

Their meanings:

- p1 = largest unread value remaining in the original part of nums1

- p2 = largest unread value remaining in nums2

- write = next output position to fill

Notice again: the pointers are just positions. The real idea is **what information each position represents**.

---

**Regions**

During execution, nums1 has roughly this shape:

```
| unread nums1 values | free/unsafe area | finished merged suffix |
                                      write+1              end
```

The finished suffix is important:

> Everything to the right of write is already in its final correct position and must never change again.

This is different from our earlier prefix invariants.

Now the finished answer grows from the right.

```
Information:
The remaining unread values from both arrays must be preserved until each one is copied into its final position.

State:
p1 = largest unread value remaining in the original part of nums1
p2 = largest unread value remaining in nums2
write = next output position to fill

Transition:
while p1 >= 0 and p2 >= 0:
    compare nums1[p1] and nums2[p2]
    write the larger one
    move that reader
    move write

if nums2 still has values:
    copy them into nums1

Invariant:
Before each comparison, nums1[write+1:] contains the largest elements from both arrays that have already been merged, in correct sorted order.
```

## Pattern 4 - Opposite-Direction Pointers

Previously, pointers mostly moved in the **same general direction** through data.

Now we’ll have:

```
left  →          ←  right
```

Two pointers start at opposite ends and move toward each other.

The key question is:

> What relationship between the left side and right side are we trying to maintain or test?

**Problem — Reverse an Array In Place**

Example:

```
[1, 2, 3, 4, 5]
```

Final result:

```
[5, 4, 3, 2, 1]
```

Imagine:

```
[1, 2, 3, 4, 5]
 ↑           ↑
left       right
```

What should happen to 1 and 5?

They belong in each other’s positions.

So we swap them:

```
[5, 2, 3, 4, 1]
    ↑     ↑
   left right
```

Now the outer positions are finished.

That gives us the important region picture:

```
| finished reversed | unknown middle | finished reversed |
```

The finished regions grow inward from both ends.

```
Information:
Each original value must end up in its mirrored position on the opposite side.

State:
left = the next position from the left that has not yet been placed correctly.
right = the next position from the right that has not yet been placed correctly.

Transition:
while left < right:
    nums[left] ↔ nums[right]

Invariant:
Before each swap, every position outside `[left, right]` already contains its correct final reversed value.
```

### Drill - Valid Palindrome

Example:

```
"racecar"
```

We want to know whether the string reads the same forwards and backwards.

```
Transition:
if s[left] != s[right]:
    return False

otherwise:
    left moves right
    right moves left

Invariant:
Before each comparison, every mirrored pair outside [left, right] has already been confirmed equal.
```

### Drill - Two Sum II (Sorted Array)

Example:

```
nums = [2, 7, 11, 15]
target = 9
```

We want the pair:

```
2 + 7 = 9
```

Because the array is sorted, opposite-direction pointers become useful:

```
[2, 7, 11, 15]
 ↑            ↑
left        right
```

The key difference from palindrome checking is this:

We do not always move both pointers.

Instead, the **sum tells us which side is wrong**.

If:

```
nums[left] + nums[right] < target
```

the sum is too small.

Because the array is sorted, moving right left would only make the sum smaller or equal. So that cannot help.

We must increase the smaller side:

```
left moves right
```

If:

```
nums[left] + nums[right] > target
```

the sum is too large.

Moving left right would make the sum even larger or equal. So we must decrease the larger side:

```
right moves left
```

And if:

```
nums[left] + nums[right] == target
```

we found the answer.

```
Information:
Find two values whose sum equals target.

State:
left points to the smallest remaining candidate.
right points to the largest remaining candidate.

Transition:
sum too small
→ prove left candidate impossible
→ eliminate left

sum too large
→ prove right candidate impossible
→ eliminate right

sum correct
→ answer found

Invariant:
If a valid pair still exists, it must lie within the remaining search region [left, right].
```

### Drill - Container With Most Water

Problem:

```
heights = [1,8,6,2,5,4,8,3,7]
```

Choose two vertical lines. The water area is:

```
width × shorter height
```

So for positions left and right:

```
area = (right - left) × min(height[left], height[right])
```

We start at the widest possible pair:

```
left                   right
 ↓                       ↓
[1, 8, 6, 2, 5, 4, 8, 3, 7]
```

The key question is not:

Which pointer should move?

It is:

> Which boundary can be safely discarded because keeping it cannot lead to a better area?

Suppose:

```
height[left] = 1
height[right] = 7
```

The area is limited by the shorter side:

```
min(1, 7) = 1
```

Now imagine keeping the left height 1 and moving right inward.

What happens?

The width gets smaller, while the limiting height is still at most `1`.

So any container that keeps this left boundary has area:

```
smaller width × at most 1
```

It cannot beat the current area.

Therefore the left boundary is proven useless and can be eliminated:

```
left += 1
```

That is the elimination argument.

Likewise, if:

```
height[right] < height[left]
```

then the right boundary is the limiting side, so we safely move:

```
right -= 1
```

The rule is therefore:

```
shorter side is left
→ eliminate left

shorter side is right
→ eliminate right
```

Why not move the taller side? Because reducing width while keeping the same shorter limiting side cannot improve the area.

The invariant/search-space view is:

`best` stores the largest area found so far, and any pair that could still beat it must lie inside the current `[left, right]` region.

Solution:

```
def maxArea(self, height: List[int]) -> int:
        left = 0
        right = len(height) - 1
        best = 0

        while left < right:
            area = (right - left) * min(height[left], height[right])
            best = max(area, best)

            if height[left] < height[right]:
                left += 1
            else:
                right -= 1

        return best
```

## Pattern 5 - Fast / Slow Pointers

We'll start with the classic problem:

Determine whether a linked list contains a cycle.

Imagine:

```
A → B → C → D → E
        ↑       ↓
        ← ← ← ←
```

After reaching E, instead of ending, the list points back to C.

So following next forever gives:

```
A → B → C → D → E → C → D → E → C ...
```

We need to determine:

> Does this list eventually end, or are we trapped in a loop?

One naive solution would be to remember every node we've visited in a `HashSet`.

But fast/slow pointers give us another way.

We create two observers:

```
slow → moves 1 node each iteration
fast → moves 2 nodes each iteration
```

For now, don't worry about why that detects a cycle.

First establish the state.

Suppose:

```
A → B → C → D → E
```

Initially:

```
slow
fast
 ↓
 A → B → C → D → E
```

After one iteration:

```
     slow
      ↓
 A → B → C → D → E
         ↑
        fast
```

So:

```
slow = B
fast = C
```

After another:

```
slow = C
fast = E
```

The pointers are not building an output region like Read/Write.

They are tracking two positions moving through the same structure at different rates.

If a linked list with **no cycle**, what will eventually happen to fast?

Eventually:

```
fast == null
      ↓
no cycle
```

**What if there is a cycle?**

Once slow and fast enter that cycle, neither can ever reach null.

But something else happens.

Think of it like two runners on a circular running track:

```
slow → 1 step each turn
fast → 2 steps each turn
```

Suppose fast is behind slow somewhere on the circle.

Every iteration:

```
slow advances: +1
fast advances: +2
```

So relative to slow, fast gains:

```
2 - 1 = 1 position
```

every iteration.

Eventually fast must catch slow.

That's the fundamental reason Floyd's cycle detection works.

### Drill - Cycly detection

Example:

```
A → B → C → D
↑           |
└───────────┘
```

Start:

```
slow = A
fast = A
```

We don't immediately say “cycle!” because they started at the same place.

Iteration 1:

```
slow: A → B
fast: A → B → C

slow = B
fast = C
```

Iteration 2:

```
slow: B → C
fast: C → D → A

slow = C
fast = A
```

Iteration 3:

```
slow: C → D
fast: A → B → C

slow = D
fast = C
```

Iteration 4:

```
slow: D → A
fast: C → D → A

slow = A
fast = A
```

They meet.

Therefore:

```
fast reaches null
        ↓
     no cycle

slow == fast after movement
        ↓
      cycle
```

**Different movement rates expose repetition in a cyclic structure.**

For linked-list cycle detection:

```
slow = head
fast = head

while fast != null and fast.next != null:
    slow = slow.next
    fast = fast.next.next

    if slow == fast:
        cycle exists

no cycle
```

Notice the safety condition:

```
fast != null
AND
fast.next != null
```

We need both because fast tries to move two steps.

If either step doesn't exist, we've reached the end of a normal linked list.

### Drill - Find the Middle of a Linked List

Example:

```
A → B → C → D → E → null
```

Again:

```
slow moves 1 step
fast moves 2 steps
```

Start:

```
slow = A
fast = A
```

Each iteration:

```
slow travels 1
fast travels 2
```

So their travelled distances always have this relationship:

```
fast_distance = 2 × slow_distance
```

When fast has travelled roughly the entire list:

```
fast_distance ≈ list length
```

therefore:

```
slow_distance ≈ list length / 2
```

So `slow` is around the middle.

This gives us a second use of Fast/Slow:

```
Cycle detection
→ compare positions
→ eventually meet if cyclic

Find middle
→ compare travel rates
→ slow has travelled half as far when fast finishes
```

**One subtle case: even-length lists**

Consider:

```
A → B → C → D → E → F → null
```

There are technically two middle nodes:

```
C and D
```

With the usual fast/slow implementation, slow ends at:

```
D
```

the **second middle**.

We don't need to dive deeply into that implementation detail yet, but it's useful to know.

```
Information:
middle node of the linked list.

State:
slow and fast

Invariant/relationship:
After each iteration, fast has travelled twice as many steps as slow.

Transition:
slow moves 1 step, fast moves 2.

Termination meaning:
when fast reaches the end, slow has travelled about half the list, so slow is at the middle.
```

### Drill - Happy Number

Given a number, repeatedly replace it with the sum of the squares of its digits.

Take:

```
19
```

Calculate:

```
1² + 9² = 82
8² + 2² = 68
6² + 8² = 100
1² + 0² + 0² = 1
```

We reached 1, so 19 is a happy number.

Another number might instead do this:

```
n → ... → 4 → ... → 4 → ... → 4
```

It enters a cycle and never reaches 1.

So there are two possible structures:

```
Happy:
19 → 82 → 68 → 100 → 1


Not happy:
n → A → B → C → D
        ↑         ↓
        ← ← ← ← ←
```

It's effectively a linked list with a cycle.

But there are no actual nodes or next pointers.

Instead, we have a function:

```
next_number(n)
```

that tells us where to go next.

For example:

```
19 → 82
```

means:

```
next_number(19) == 82
```

So conceptually:

```
Linked list:
node → node.next

Happy Number:
number → next_number(number)
```

**Why Fast/Slow works**

We can create:

```
slow = n
fast = n
```

Then:

```
slow moves once:
slow = next_number(slow)

fast moves twice:
fast = next_number(next_number(fast))
```

Same movement relationship as Floyd's cycle detection.

Eventually:

**Happy case**

The sequence reaches:

```
1 → 1 → 1 → 1 ...
```

So the pointers eventually meet at 1.

**Unhappy case**

The sequence enters some other cycle.

Fast eventually catches slow inside that cycle.

So when they meet, we ask:

```
Did they meet at 1?
```

If yes:

```
happy
```

Otherwise:

```
cycle that doesn't contain 1
→ not happy
```

### Summary

> Two observers follow the same transition function at different rates. Their positional relationship reveals information about the structure being traversed.

| Problem               | Transition       | What different rates reveal                                   |
| --------------------- | ---------------- | ------------------------------------------------------------- |
| Linked List Cycle     | `node.next`      | Whether traversal enters a cycle                              |
| Middle of Linked List | `node.next`      | Halfway position                                              |
| Happy Number          | `next_number(n)` | Whether repeated state transitions reach `1` or another cycle |

```
Fast / Slow
=
same transition
+
different movement rates
+
use their relationship to infer structure
```
