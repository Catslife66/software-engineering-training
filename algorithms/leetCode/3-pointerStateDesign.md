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

## Read / Write

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

Our first invariant
