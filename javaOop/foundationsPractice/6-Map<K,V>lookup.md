# Session 6 - `Map<K, V>` lookup

Last session you grouped objects:

```
Map<String, List<Product>>
```

Today we'll look at another common use of a HashMap: fast lookup by identity.

Suppose we have:

```
class User {
    private int id;
    private String name;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

And:

```
List<User> users = List.of(
    new User(101, "Amy"),
    new User(102, "Ben"),
    new User(103, "Cara")
);
```

Imagine another part of the application repeatedly asks:

Give me the user whose ID is `102`.

With only the list, we could search:

```
for (User user : users) {
    if (user.getId() == 102) {
        return user;
    }
}
```

That is a linear search:

```
101 → not it
102 → found
```

Worst case: O(n).

If we're going to perform many lookups, we can instead build:

```
Map<Integer, User> usersById = new HashMap<>();
```

with the relationship:

```
101 → Amy
102 → Ben
103 → Cara
```

Then:

```
User user = usersById.get(102);
```

is average O(1) lookup.

## Building the lookup map

```
Map<Integer, User> usersById = new HashMap<>();

for (User user : users) {
    usersById.put(user.getId(), user);
}
```

The state means:

`usersById` stores every processed user indexed by their ID.

Our invariant is:

After processing each user, every user in the processed prefix can be retrieved from usersById using that user's ID.

That's slightly different from last session.

Previously:

```
category → MANY products

Map<String, List<Product>>
```

Today:

```
user ID → ONE user

Map<Integer, User>
```

So again, the **relationship in the requirement determines the map's value type**.

## OOP connection: identity

Why use:

```
Map<Integer, User>
```

rather than:

```
Map<String, User>
```

using the user's name?

Because names aren't necessarily unique:

```
101 → Amy Smith
247 → Amy Smith
```

An ID is normally intended to identify one entity uniquely.

This appears constantly in real applications:

```
database primary key
        ↓
user.id
        ↓
application lookup
        ↓
Map<Integer, User>
```

So this small collection exercise connects directly to entity modelling and databases.

## Exercise

Suppose:

```
class Product {
    private int id;
    private String name;

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
}
```

Given:

```
List<Product> products = List.of(
    new Product(201, "Keyboard"),
    new Product(202, "Mouse"),
    new Product(203, "Monitor")
);
```

Requirement:

> Build a lookup map so that a product can later be retrieved efficiently using its ID.

```
Map<Integer, Product> productsById = new HashMap<>();

for(Product product : products){
    productsById.put(product.getId(), product);
}

return productsById;

Data structure:
HashMap

State:
productsById stores the every processed product indexed by its ID.

Invariant:
After processing each product, every product in the processed prefix can be retrieved from productsById by its ID.
```
