## 290 — Word Pattern (Easy)

Pattern Family: Hash Map / Bidirectional Mapping

What does my dictionary represent?
mapping characters with words

Why do I probably need two dictionaries?
To guarantee the mapping is one-to-one.

What invariant am I maintaining?
Every character always maps to the same word, and every word always maps to the same character.

## 219 — Contains Duplicate II (Easy)

Pattern Family: map lookup

What information is stored?
the num is seen and its index

Why is the index useful?
because we need to find if there are two distinct indices i and j in the array such that nums[i] == nums[j] and abs(i - j) <= k.

When do I update it?
if the num is in seen update its value to the latest index

## 451 — Sort Characters By Frequency (Medium)

Pattern Family: hash map

What is my dictionary key?
letter

What is my dictionary value?
frequency

After counting frequencies, what still needs to happen?
sort the key by their frenquency

I'm stuck at dont know how to reorder the map based on the character frequency.
Once it's reordered, i just need to concat char\*freq into a full string.

## 560 — Subarray Sum Equals K (Medium)

Pattern Family:

What state am I keeping while scanning?

Why isn't frequency counting enough?

What question am I asking the hash map?

## 525 — Contiguous Array (Medium)

Pattern Family:

What does my hash map store?

Why do I only store the FIRST occurrence?

## 599 — Minimum Index Sum of Two Lists (Easy)

Pattern Family: hashmap

What information belongs in the dictionary?
word maps to its index

Why only one list goes into the dictionary?
can access to list2 index of each word by literating a for loop

## 594 — Longest Harmonious Subsequence

You’ll use frequencies again.

nums = [1,3,2,2,5,2,3,7]

{
1: 1,
3: 2,
2: 3,
5: 1,
7: 1
}

{
1: 1,
2: 1,
3: 1,
4: 1
}

Before coding:

1. What is the dictionary key?

2. What is the dictionary value?
3. After building the dictionary…
   what information are you searching for?

## 359 — Logger Rate Limiter

This is a wonderful hash map design exercise.

Don’t worry if you can’t submit it (it’s on a design platform rather than the main LeetCode list).

Think about:

What should the dictionary store?
