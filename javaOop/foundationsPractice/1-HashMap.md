# Session 1 - `HashMap` and choosing the right data structure

Today we'll connect **Java collections**, **algorithmic thinking**, and **engineer communication**.

Suppose we receive a list of programming languages:

```
List<String> languages = List.of(
    "Java",
    "Python",
    "Java",
    "JavaScript",
    "Python",
    "Java"
);
```

Our requirement is:

> Count how many times each language appears.

## 1. Choose the state

We need to remember:

```
Java       -> 3
Python     -> 2
JavaScript -> 1
```

A HashMap is a natural fit because it stores key-value pairs:

```
Map<String, Integer> counts = new HashMap<>();
```

Here:

```
key   = language
value = number of occurrences seen so far
```

That gives us an important invariant:

> After processing an element, counts contains the number of occurrences of each language seen so far.

## 2. Build the algorithm

We process each language:

```
for (String language : languages) {

    if (counts.containsKey(language)) {
        counts.put(language, counts.get(language) + 1);
    } else {
        counts.put(language, 1);
    }
}
```

Let's trace the first few steps.

## 3. A more idiomatic Java version

Once you understand the algorithm above, Java lets us write:

```
for (String language : languages) {
    counts.put(
        language,
        counts.getOrDefault(language, 0) + 1
    );
}
```

getOrDefault(language, 0) means:

Give me the existing count, or 0 if this language hasn't appeared yet.

So the state transition becomes simply:

new count = previous count + 1

That's the important idea. The shorter syntax comes afterwards.

## 4. Engineer language

A weak explanation would be:

> "I use a HashMap and loop through the list."

That's correct, but it doesn't explain why.

A stronger explanation is:

> "I use a HashMap to maintain a frequency count for each language. As I iterate through the input, each language is used as the key and its occurrence count as the value. When a language is encountered, I increment its existing count."

Even better, you can explain the data-structure decision:

> "A HashMap is appropriate because I need to associate each unique language with its frequency and efficiently retrieve and update that frequency."

That is the kind of explanation worth developing for interviews.

## Exercise

Now suppose we have:

```
List<Integer> numbers = List.of(
    4, 7, 4, 2, 7, 4, 9, 2
);
```

Your job is to build this result:

```
4 -> 3
7 -> 2
2 -> 2
9 -> 1
```

```
HashMap<Integer, Integer> counts = new HashMap<>();

for(Integer number : numbers){
    int currentCount = counts.getOrDefault(number, 0);
    counts.put(
        number,
        currentCount + 1
    );
}
```

```
Data structure:
HashMap

State:
counts stores the frequency of each integer seen so far.

Invariant:
After processing each element, counts contains the correct frequency for every integer in the processed prefix.
```
