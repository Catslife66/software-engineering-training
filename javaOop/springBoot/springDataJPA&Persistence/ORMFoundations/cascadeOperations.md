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
When I perform an operation on the parent entity,
should that operation automatically apply to the related child entities?
```

For:

```
Property → Viewings
```

the parent is Property, and the children are Viewing objects.

`CascadeType.PERSIST`

Use case:

```
Property property = new Property(...);
Viewing viewing = new Viewing(...);

property.addViewing(viewing);

propertyRepository.save(property);
```

With:

```
cascade = CascadeType.PERSIST
```

saving the new property also saves the new viewing.

```
persist Property
→ persist new Viewings
```

Without it, you may need to save the viewing separately.

`CascadeType.MERGE`

A detached parent and its detached children are merged back into the persistence context.

Conceptually:

```
merge Property
→ merge related Viewings
```

This matters more when using detached entities and EntityManager.merge().

In ordinary Spring Data JPA code, `save()` may internally use merge for existing detached entities.

`CascadeType.REMOVE`

Use case:

```
propertyRepository.delete(property);
```

With:

```
cascade = CascadeType.REMOVE
```

Hibernate also deletes all related viewings:

```
delete Property
→ delete all its Viewings
```

This makes sense when viewings cannot exist without a property.

`CascadeType.ALL`

This is shorthand for all cascade operations:

```
PERSIST
MERGE
REMOVE
REFRESH
DETACH
```

So:

```
cascade = CascadeType.ALL
```

means that all those parent operations cascade to children.

For a tightly owned relationship like:

```
Property → Viewings
```

ALL can be reasonable.

For:

```
Agent → Properties
```

`ALL` is dangerous because it includes `REMOVE`.

Deleting an agent could then delete all properties.

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

It handles this situation:

```
property.removeViewing(viewing);
```

The property still exists, but one viewing has been removed from its collection.

With:

```
orphanRemoval = true
```

Hibernate deletes that viewing from the database.

```
Property remains
Viewing removed from collection
→ Viewing row deleted
```

Without orphan removal, Hibernate may instead try to break the relationship by setting:

```
property_id = null
```

if the database mapping allows it.

Example:

```
order.getItems().remove(item);
```

If orphanRemoval = `true`, Hibernate treats that `OrderItem` as no longer belonging to the Order, so it deletes it from the database.

### Where should `orphanRemoval` go?

Only on the parent collection side:

```
@Entity
public class Property {

    @OneToMany(
        mappedBy = "property",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Viewing> viewings = new ArrayList<>();
}
```

Do not put it on the Viewing side.

The Viewing side is:

```
@ManyToOne
@JoinColumn(name = "property_id", nullable = false)
private Property property;
```

`orphanRemoval` belongs to collection-valued parent relationships such as `@OneToMany`.

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

`CascadeType.REMOVE` vs `orphanRemoval`

Cascade REMOVE:

```
Delete the Property
→ delete all its Viewings
```

orphanRemoval:

```
Keep the Property
but remove one Viewing from its collection
→ delete that Viewing
```

```
Delete Property itself
→ repository.delete(property)

Delete Property's children automatically
→ cascade REMOVE on Property.viewings

Delete one Viewing removed from collection
→ orphanRemoval on Property.viewings
```

## Important implementation habit

We should not expose the mutable collection directly and rely on callers to manage both sides.

Add helper methods inside `property`:

```
public void addViewing(Viewing viewing) {
    viewings.add(viewing);
    viewing.setProperty(this);
}

public void removeViewing(Viewing viewing) {
    viewings.remove(viewing);
    viewing.setProperty(null);
}
```

These methods keep both Java sides consistent:

```
Property.viewings
and
Viewing.property
```
