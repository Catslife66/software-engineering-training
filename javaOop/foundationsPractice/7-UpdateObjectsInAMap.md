# Session 7 - Updating objects in a Map — inventory state

You've practised using a Map to build an index:

```
product ID → Product
```

Today we'll use that index to change application state. This introduces an important distinction:

```
finding an object
vs
changing the object after finding it
```

Suppose an inventory system has:

```
class Product {
    private int id;
    private String name;
    private int stock;

    public Product(int id, String name, int stock) {
        this.id = id;
        this.name = name;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public int getStock() {
        return stock;
    }

    public void reduceStock(int quantity) {
        stock -= quantity;
    }
}
```

And we maintain:

```
Map<Integer, Product> productsById = new HashMap<>();
```

Conceptually:

```
201 → Product("Keyboard", stock=10)
202 → Product("Mouse", stock=20)
203 → Product("Monitor", stock=5)
```

A customer wants to buy:

```
productId = 203
quantity  = 2
```

## 1. Lookup first

Because the map is indexed by product ID:

Product product = productsById.get(203);

Now:

```
product
   │
   ▼
Product("Monitor", stock=5)
```

The local variable and the map both ultimately refer to the same Product object.

Therefore:

```
product.reduceStock(2);
```

changes that object to:

```
Product("Monitor", stock=3)
```

If we later do:

```
productsById.get(203).getStock();
```

we get:

```
3
```

We don't need to `put()` the product back into the map. We mutated the existing object.

## 2. But what if the ID doesn't exist?

This is dangerous:

```
Product product = productsById.get(999);

product.reduceStock(2);
```

get(999) returns:

```
null
```

so calling:

```
product.reduceStock(2);
```

causes a NullPointerException.

Therefore our algorithm needs another piece of reasoning:

```
lookup
  ↓
did we actually find the object?
  ↓
yes → continue
no  → handle missing product
```

For example:

```
Product product = productsById.get(productId);

if (product == null) {
    return false;
}

product.reduceStock(quantity);
return true;
```

Now the boolean communicates:

```
true  → stock was updated
false → product wasn't found
```

## 3. There's still a business invariant

Consider:

```
Monitor stock = 5
customer requests = 8
```

Our current method would produce:

```
stock = -3
```

Java has no problem with that.

But the business domain probably does.

We may have an invariant:

```
Product stock must never be negative.
```

So before changing state:

```
if (product.getStock() < quantity) {
    return false;
}

product.reduceStock(quantity);
```

Notice the engineering pattern:

```
find entity
    ↓
validate operation
    ↓
change state
```

This pattern appears everywhere in backend development.

For example:

```
find bank account
→ check sufficient balance
→ withdraw

find order
→ check cancellable status
→ cancel

find seat
→ check availability
→ reserve
```

## Exercise

Implement:

```
public static boolean purchaseProduct(
        Map<Integer, Product> productsById,
        int productId,
        int quantity) {

    Product product = productsById.get(productId);

    if(product == null){
        return false;
    }
    if(product.getStock() < quantity){
        return false;
    }
    product.reduceStock(quantity);
    return true;
}
```
