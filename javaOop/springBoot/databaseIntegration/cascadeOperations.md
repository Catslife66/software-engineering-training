# Cascade Operations

Imagine:

```
Property property = new Property();

Viewing viewing1 = new Viewing();
Viewing viewing2 = new Viewing();

property.getViewings().add(viewing1);
property.getViewings().add(viewing2);
```

Question:

If we call:

```
propertyRepository.save(property);
```

Should Hibernate automatically save:

```
viewing1
viewing2
```

as well?

Or should it save only:

```
property
```

and require separate saves for the viewings?

For this case, saving a Property should NOT automatically save Viewings.

Because creating a viewing has its own business rules:

```
check user exists
check property exists
check slot availability
prevent double booking
maybe create outbox notification
maybe use transaction/idempotency
```

So this should usually go through:

```
ViewingService
```

not accidentally happen through:

```
propertyRepository.save(property)
```

## What Cascade Means

Cascade means:

```
When I perform an operation on parent,
also perform it on related children.
```

Example:

```
@OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
private List<Viewing> viewings;
```

This would mean:

```
save property
→ also save viewings

delete property
→ also delete viewings
```

That can be dangerous if you don’t intend it.

## Better default mindset

Do not use cascade unless the child truly belongs to the parent lifecycle.

For example:

### Good cascade example

```
Order
→ OrderItem
```

An order item usually cannot exist without its order.

So cascade can make sense.

### Risky cascade example

```
Property
→ Viewing
```

A viewing has its own workflow and business rules.

So be careful.

### Rule to remember

```
Cascade is about lifecycle ownership.
```

Not just relationship existence.

A relationship means:

```
these objects are connected
```

Cascade means:

```
their save/delete lifecycle is connected
```

Those are different.

## Orphan Removal

Cascade answers:

```
If I save/delete the parent, should related children also be saved/deleted?
```

Orphan removal answers:

```
If I remove a child from the parent’s collection, should that child be deleted from the database?
```

Example:

```
order.getItems().remove(item);
```

If orphanRemoval = `true`, Hibernate treats that `OrderItem` as no longer belonging to the Order, so it deletes it from the database.

### Good use case

```
Order → OrderItem
```

Because an `OrderItem` usually has no meaning without its `Order`.

```
@OneToMany(
    mappedBy = "order",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<OrderItem> items;
```

### Risky use case

```
User → Property
```

If you remove a property from `user.getProperties()`, should the property be deleted from the database?

Usually no.

The property may need to be reassigned, archived, or reviewed.

### Key rule

```
Cascade = parent operation affects child.
orphanRemoval = delete child when parent no longer references it
```

Use it only when the child fully depends on the parent’s lifecycle.
