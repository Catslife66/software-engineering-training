## 290 — Word Pattern (Easy)

Pattern Family: map lookup

What does my dictionary represent?
mapping characters with words

Why do I probably need two dictionaries?
To compare if 2 mappings match

What invariant am I maintaining?
char_p = {}
word_s = {}

## 219 — Contains Duplicate II (Easy)

Pattern Family: map lookup

What information is stored?
the num is seen and its index

Why is the index useful?
because we need to find if there are two distinct indices i and j in the array such that nums[i] == nums[j] and abs(i - j) <= k.

When do I update it?
if the num is in seen update its value to the latest index

## 451 — Sort Characters By Frequency (Medium)

Pattern Family: map lookup

What is my dictionary key?
letter

What is my dictionary value?
frequency

After counting frequencies, what still needs to happen?
sort the key by their frenquency

I'm stuck at dont know how to reorder the map based on the character frequency.
Once it's reordered, i just need to concat char\*freq into a full string.
