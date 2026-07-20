# Code Review

ENGINEER CHECKLIST

Whenever you see code, don't immediately solve it.

Ask:

1. What state exists?

```
Mutable?

Immutable?

Local?

Shared?
```

2. Who can access it?

```
One object?

Many objects?

One thread?

Many threads?
```

3. What operations happen?

```
Read?

Write?

Read-modify-write?
```

4. What could go wrong?

```
Race condition?

Lost update?

Inconsistent state?
```

## Drill 1

Look at this block of code.

```
public class Counter {

    private int value = 0;

    public void increment() {
        value++;
    }

    public int getValue() {
        return value;
    }
}
```

Flowchart:

```
Mutable?
↓
Yes.

Shared?
↓
Don't know yet.

Who modifies it?
↓
increment()

Who reads it?
↓
getValue()

Can multiple threads call these methods?
↓
Don't know yet.

If yes...
↓
Potential race condition.
```

```
I notice that value is a mutable instance field. Although it is private, that only provides encapsulation—it does not guarantee thread safety.

From this class alone, I don't yet know whether the object will be shared between multiple threads. However, if multiple threads can call increment() concurrently, they will update the same mutable state.

Since those updates are not coordinated, a race condition may occur, leading to lost updates and leaving the counter in an inconsistent state.

```

Pattern:

```
Method-local mutable state
↓
Belongs to one method invocation
↓
Not shared
↓
Usually no synchronization required
```

## Drill 2

```
public class UserService {

    private String applicationName = "ShopEasy";

    public String getApplicationName() {
        return applicationName;
    }
}
```

1. What state exists?
2. Is it shared?
3. Is it mutable?
4. Do you have any immediate thread-safety concerns?

```
The field is shared because all method calls on the same object can access it. However, based on the current implementation, I don't see any code that modifies the field after construction. In addition, String objects are immutable in Java, so reading this value concurrently does not immediately raise thread-safety concerns.
```

Checklist:

When you see a field, don't ask:

Is it private?

Instead ask:

```
Who can modify it?
↓
Does anyone modify it?
↓
Can two threads modify it at the same time?
```

Pattern:

```
Shared
+
Read Only
↓
Usually no synchronization required
```

## Drill 3

```
public class UserService {

    private final List<String> users = new ArrayList<>();

    public List<String> getUsers() {
        return users;
    }
}
```

1. What state exists?

Shared mutable state.

`final` does NOT make an object immutable.

It only makes the reference immutable.

2. Is it shared?

It is an instance field, so every method executing on the same object accesses the same List.

3. Is it mutable?

It is mutable.

4. Does anything immediately catch your attention?
   Who modifies it.

Pattern:

```
Shared

Mutable

↓

Interesting...
Who modifies it?
```
