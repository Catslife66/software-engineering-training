# JPA Annotation

Example:

```
Bob
 ├── Property A
 ├── Property B
 └── Property C

```

## Build the Domain First

Let's start with Property.

```
@Entity
@Table(name = "viewings")
public class Viewing {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "request_date_time", nullable = false)
    private LocalDateTime requestedDateTime;

    @Column(nullable = false)
    private String status;
}

@Entity
@Table(name = "properties")
public class Property {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "property")
    private List<Viewing> viewings;
}
```

`@ManyToOne` Relationship Annotation

tells Hibernate:

```
Many Properties
        ↓
      One User

owner is another entity
Many properties can reference the same user
```

`@JoinColumn(name = "owner_id")` Foreign Key Column

tells Hibernate:

```
Property.owner
        ↓
properties.owner_id
        ↓
users.id
```

Explicitly says use `owner_id` as the foreign key column.

**Property**

```
@ManyToOne
@JoinColumn(name = "owner_id")
private User owner;
```

Hibernate knows exactly where the relationship is stored:

```
properties.owner_id
```

because the foreign key lives in the properties table.

**User**

```
@OneToMany
private List<Property> properties;
```

Question:

```
Where is the foreign key?
```

Not in users table.

It is still in:

```
properties.owner_id
```

table.

So Hibernate needs to know:

```
This relationship is already managed by Property.owner
```

This leads to:

```
@OneToMany(mappedBy = "owner")
private List<Property> properties;
```

`mappedBy` means:

```
Look at Property.owner
That side owns the relationship.
```

**Owning Side**

```
@ManyToOne
@JoinColumn(name = "owner_id")
private User owner;
```

This side controls:

```
owner_id
```

in the database.

**Inverse Side**

```
@OneToMany(mappedBy = "owner")
private List<Property> properties;
```

This side is just:

```
navigation
```

for Java code.

## Unidirectional Relationship

Only:

```
Property
    ↓
User
```

exists.

```
@Entity
class Property {

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
```

No property list inside User.

This is perfectly valid.

## Bidirectional Relationship

Both sides exist.

```
Property
    ↓
User
```

and

```
User
    ↓
Properties
```

```
@Entity
class User {

    @OneToMany(mappedBy = "owner")
    private List<Property> properties;
}
```

Now navigation works both ways.

## Engineering Tradeoff

Many beginners think:

```
Always create both sides.
```

Experienced developers often ask:

```
Do I actually need both directions?
```

Because every relationship adds:

```
complexity
loading concerns
DTO concerns
serialization concerns
```

**Example**

Suppose your use cases are:

```
Get Property Details
Update Property
Check Ownership
```

You might only need:

```
Property.owner
```

and never need:

```
User.properties
```

In that case:

```
Unidirectional relationship
```

is simpler.

But suppose you have:

```
Agent Dashboard
```

and need:

```
Show all properties owned by Bob
```

Then:

```
bob.getProperties()
```

becomes useful.

Now the bidirectional relationship may make sense.
