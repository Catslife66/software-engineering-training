# Entity Relationships

So far, we have treated entities individually:

```
Agent
Property
Viewing
```

Real applications rarely consist of isolated entities.

BrightMove has relationships:

```
Agent
  │
  │ owns/manages
  ▼
Property
  │
  │ has
  ▼
Viewing
```

For example:

- one agent can manage many properties
- each property belongs to one agent
- one property can have many viewings
- each viewing belongs to one property

In the relational database, these relationships are represented primarily through foreign keys.

In Java, they are represented through object references and collections.

JPA must map between those two worlds.

This module covers:

- @ManyToOne
- @OneToMany
- foreign keys
- @JoinColumn
- owning side
- mappedBy
- bidirectional relationships
- helper methods
- cascade
- orphanRemoval
- relationship lifecycle design

## Start With the Database

Before thinking about annotations, establish the relational model.

Suppose:

```
One Agent → many Properties
```

The database should normally place the foreign key on the many side:

```
agents
----------------
id
name
email


properties
----------------
id
title
price
agent_id   ← foreign key
```

Example:

```
agents

id   name
----------------
A1   Sarah


properties

id   title             agent_id
--------------------------------
P1   City Centre Flat  A1
P2   Riverside House   A1
P3   Old Town Flat     A1
```

Sarah appears once in agents.

Her ID appears in several property rows.

Therefore:

```
Agent 1
   │
   ├── Property 1
   ├── Property 2
   └── Property 3
```

This database structure should drive our JPA mapping.

## @ManyToOne

Look from the Property perspective:

Many properties can belong to one agent.

Therefore:

```
@ManyToOne
private Agent agent;
```

A more complete mapping:

```
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(
    name = "agent_id",
    nullable = false
)
private Agent agent;
```

This maps:

```
Java

property.agent
      │
      ▼
Agent object
```

to:

```
Database

properties.agent_id
      │
      ▼
agents.id
```

## @JoinColumn

`@JoinColumn` tells JPA which database column stores the foreign key.

```
@JoinColumn(name = "agent_id")
```

means:

```
properties table
      ↓
agent_id
      ↓
references Agent
```

The conceptual SQL schema is:

```
CREATE TABLE properties (
    id UUID PRIMARY KEY,
    title VARCHAR(150),
    agent_id UUID NOT NULL,

    FOREIGN KEY (agent_id)
        REFERENCES agents(id)
);
```

So:

```
private Agent agent;
```

and:

```
agent_id
```

represent the same relationship at different abstraction levels:

```
Java world              Relational world

Agent object       ↔     foreign key
```

This is a fundamental ORM idea.

## @OneToMany

Now look from the Agent perspective:

One agent can have many properties.

We could model:

```
@OneToMany
private List<Property> properties;
```

But for a bidirectional relationship, we normally need to tell JPA that this collection represents the **same relationship already mapped by** Property.agent.

Therefore:

```
@OneToMany(mappedBy = "agent")
private List<Property> properties = new ArrayList<>();
```

Now:

```
Property
@ManyToOne
private Agent agent;
```

and:

```
Agent
@OneToMany(mappedBy = "agent")
private List<Property> properties;
```

describe two Java directions of the same database relationship.

## Owning Side

This is one of the most important JPA relationship concepts.

In our database, which entity actually contains the foreign key?

```
properties.agent_id
```

Therefore:

```
Property
```

is the **owning side** of this relationship.

In JPA:

```
@ManyToOne
@JoinColumn(name = "agent_id")
private Agent agent;
```

is the owning side.

Why?

Because changing:

```
property.agent
```

determines the value of:

```
properties.agent_id
```

The owning side is essentially:

The side whose mapping controls the relationship in the database.

## Inverse Side and `mappedBy`

The other side:

```
@OneToMany(mappedBy = "agent")
private List<Property> properties;
```

is the inverse side.

`mappedBy = "agent"` means:

This collection does not define another relationship. The relationship is already owned by the agent field inside Property.

Be very precise here:

```
mappedBy = "agent"
```

refers to the Java field name:

```
private Agent agent;
```

It does not refer to:

```
agent_id
```

So:

```
Property.java

private Agent agent;
              ↑
              │
mappedBy = "agent"
```

while:

```
@JoinColumn(name = "agent_id")
```

refers to the database column.

This distinction is worth putting in your notes:

```
mappedBy
→ Java property/field name

@JoinColumn
→ database foreign-key column
```

## Why mappedBy Matters

Imagine both sides independently declared relationships without connecting them properly.

Hibernate could interpret them as two separate relationships.

We want:

```
ONE relationship
```

represented in Java from two directions:

```
Agent → Properties

Property → Agent
```

`mappedBy` tells Hibernate:

These aren't two independent relationships.

Conceptually:

```
Agent.properties
       │
       │ mappedBy = "agent"
       ▼
Property.agent
       │
       │ @JoinColumn
       ▼
properties.agent_id
```

## Bidirectional vs Unidirectional

Relationships don't have to be navigable from both directions.

**Unidirectional**

We could have only:

```
class Property {

    @ManyToOne
    private Agent agent;
}
```

Then:

```
property.getAgent();
```

works.

But:

```
agent.getProperties();
```

doesn't exist.

This may be perfectly sufficient.

**Bidirectional**

If the application genuinely needs navigation both ways:

```
property.getAgent();
```

and:

```
agent.getProperties();
```

we can model both sides.

```
Agent
 ↕
Property
```

But bidirectional relationships introduce additional responsibility:

Both sides of the Java object graph must remain consistent.

That brings us to helper methods.

## Bidirectional Consistency

Suppose we do only this:

```
agent.getProperties().add(property);
```

Java now sees:

```
Agent
properties = [property]
```

But what does the property contain?

Potentially:

```
property.agent = null
```

Our Java object graph contradicts itself:

```
Agent says:
"I own Property P1."

Property says:
"I have no Agent."
```

That's dangerous.

Remember that the owning side is:

```
Property.agent
```

So merely changing the inverse collection is not enough to reliably establish the database relationship.

## Helper Methods

A common solution is to provide relationship helper methods.

Inside Agent:

```
public void addProperty(Property property) {
    properties.add(property);
    property.setAgent(this);
}
```

Now:

```
agent.addProperty(property);
```

performs both operations:

```
Agent side
properties.add(property)

        +

Property side
property.agent = agent
```

The object graph stays consistent.

Removing the Relationship

Likewise:

```
public void removeProperty(Property property) {
    properties.remove(property);
    property.setAgent(null);
}
```

Now both directions agree:

```
Agent
no longer contains Property

Property
no longer references Agent
```

However, whether null is actually a valid business state is another question.

If:

```
@JoinColumn(
    name = "agent_id",
    nullable = false
)
```

then a property cannot persist without an agent.

So perhaps BrightMove's workflow should reassign a property directly:

```
Agent A
   ↓
Property
   ↓
Agent B
```

rather than temporarily leave:

```
agent = null
```

This is where persistence mapping and business rules meet.

## Why setAgent() May Still Exist

We might have:

```
public void setAgent(Agent agent) {
    this.agent = agent;
}
```

But unrestricted public relationship setters can make consistency harder.

For example:

```
property.setAgent(agent);
```

changes:

```
Property → Agent
```

but doesn't automatically update:

```
Agent → properties
```

One design is to control relationship changes through helper/domain methods.

For example:

```
public void assignTo(Agent agent) {
    this.agent = agent;
}
```

combined carefully with collection management.

There isn't one universal rule saying every relationship setter must be private or public.

The engineering goal is:

Provide an API that makes it difficult to create an inconsistent object graph.

## BrightMove Example

**Property → Viewing**

Now consider our second BrightMove relationship:

```
Property
    │
    ├── Viewing
    ├── Viewing
    └── Viewing
```

Database:

```
properties
-----------
id


viewings
-----------
id
requested_date_time
status
property_id   ← FK
```

Therefore:

```
Property 1
    ↓
many Viewings
```

and:

```
Viewing many
    ↓
one Property
```

---

**Viewing Owning Side**

Because:

```
viewings.property_id
```

contains the foreign key, Viewing owns the relationship.

```
@Entity
public class Viewing {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "property_id",
        nullable = false
    )
    private Property property;
}
```

Then Property has the inverse side:

```
@OneToMany(
    mappedBy = "property"
)
private List<Viewing> viewings =
        new ArrayList<>();
```

Again:

```
Property.viewings
      │
      │ mappedBy = "property"
      ▼
Viewing.property
      │
      │ @JoinColumn
      ▼
viewings.property_id
```

---

**Property Helper Methods**

For a bidirectional relationship:

```
public void addViewing(Viewing viewing) {
    viewings.add(viewing);
    viewing.setProperty(this);
}
```

and:

```
public void removeViewing(Viewing viewing) {
    viewings.remove(viewing);
    viewing.setProperty(null);
}
```

Then the service can say:

```
property.addViewing(viewing);
```

instead of remembering:

```
property.getViewings().add(viewing);
viewing.setProperty(property);
```

everywhere.

This isn't just shorter syntax.

It protects an **object invariant**:

If Property says it contains Viewing, Viewing should point back to that Property.

## Cascade

Now we reach a concept that is frequently misunderstood.

Suppose:

```
Property property = ...;

Viewing viewing = new Viewing(...);

property.addViewing(viewing);
```

The viewing is new.

Do we need:

```
viewingRepository.save(viewing);
```

or can persistence of the property propagate to the viewing?

That's what **cascade** controls.

Cascade means:

When a persistence operation happens to one entity, should that operation propagate to related entities?

For example:

```
@OneToMany(
    mappedBy = "property",
    cascade = CascadeType.PERSIST
)
private List<Viewing> viewings;
```

means:

```
persist Property
      ↓
also persist new Viewings
```

## Cascade Types

Important cascade types include:

- CascadeType.PERSIST
- CascadeType.MERGE
- CascadeType.REMOVE
- CascadeType.ALL

There are others, but these are the most useful for our current model.

---

`CascadeType.PERSIST`

```
cascade = CascadeType.PERSIST
```

means:

When the parent is persisted, also persist new related children.

Conceptually:

```
Property
   │
 persist
   ▼
Viewing
also persisted
```

---

`CascadeType.MERGE`

```
cascade = CascadeType.MERGE
```

means:

When entity state is merged, propagate that merge operation to the related entities.

This relates to the detached/managed lifecycle we covered in Module 6.

---

`CascadeType.REMOVE`

```
cascade = CascadeType.REMOVE
```

means:

When the parent entity is removed, also remove the related entities.

For example:

```
delete Property
      ↓
delete its Viewings
```

Whether this is correct depends entirely on the domain.

---

`CascadeType.ALL`

```
cascade = CascadeType.ALL
```

means essentially:

Cascade all supported JPA lifecycle operations.

It includes operations such as persist, merge and remove.

A common mapping is:

```
@OneToMany(
    mappedBy = "property",
    cascade = CascadeType.ALL
)
private List<Viewing> viewings =
        new ArrayList<>();
```

But don't use ALL merely because it is convenient.

The correct question is:

Does the child's persistence lifecycle genuinely belong to the parent?

## Cascade Is Not the Same as Database ON DELETE

This distinction matters.

JPA cascade:

```
cascade = CascadeType.REMOVE
```

means Hibernate propagates an entity operation.

A database foreign key may separately have something like:

```
ON DELETE CASCADE
```

which means the **database itself** propagates deletion.

These are different mechanisms operating at different layers:

```
JPA cascade
→ ORM/entity lifecycle

Database cascade
→ relational/database constraint behaviour
```

Don't treat them as synonyms.

## Cascade Design

**Agent → Property**

Suppose an agent leaves BrightMove.

Should this:

```
agentRepository.delete(agent);
```

automatically delete every property they manage?

Probably not.

The properties are business assets that should likely be reassigned:

```
Agent A leaves
      ↓
Properties remain
      ↓
Agent B takes over
```

Therefore this would be dangerous:

```
@OneToMany(
    mappedBy = "agent",
    cascade = CascadeType.ALL
)
private List<Property> properties;
```

because `ALL` includes `REMOVE`.

We generally would not want:

```
delete Agent
    ↓
delete Properties
```

This gives us an important cascade principle:

Relationship cardinality does not determine cascade. Lifecycle ownership does.

`Agent` having `@OneToMany` properties does not automatically mean Agent should cascade all operations to Property.

---

**Property → Viewing**

Now compare:

```
Property → Viewings
```

A viewing exists specifically for a property.

If the property is permanently deleted from the system, keeping its scheduled viewings may make no sense.

Here, stronger lifecycle ownership exists:

```
Property
   │
   └── Viewing
       exists because of Property
```

Therefore:

```
@OneToMany(
    mappedBy = "property",
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
private List<Viewing> viewings =
        new ArrayList<>();
```

may be a reasonable model.

But notice that we've now introduced something new:

```
orphanRemoval = true
```

Cascade remove and orphan removal solve different problems.

## orphanRemoval

Suppose:

```
Property P1

viewings:
V1
V2
V3
```

Then we do:

```
property.removeViewing(v2);
```

After removal:

```
Property P1

viewings:
V1
V3
```

What should happen to V2 in the database?

Without orphan removal, removing it from the collection does not inherently mean:

Delete this entity.

With:

```
orphanRemoval = true
```

JPA understands:

If this child is removed from its parent's relationship, remove that orphaned child entity.

So:

```
Property.viewings

[V1, V2, V3]

      ↓ remove V2

[V1, V3]

      ↓ orphanRemoval

DELETE V2
```

This fits the idea that a Viewing has no meaningful independent lifecycle once removed from its Property.

## `CascadeType.REMOVE` vs `orphanRemoval`

This distinction is worth memorizing conceptually.

CascadeType.REMOVE

Trigger:

```
Parent itself is deleted
```

Result:

```
delete parent
    ↓
delete children
```

Example:

```
Delete Property
→ delete its Viewings
```

---

orphanRemoval = true

Trigger:

Child is removed from parent's relationship

Result:

```
remove Viewing from Property.viewings
    ↓
delete that Viewing
```

So:

```
CascadeType.REMOVE
→ What happens to children when PARENT dies?

orphanRemoval
→ What happens to a child when the RELATIONSHIP is removed?
```

That is the cleanest mental distinction.

## Should Both Sides Have orphanRemoval?

No.

`orphanRemoval` is normally configured on the parent collection/one-to-one relationship that owns the child's lifecycle.

For BrightMove:

```
class Property {

    @OneToMany(
        mappedBy = "property",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Viewing> viewings;
}
```

You would **not** put orphanRemoval on:

```
@ManyToOne
private Property property;
```

inside Viewing.

The idea belongs to:

```
Property
    ↓
its collection of lifecycle-dependent Viewings
```

## Why Agent → Property Should Probably NOT Use orphanRemoval

Suppose:

```
Agent A
  ↓
Property P1
```

and P1 is reassigned:

```
Agent B
  ↓
Property P1
```

Removing P1 from Agent A's collection should not mean:

```
DELETE Property P1
```

The property hasn't ceased to exist.

It merely has a different agent.

Therefore:

```
@OneToMany(
    mappedBy = "agent",
    orphanRemoval = true
)
```

would likely model the wrong lifecycle.

This distinction is extremely important:

```
Property removed from Agent
→ Property still exists

Viewing removed from Property
→ Viewing may cease to exist
```

Same Java relationship cardinality:

```
@OneToMany
```

but completely different lifecycle semantics.

## Full BrightMove Relationship Mapping

Agent

```
@Entity
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "agent")
    private List<Property> properties =
            new ArrayList<>();

    protected Agent() {
    }

    public void addProperty(Property property) {
        properties.add(property);
        property.setAgent(this);
    }

    public void removeProperty(Property property) {
        properties.remove(property);
        property.setAgent(null);
    }
}
```

---

Property

```
@Entity
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "agent_id",
        nullable = false
    )
    private Agent agent;

    @OneToMany(
        mappedBy = "property",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<Viewing> viewings =
            new ArrayList<>();

    protected Property() {
    }

    void setAgent(Agent agent) {
        this.agent = agent;
    }

    public void addViewing(Viewing viewing) {
        viewings.add(viewing);
        viewing.setProperty(this);
    }

    public void removeViewing(Viewing viewing) {
        viewings.remove(viewing);
        viewing.setProperty(null);
    }
}
```

---

Viewing

```
@Entity
public class Viewing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "property_id",
        nullable = false
    )
    private Property property;

    protected Viewing() {
    }

    void setProperty(Property property) {
        this.property = property;
    }
}
```

Notice the design intent:

```
Agent → Property

Relationship:
bidirectional

Database owner:
Property

Lifecycle:
Property survives Agent relationship changes


Property → Viewing

Relationship:
bidirectional

Database owner:
Viewing

Lifecycle:
Viewing strongly belongs to Property
```

## Relationship Creation Workflow

Suppose we create a new Viewing.

Service:

```
@Transactional
public ViewingResponse createViewing(
        UUID propertyId,
        CreateViewingRequest request
) {

    Property property =
            propertyRepository.findById(propertyId)
                    .orElseThrow(
                        () -> new PropertyNotFoundException(propertyId)
                    );

    Viewing viewing =
            viewingMapper.toEntity(request);

    property.addViewing(viewing);

    return viewingMapper.toResponse(viewing);
}
```

Let's trace this carefully.

First:

```
Property property =
        propertyRepository.findById(...);
```

Property becomes managed.

Then:

```
Viewing viewing =
        viewingMapper.toEntity(request);
```

Viewing is initially:

```
TRANSIENT
```

Then:

```
property.addViewing(viewing);
```

performs:

```
viewings.add(viewing);
viewing.setProperty(this);
```

So:

```
Property
    ↓
contains Viewing

Viewing
    ↓
points to Property
```

Because the relationship has appropriate cascade persistence:

```
cascade = CascadeType.ALL
```

the new Viewing can be persisted through the managed aggregate relationship without an explicit:

```
viewingRepository.save(viewing);
```

This is the deeper reason the helper method + cascade combination matters.

The helper method establishes the object relationship.

Cascade determines how persistence operations propagate.

Those are two different jobs.

## Relationship Removal Workflow

Suppose a viewing is cancelled by actually removing that viewing record.

```
@Transactional
public void deleteViewing(
        UUID propertyId,
        UUID viewingId
) {

    Property property =
            propertyRepository.findById(propertyId)
                    .orElseThrow(
                        () -> new PropertyNotFoundException(propertyId)
                    );

    Viewing viewing = property.getViewings()
            .stream()
            .filter(item ->
                    item.getId().equals(viewingId)
            )
            .findFirst()
            .orElseThrow(
                () -> new ViewingNotFoundException(viewingId)
            );

    property.removeViewing(viewing);
}
```

The helper:

```
public void removeViewing(Viewing viewing) {
    viewings.remove(viewing);
    viewing.setProperty(null);
}
```

changes the relationship.

Because:

```
orphanRemoval = true
```

Hibernate can then delete that Viewing.

Conceptually:

```
Managed Property
       ↓
removeViewing(V2)
       ↓
V2 removed from collection
       ↓
V2 becomes orphan
       ↓
orphanRemoval
       ↓
DELETE viewing
```

Again:

```
No viewingRepository.delete(viewing)
```

is necessary in this particular model.

## Relationship Querying

Once relationships are mapped, Spring Data can navigate them in derived query names.

Suppose Viewing contains:

```
private Property property;
```

and `Property` contains:

```
private Agent agent;
```

We could define:

```
List<Viewing> findByPropertyId(UUID propertyId);
```

Spring interprets:

```
Viewing
  ↓
property
  ↓
id
```

We could also navigate deeper relationships:

```
List<Viewing> findByPropertyAgentId(UUID agentId);
```

Conceptually:

```
Viewing
   ↓
Property
   ↓
Agent
   ↓
ID
```

Spring Data derives the required relationship query.

We'll study richer relationship querying, JPQL and projections in Module 10.

## Don't Automatically Return Entities From Controllers

Bidirectional relationships introduce another problem.

Suppose Jackson serializes:

```
Property
   ↓
Agent
   ↓
Properties
   ↓
Agent
   ↓
Properties
   ↓
...
```

or:

```
Property
   ↓
Viewings
   ↓
Property
   ↓
Viewings
   ↓
...
```

You can get recursive serialization problems or expose far more data than intended.

This reinforces why we use DTOs:

```
Entity graph
     ↓
Mapper
     ↓
Purpose-specific DTO
     ↓
JSON
```

For example:

```
public record ViewingResponse(
        UUID id,
        LocalDateTime requestedDateTime,
        ViewingStatus status
) {}
```

The API doesn't need to expose the entire Hibernate object graph.

## Common Mistakes

**Putting the foreign key on the wrong conceptual side**

Start from the relational model:

```
One Agent
Many Properties

→ properties.agent_id
```

The many side usually contains the FK.

---

**Confusing mappedBy and @JoinColumn**

Remember:

```
mappedBy = "agent"
→ Java field

@JoinColumn(name = "agent_id")
→ database column
```

---

**Updating only one side of a bidirectional relationship**

Avoid:

```
agent.getProperties().add(property);
```

without ensuring:

```
property.agent = agent;
```

Use controlled helper methods.

---

**Assuming helper methods cause persistence**

This:

```
property.addViewing(viewing);
```

manages Java object consistency.

It does not itself mean:

```
INSERT INTO viewings
```

Persistence occurs because of the surrounding JPA lifecycle, managed entities, cascade and transaction.

---

**Using CascadeType.ALL everywhere**

Don't reason:

```
@OneToMany
→ therefore CascadeType.ALL
```

Instead ask:

```
Does the child share the parent's lifecycle?
```

---

**Using orphanRemoval because something has @OneToMany**

Again, cardinality isn't lifecycle.

```
Agent → Property
@OneToMany
but Property survives reassignment

→ probably NO orphanRemoval
```

versus:

```
Property → Viewing
@OneToMany
and removed Viewing has no independent purpose

→ orphanRemoval may be appropriate
```

## Engineering Trade-offs

Bidirectional relationships are convenient:

```
property.getAgent();

agent.getProperties();
```

But every additional navigation direction creates complexity:

- object graph consistency
- helper methods
- serialization risk
- fetching behaviour
- larger persistence graphs

Therefore:

Don't make every relationship bidirectional automatically.

Ask whether the application actually needs both navigation directions.

Likewise, cascading is convenient but dangerous when lifecycle ownership is misunderstood.

The relationship:

```
A has many B
```

does not tell us whether:

```
deleting A should delete B
```

That's a domain decision.

## The Relationship Design Framework

When you encounter a relationship in a future Spring project, reason in this order.

**Step 1 — Cardinality**

```
One-to-one?
One-to-many?
Many-to-one?
Many-to-many?
```

**Step 2 — Foreign key**

Ask:

Which table should contain the foreign key?

Example:

```
properties.agent_id
```

**Step 3 — Owning side**

The entity mapping the foreign key is normally the owning side.

```
Property.agent
```

**Step 4 — Navigation**

Do we need:

```
Property → Agent
```

only?

Or also:

```
Agent → Properties
```

Don't add bidirectionality without a reason.

**Step 5 — Consistency**

If bidirectional:

How will we guarantee both sides agree?

Usually helper/domain methods.

**Step 6 — Lifecycle**

Ask:

If the parent disappears, should the child disappear?

This determines cascade behaviour.

**Step 7 — Orphan semantics**

Ask:

If the child is removed from this parent's relationship, should the child itself cease to exist?

This determines `orphanRemoval`.

This framework is more valuable than memorizing annotations.

## Summary

For:

```
Agent 1 → many Properties
```

database:

```
properties.agent_id
```

Java:

```
Property:
@ManyToOne
@JoinColumn(name = "agent_id")
private Agent agent;
```

and optionally:

```
Agent:
@OneToMany(mappedBy = "agent")
private List<Property> properties;
```

The fundamental rule:

The entity containing the foreign-key mapping is the owning side.

For bidirectional relationships:

```
Owning side
→ controls database relationship

Inverse side
→ mappedBy points to owning-side Java field
```

Helper methods maintain Java consistency:

```
property.addViewing(viewing);
```

can internally perform:

```
viewings.add(viewing);
viewing.setProperty(this);
```

Cascade answers:

Should persistence operations propagate to related entities?

Orphan removal answers:

Should a child be deleted when removed from the parent's relationship?

And the most important lifecycle comparison from BrightMove is:

```
Agent → Property
Property can survive reassignment
→ weak lifecycle ownership
→ don't cascade REMOVE / orphan removal casually


Property → Viewing
Viewing belongs strongly to Property
→ strong lifecycle ownership
→ cascade + orphanRemoval may make sense
```

Finally, use this design sequence:

```
Cardinality
    ↓
Foreign Key
    ↓
Owning Side
    ↓
Navigation Direction
    ↓
Bidirectional Consistency
    ↓
Cascade
    ↓
Orphan Semantics
```

If you reason through relationships in that order, the annotations become consequences of the model rather than things you have to memorize.
