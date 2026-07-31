# Engineer Pattern Recognition

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

## Drill 1 - Shared counter

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

## Drill 4 — Database as source of truth

```
@Service
public class StatisticsService {

    private final UserRepository repository;

    public long getTotalUsers() {
        return repository.count();
    }
}
```

Questions:

1. What state exists inside StatisticsService?
2. Is there any mutable counter stored in this object?
3. Where does the count now come from?
4. Does this remove the lost-update problem from the previous implementation?
5. What new engineering question would you ask about this design?

```
1. The service contains a final reference to a shared repository dependency, but no mutable user-count state.

2. No

3. It is retrieved from the database through repository.count(). The database is now being treated as the source of truth.

4. Yes. The service no longer maintains a separate in-memory counter through concurrent read-modify-write operations.

5. How frequently is this method called, and is querying the database each time acceptable for performance? What exactly counts as a user—every database row, only active users, or only successfully registered users?
```

## Drill 5 — Cached count

```
@Service
public class StatisticsService {

    private final UserRepository repository;
    private long cachedTotalUsers;

    public StatisticsService(UserRepository repository) {
        this.repository = repository;
        this.cachedTotalUsers = repository.count();
    }

    public long getTotalUsers() {
        return cachedTotalUsers;
    }
}
```

Questions:

1. What state exists?
2. Which state is shared and mutable?
3. What performance problem is this design trying to avoid?
4. What correctness problem might now appear when a new user is registered?

```
1 & 2:
The service contains:
a shared dependency reference: repository
shared mutable state: cachedTotalUsers

3 & 4:
cachedTotalUsers avoids repeated database count queries, but the cached value is initialized only once. Unless it is updated or refreshed when user data changes, it can become stale and stop matching the database, which is the source of truth.
```

## Drill 6 — Updating the cache manually

```
@Service
public class StatisticsService {

    private final UserRepository repository;
    private long cachedTotalUsers;

    public StatisticsService(UserRepository repository) {
        this.repository = repository;
        this.cachedTotalUsers = repository.count();
    }

    public void userRegistered() {
        cachedTotalUsers++;
    }

    public long getTotalUsers() {
        return cachedTotalUsers;
    }
}
```

Questions:

1. What stale-data problem is the teammate trying to fix?
2. Does userRegistered() introduce a familiar concurrency risk?
3. What event could make the cache disagree with the database even without a race condition?
4. What business invariant should remain true?

```
1. userRegistered() updates the cached count whenever a user is registered, so the cache does not remain permanently at its startup value.

2. cachedTotalUsers++ is a read-modify-write operation on shared mutable state. If two request threads execute it concurrently, a lost update may occur.

3.
Suppose the database save fails, but userRegistered() is still called because of incorrect error handling:

Database count = 100
Cached count   = 101

The cache records a registration that never succeeded.

The opposite can also happen: the database successfully saves the user, but the application crashes before calling userRegistered():

Database count = 101
Cached count   = 100

Other possible causes include:

another code path creates a user but forgets to call userRegistered();
a user is deleted without decrementing the cache;
another application instance updates the same database;
the service restarts while transactions are occurring.

Notice that none of these requires two threads to race. They are coordination and consistency problems between two pieces of state.

4. cachedTotalUsers must equal the number of successfully persisted users represented by the database.
```

## Drill 7 — Two application servers

```
Server A                      Server B
cachedTotalUsers = 100        cachedTotalUsers = 100
```

A registration request reaches Server A:

```
repository.save(user);
cachedTotalUsers++;
```

Questions:

1. What value does Server A now return?
2. What value does Server B return?
3. Has any thread race occurred?
4. What new systems problem has appeared?

```
1. Server A returns 101.
2. Server B returns 100.
3. No thread race occurred because each server modifies its own separate memory.
4. The inconsistency appears because both servers maintain independent copies of the same cached fact.

Each application instance maintains its own in-memory cache. When Server A updates its cached count, Server B is not automatically informed, so the two instances can return different values even though they use the same database. This is a distributed cache consistency problem, not an in-process race condition.
```

## Drill 8 — Message after registration

Server A sends this message to Server B:

```
Increment your cached user count.
```

Questions:

1. What happens if Server A updates the database but the message to Server B is lost?
2. What happens if Server B receives the same message twice?
3. What happens if the message arrives before the database transaction commits?
4. What new engineering concerns are appearing?

```
1. Message is lost. Server B misses the update and becomes stale. This is a message delivery failure.
2. Message is delivered twice. Server B processes the same registration twice.
3. For example:

Server B receives message → cache becomes 101
Database still shows 100

But there is an even more serious possibility.

Suppose the transaction later fails and rolls back:

Database remains 100
Server B cache remains 101

Now the inconsistency is permanent until something repairs it.

This is an ordering and transaction-boundary problem.

4. How do separate systems remain consistent when communication is unreliable and each operation can succeed or fail independently?
```

## Drill 9 — Event ID and deduplication

```
eventId: event-abc123
type: UserRegistered
userId: 8472
```

Server B records processed event IDs:

```
event-abc123 → processed
```

Questions:

1. What duplicate-message problem is this trying to solve?
2. Why is an event ID important?
3. Could the message still be lost?
4. Would this guarantee that every cache always matches the database immediately?

```
1.
The problem is that the same message may be delivered twice:

event-abc123: User 8472 was registered
event-abc123: User 8472 was registered

Without deduplication, Server B might process both messages:

cachedTotalUsers = 100
process first message  → 101
process duplicate      → 102

The event ID lets Server B recognise:

“I have already processed this event, so I must not apply its effect again.”

That gives us idempotent event handling.

2.
The logic might look conceptually like this:

Receive event-abc123

Has event-abc123 already been processed?

├── Yes → ignore it
└── No  → apply the update and record the event ID

The important business invariant becomes:

Each registration event must affect the cache at most once.

Notice the phrase at most once.

It means:

zero times is still possible if the message is lost;
more than once should be prevented.

3.
An event ID helps with duplicates.

Retries help with temporary delivery failures.

Acknowledgements help determine whether processing succeeded.

Reconciliation helps repair disagreements later.

Each mechanism addresses a different risk.

4.
Now the cache is ahead of the database.

But even if the database save succeeds first, immediate consistency is still not guaranteed.

For example:

12:00:00 Database commits user 8472
12:00:01 Event enters message queue
12:00:04 Server B processes event

For several seconds:

Database = 101
Server B cache = 100

Eventually, Server B catches up.

This is eventual consistency:

Different parts of the system may temporarily disagree, but they are expected to converge later.
```

## Drill 10 - Database commits, application crashes

```
1. Save the user in the database.
2. Commit the database transaction.
3. Publish UserRegistered event.
```

Then:

```
Database commits successfully.
Application crashes before publishing the event.
```

Answer:

1. Does the user exist in the database?
2. Will other servers know to update their caches?
3. Is this a database failure, messaging failure, or partial failure?
4. What fact is now inconsistent across the system?

```
1. Yes, the transaction committed successfully.

2. No, the application crashed before this happened - Publish UserRegistered, so nothing tells the other servers - “A new user exists.”

3. It is a partial failure. Because part of the operation succeeded. One component succeeded. One component failed.

Database
✓ Success

Publish Event
✗ Failed

4. The caches in different server does not match with the data in database.
```

## Summary

Stage 1

```
totalUsers++;
```

Problem?

```
Lost update
```

Stage 2

Move the count into the database.

Problem?

```
More database reads
```

Stage 3

Add a cache.

Problem?

```
Stale cache
```

Stage 4

Update the cache.

Problem?

```
Lost update returns
```

Stage 5

Multiple servers.

Problem?

```
Separate memory
```

Stage 6

Send messages.

Problem?

```
Lost messages
Duplicate messages
Delayed messages
```

Stage 7

Use event IDs.

Problem?

```
Duplicates solved
↓
Lost messages still possible
```

Stage 8

Commit database first.

Problem?

```
Partial failure
```
