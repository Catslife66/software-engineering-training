# Fetching & Performance

We have now reached one of the places where JPA starts to feel less like ordinary Java.

Consider:

```
Agent agent = agentRepository.findById(agentId)
        .orElseThrow(...);

List<Property> properties = agent.getProperties();
```

At Java level:

```
agent.getProperties();
```

looks like a simple getter.

But Hibernate may interpret it as:

```
"I don't have these Properties yet."
        ↓
execute SQL
        ↓
load them from PostgreSQL
```

This is the central problem of this lesson:

> Your Java object graph and your database loading behaviour are not the same thing.

We will cover:

- LAZY
- EAGER
- Hibernate proxies
- persistence context and lazy loading
- LazyInitializationException
- N+1 queries
- first-level cache and why it doesn't fully solve N+1
- JOIN FETCH
- duplicate rows from collection fetches
- `@EntityGraph`
- DTO projections
- choosing a fetch strategy

## Start With the Entity Graph

Our BrightMove relationships look like:

```
Agent
  │
  └── Properties
          │
          └── Viewings
```

Java lets us navigate:

```
agent.getProperties();
```

and:

```
property.getAgent();
```

and:

```
property.getViewings();
```

But imagine loading one Agent.

Should Hibernate automatically load:

```
Agent
 ↓
all Properties
 ↓
all Viewings
 ↓
their other relationships...
```

Probably not.

An object graph can become very large.

Hibernate therefore needs a fetching strategy:

> When should related entities actually be loaded from the database?

## `LAZY` Loading

Lazy loading means:

Don't load the relationship immediately. Load it when the application actually accesses it.

For example:

```
@OneToMany(
    mappedBy = "agent",
    fetch = FetchType.LAZY
)
private List<Property> properties;
```

Suppose:

```
Agent agent = agentRepository.findById(agentId)
        .orElseThrow(...);
```

Hibernate initially needs:

```
SELECT ...
FROM agents
WHERE id = ?;
```

Conceptually:

```
Agent
├── id       ✓
├── name     ✓
├── email    ✓
└── properties
       ↓
    not loaded yet
```

Then later:

```
agent.getProperties();
```

Hibernate realizes the collection is needed.

It can issue:

```
SELECT ...
FROM properties
WHERE agent_id = ?;
```

Now:

```
Agent
└── properties
      ├── Property A
      ├── Property B
      └── Property C
```

This is lazy loading.

---

**Lazy Loading Is Deferred Database Work**

This mental model is extremely important.

Don't think:

```
agent.getProperties();
```

means only:

```
return a Java field
```

With a Hibernate-managed relationship, it can mean:

```
Access relationship
       ↓
Is it initialized?
       │
       ├── yes → return loaded data
       │
       └── no
             ↓
          execute SQL
             ↓
          initialize relationship
             ↓
          return data
```

So a getter can indirectly trigger I/O.

That is one reason ORM performance can surprise developers.

## `EAGER` Loading

Eager loading means:

This relationship should be available as part of loading the entity.

Example:

```
@ManyToOne(fetch = FetchType.EAGER)
private Agent agent;
```

If we retrieve:

```
Property property =
        propertyRepository.findById(id)
                .orElseThrow(...);
```

Hibernate must ensure the associated Agent is loaded as part of fulfilling that eager relationship.

Conceptually:

```
Load Property
      ↓
also load Agent
```

The exact SQL strategy is provider/query dependent. It might use a join or additional SQL.

This distinction matters:

> EAGER means the relationship must be fetched; it does not mean Hibernate must always use one SQL JOIN.

## Default Fetch Types

JPA has defaults.

For to-one relationships:

```
@ManyToOne
@OneToOne
```

the JPA default is:

```
EAGER
```

For collection relationships:

```
@OneToMany
@ManyToMany
```

the default is:

```
LAZY
```

So:

```
@ManyToOne
private Agent agent;
```

is eager by JPA default.

But in many real applications, we deliberately write:

```
@ManyToOne(fetch = FetchType.LAZY)
private Agent agent;
```

because we don't want every Property query automatically requiring Agent data.

A common practical approach is:

> Keep relationships lazy by default and explicitly fetch what each use case needs.

Not as an absolute law, but as a useful starting strategy.

---

**Why Not Just Make Everything EAGER?**

Suppose:

```
Property
├── Agent
├── Viewings
├── Images
├── Features
└── Offers
```

If everything is eager, asking:

```
propertyRepository.findById(id);
```

may cause Hibernate to load much more information than this endpoint needs.

Maybe the endpoint only wants:

```
{
  "id": "...",
  "title": "City Centre Flat",
  "price": 250000
}
```

But the persistence layer potentially loads a much larger graph.

This causes:

```
unnecessary database work
        +
more memory usage
        +
more data transfer
        +
harder-to-predict queries
```

So:

```
EAGER everywhere
```

is not the solution to lazy-loading problems.

## Persistence Context and Lazy Loading

Lazy loading depends heavily on something we studied earlier:

**The persistence context.**

Consider:

```
@Transactional(readOnly = true)
public PropertyResponse getProperty(UUID id) {

    Property property =
            propertyRepository.findById(id)
                    .orElseThrow(...);

    Agent agent = property.getAgent(); // default EAGER

    return mapper.toResponse(property);
}
```

Inside the transaction:

```
@Transactional
      ↓
Persistence Context OPEN
      ↓
load Property
      ↓
property.getAgent()
      ↓
Hibernate can query Agent
      ↓
return DTO
      ↓
transaction ends
      ↓
Persistence Context closes
```

Hibernate can lazy-load because the entity is still managed and connected to an active persistence context.

## `LazyInitializationException`

Now consider:

```
public Property getProperty(UUID id) {

    return propertyRepository.findById(id)
            .orElseThrow(...);
}
```

Later, after the persistence context is no longer available:

```
property.getViewings();
```

If viewings was lazy and never initialized, Hibernate needs to query the database.

But it no longer has the required active persistence context/session for that detached entity.

Result:

```
LazyInitializationException
```

Conceptually:

```
Property loaded
      ↓
viewings NOT loaded
      ↓
Persistence Context closes
      ↓
Property becomes detached
      ↓
getViewings()
      ↓
Hibernate needs DB
      ↓
but cannot lazy-load
      ↓
LazyInitializationException
```

## Why This Is Not Really a "Getter Problem"

The real problem is:

The application tried to access unloaded persistence state after the entity left the context capable of loading it.

That's why this can work:

```
@Transactional(readOnly = true)
public PropertyResponse getProperty(UUID id) {

    Property property = propertyRepository.findById(id)
            .orElseThrow(...);

    return mapper.toResponse(property);
}
```

if the mapper accesses:

```
property.getViewings();
```

inside the transaction.

But moving the mapping outside can fail if the collection was never initialized.

---

**Don't Fix This by Making Everything Eager**

A beginner often sees:

```
LazyInitializationException
```

and thinks:

I'll change `LAZY` to `EAGER`.

That may remove this particular exception while introducing performance problems elsewhere.

The better question is:

> What data does this use case need, and where should that data be loaded?

Possible answers include:

```
Entity only
→ ordinary query

Entity + specific relationship
→ JOIN FETCH / EntityGraph

Only selected output fields
→ DTO projection
```

That is a much stronger design approach.

## The N+1 Problem

Now we reach the most important performance problem in this module.

Suppose we want:

```
Return 100 properties and the name of each property's agent.
```

We query:

```
List<Property> properties =
        propertyRepository.findAll();
```

Assume:

```
@ManyToOne(fetch = FetchType.LAZY)
private Agent agent;
```

Hibernate executes:

```
Query 1
→ load 100 Properties
```

Then our mapper does:

```
properties.stream()
        .map(property -> new PropertyResponse(
                property.getId(),
                property.getTitle(),
                property.getAgent().getName()
        ))
        .toList();
```

For each Property:

```
property.getAgent()
```

may trigger another query.

Worst-case conceptual behaviour:

```
1 query
→ Properties

+ 100 queries
→ Agent for Property 1
→ Agent for Property 2
→ Agent for Property 3
...
→ Agent for Property 100
```

Total:

```
1 + 100 = 101 queries
```

This is the **N+1 query problem**.

---

**Why It Is Called N+1**

Because:

```
1 query
```

loads the initial collection.

Then:

```
N additional queries
```

load related data.

So:

```
1 + N
```

For 10 properties:

```
11 queries
```

For 100:

```
101 queries
```

For 10,000:

potentially disastrous.

The Java code may look completely innocent:

```
property.getAgent().getName();
```

which is why N+1 can be easy to miss.

---

**Does Hibernate Query the Same Agent 100 Times?**

Not necessarily.

Suppose all 100 properties belong to the same Agent.

Hibernate has a **first-level cache** inside the persistence context.

After Agent A has been loaded, Hibernate knows:

```
Agent A
→ already managed
```

So repeated references to exactly the same Agent identity may be resolved from the persistence context rather than requiring another SQL query each time.

If:

```
100 Properties
10 unique Agents
```

the pattern may be closer to:

```
1 Property query
+
up to 10 Agent loads
```

rather than 101 queries.

This is why N+1 isn't always literally N + 1 SQL statements in every execution.

But the underlying problem remains:

> Relationship traversal is causing additional database queries proportional to related entities.

The persistence context can reduce duplicate entity loads.

It does not make the fetch strategy good.

---

**N+1 With Collections**

The problem becomes even clearer with:

```
Properties → Viewings
```

Suppose:

```
List<Property> properties =
        propertyRepository.findAll();
```

Then:

```
for (Property property : properties) {
    System.out.println(
            property.getViewings().size()
    );
}
```

Hibernate may do:

```
Query 1:
load Properties

Query 2:
load viewings for Property 1

Query 3:
load viewings for Property 2

Query 4:
load viewings for Property 3

...
```

Collections are associated with different parents, so first-level caching doesn't magically collapse these into one relationship query.

## How Do We Solve N+1?

If we already know the use case needs:

```
Property + Agent
```

why fetch Properties first and discover Agents one at a time?

Instead, tell Hibernate:

Fetch the Agent relationship as part of this query.

One major tool is:

```
JOIN FETCH
```

### `JOIN FETCH`

Repository:

```
@Query("""
    SELECT p
    FROM Property p
    JOIN FETCH p.agent
""")
List<Property> findAllWithAgent();
```

Remember the distinction from 10.

Ordinary:

```
JOIN p.agent a
```

means:

```
Use Agent in the query.
```

But:

JOIN FETCH p.agent

means:

Fetch this relationship as part of loading the returned Property entities.

Conceptually:

```
Without fetch join:

Query Properties
      ↓
Property.agent lazy
      ↓
later queries


With fetch join:

Property + Agent
      ↓
one planned query
      ↓
relationships initialized
```

Conceptual SQL:

```
SELECT p
FROM Property p
JOIN FETCH p.agent
```

may translate conceptually into SQL like:

```
SELECT p.*, a.*
FROM properties p
JOIN agents a
    ON p.agent_id = a.id;
```

The database returns the information together.

Hibernate reconstructs:

```
Property A → Agent X
Property B → Agent Y
Property C → Agent X
```

from that result set.

Now calling:

```
property.getAgent();
```

doesn't need to lazy-load the Agent because that relationship has already been initialized for this query.

## Fetch Strategy Belongs to the Use Case

This is an important architectural principle.

Suppose one endpoint needs:

```
Property only
```

Another needs:

```
Property + Agent
```

Another needs:

```
Property + Viewings
```

We should not necessarily encode all of that permanently into:

```
@Entity
class Property
```

with EAGER relationships.

Instead:

```
Entity mapping
→ safe/default loading behaviour

Specific repository query
→ fetch plan for specific use case
```

Example:

```
Optional<Property> findById(UUID id);
```

for basic access.

And:

```
@Query("""
    SELECT p
    FROM Property p
    JOIN FETCH p.agent
    WHERE p.id = :id
""")
Optional<Property> findByIdWithAgent(UUID id);
```

for a use case that needs Agent.

## Fetching Collections

We can also fetch collections:

```
@Query("""
    SELECT p
    FROM Property p
    LEFT JOIN FETCH p.viewings
    WHERE p.id = :id
""")
Optional<Property> findByIdWithViewings(UUID id);
```

Why `LEFT JOIN FETCH`?

Because we still want the Property even if:

viewings = []

With an inner fetch join, a Property without Viewings could disappear from the result.

So:

```
JOIN FETCH
→ relationship must have matching row

LEFT JOIN FETCH
→ parent still returned if relationship is empty
```

## Collection Fetch Joins and Duplicate Rows

Collections introduce another important issue.

Suppose:

```
Property P1
├── Viewing V1
├── Viewing V2
└── Viewing V3
```

A SQL join produces:

```
P1 | V1
P1 | V2
P1 | V3
```

The relational result has three rows.

But Java conceptually wants:

```
one Property P1
    ↓
three Viewings
```

Hibernate reconstructs the object graph from the repeated relational rows.

This is a fundamental ORM idea:

```
Relational result
→ repeated parent columns

Object graph
→ one parent containing collection
```

Depending on query/result semantics, duplicates at the root result level may need attention. A common JPQL pattern is:

```
SELECT DISTINCT p
FROM Property p
LEFT JOIN FETCH p.viewings
```

The purpose is to express:

Return unique Property roots while fetching their Viewings.

Modern Hibernate can handle some root de-duplication automatically in entity fetch joins, but understanding the row multiplication is still essential.

## Why Fetching Multiple Collections Can Become Dangerous

Imagine:

```
Property P1

Viewings:
V1
V2
V3

Images:
I1
I2
I3
I4
```

If we fetch both collections in one SQL join:

```
Property
× Viewings
× Images
```

we can get:

```
3 × 4 = 12 rows
```

for one Property.

If there are:

```
10 Viewings
20 Images
15 Features
```

the result set can explode.

This is sometimes called a **Cartesian-product-style explosion**.

Therefore:

One giant fetch join is not automatically the fastest query.

Performance engineering is about retrieving the right data efficiently, not minimizing SQL statement count at all costs.

## `@EntityGraph`

Spring Data JPA gives us another way to define which relationships should be fetched:

```
@EntityGraph(attributePaths = {"agent"})
List<Property> findByCity(String city);
```

This says:

For this repository method, fetch agent along with Property.

The method still looks like a normal derived query:

```
findByCity(...)
```

but its fetch plan is customized.

Example:

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID> {

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByCity(String city);
}
```

Without EntityGraph:

```
findByCity
    ↓
Properties
    ↓
agent remains lazy
```

With EntityGraph:

```
findByCity
    ↓
Properties + Agent fetched for this use case
```

So EntityGraph separates:

```
query condition
```

from:

```
fetch plan
```

quite nicely.

---

**Multiple Attributes in an EntityGraph**

You can specify:

```
@EntityGraph(
    attributePaths = {
        "agent",
        "viewings"
    }
)
Optional<Property> findDetailedById(UUID id);
```

Conceptually:

```
Property
├── Agent
└── Viewings
```

should be fetched for that method.

But remember the previous section:

> Fetching more relationships isn't automatically better.

Always consider the size and multiplicity of the resulting graph.

## `JOIN FETCH` vs `@EntityGraph`

Both solve a similar high-level problem:

Explicitly fetch relationships required by a use case.

But they express it differently.

**JOIN FETCH**

```
@Query("""
    SELECT p
    FROM Property p
    JOIN FETCH p.agent
    WHERE p.city = :city
""")
List<Property> findByCityWithAgent(String city);
```

The fetch plan is explicitly part of JPQL.

Useful when:

- already writing custom JPQL
- need explicit join semantics
- query itself is more complex

**@EntityGraph**

```
@EntityGraph(attributePaths = "agent")
List<Property> findByCity(String city);
```

The query remains a Spring Data derived query.

Useful when:

- query condition is simple
- only fetch plan needs customization
- you want to avoid custom JPQL

Mental model:

```
JOIN FETCH
→ query and fetching expressed together

EntityGraph
→ query and fetch plan expressed separately
```

Neither is universally superior.

## What About DTO Projections?

There is another solution to many read-performance problems:

Don't load entities at all.

Suppose the endpoint needs:

```
property id
title
city
agent name
```

We could fetch:

```
Property + Agent entities
```

and map them.

Or directly query:

```
public record PropertySummary(
        UUID id,
        String title,
        String city,
        String agentName
) {}
```

with:

```
@Query("""
    SELECT new com.brightmove.dto.PropertySummary(
        p.id,
        p.title,
        p.city,
        a.name
    )
    FROM Property p
    JOIN p.agent a
    WHERE p.city = :city
""")
List<PropertySummary> findSummariesByCity(
        String city
);
```

Now:

```
Database
    ↓
only required columns
    ↓
PropertySummary DTO
```

No lazy relationship traversal is needed.

## Fetch Join vs Projection

These solve different use cases.

**Need actual entities**

For example:

```
@Transactional
public void updatePropertyAndAgent(...) {...}
```

Business logic needs managed entities.

A fetch join may be appropriate:

```
Property + Agent entities
```

**Need read-only API data**

For example:

```
GET /properties/search
```

A projection may be better:

```
PropertySummary
```

because we don't need:

```
managed entity
dirty checking
complete object state
```

This connects directly to our previous Q&A:

> Do I need the entity, or do I only need data?

## A Complete N+1 Example

Let's trace one realistic endpoint.

```
GET /properties
```

Response requires:

```
[
  {
    "title": "Flat A",
    "agentName": "Sarah"
  },
  {
    "title": "Flat B",
    "agentName": "David"
  }
]
```

Repository:

```
List<Property> findAll();
```

Service:

```
@Transactional(readOnly = true)
public List<PropertyResponse> getProperties() {

    return propertyRepository.findAll()
            .stream()
            .map(property -> new PropertyResponse(
                    property.getTitle(),
                    property.getAgent().getName()
            ))
            .toList();
}
```

At Java level:

```
find all Properties
→ map
→ get Agent name
```

At database level potentially:

```
SELECT properties

SELECT agent for property 1
SELECT agent for property 2
SELECT agent for property 3
...
```

That's N+1.

---

### Solution 1 — Fetch Join

Repository:

```
@Query("""
    SELECT p
    FROM Property p
    JOIN FETCH p.agent
""")
List<Property> findAllWithAgent();
```

Service:

```
@Transactional(readOnly = true)
public List<PropertyResponse> getProperties() {

    return propertyRepository.findAllWithAgent()
            .stream()
            .map(property -> new PropertyResponse(
                    property.getTitle(),
                    property.getAgent().getName()
            ))
            .toList();
}
```

Conceptually:

```
one planned query
      ↓
Property + Agent
      ↓
map safely
```

---

### Solution 2 — EntityGraph

Repository:

```
@EntityGraph(attributePaths = "agent")
List<Property> findAll();
```

Conceptually:

```
findAll
+
fetch agent
```

This can also avoid the relationship being lazily queried one at a time.

In a real codebase, you might prefer a specially named repository method rather than altering the fetch plan of a broadly used inherited method, so that intent remains obvious.

---

### Solution 3 — Projection

Repository:

```
@Query("""
    SELECT new com.brightmove.dto.PropertyResponse(
        p.title,
        a.name
    )
    FROM Property p
    JOIN p.agent a
""")
List<PropertyResponse> findAllResponses();
```

Now:

```
Database
    ↓
title + agent name
    ↓
DTO
```

This can be particularly attractive for a pure read endpoint.

## Why Not Always Use Projections?

Because entities aren't merely database-shaped DTOs.

Suppose the workflow is:

```
Load Property
    ↓
validate domain state
    ↓
change price
    ↓
change status
    ↓
dirty checking
    ↓
commit
```

We need the managed entity.

A projection such as:

```
PropertySummary
```

doesn't provide:

```
entity lifecycle
dirty checking
domain behaviour
managed relationships
```

So:

```
Command/write workflow
→ entities often appropriate

Read/report/query workflow
→ projections often attractive
```

This resembles a lightweight version of the broader architectural idea of separating commands from queries.

## Fetching and Pagination

There is an important warning for later.

Suppose we combine:

```
pagination
+
collection fetch join
```

For example:

```
Give me 20 Properties
+
fetch all Viewings
```

But SQL sees:

```
Property 1 repeated 8 times
Property 2 repeated 4 times
Property 3 repeated 12 times
...
```

because of the collection join.

Pagination operates over relational results, while our Java model wants unique parent entities.

This can make collection fetch joins with pagination difficult or inefficient.

We'll handle pagination properly in 3.12, but record this warning now:

> Be cautious when combining pagination with collection fetch joins.

For paginated read endpoints, projections or carefully designed multi-step queries are often better.

## Fetching Is Part of Query Design

At this point, we can refine our repository mental model.

A repository method isn't only asking:

> Which rows/entities do I need?

It may also need to answer:

> Which relationships must be initialized for this use case?

For example:

```
findById(id)
```

means:

```
Find Property
```

while:

```
findByIdWithAgent(id)
```

means:

```
Find Property
+
this workflow needs Agent
```

That fetch intent should be deliberate.

---

### Don't Rely on Accidental Lazy Loading

Consider:

```
@Transactional(readOnly = true)
public PropertyResponse getProperty(UUID id) {

    Property property =
            propertyRepository.findById(id)
                    .orElseThrow(...);

    return propertyMapper.toResponse(property);
}
```

And mapper:

```
public PropertyResponse toResponse(Property property) {
    return new PropertyResponse(
            property.getId(),
            property.getTitle(),
            property.getAgent().getName(),
            property.getViewings().size()
    );
}
```

This works.

But the repository says:

```
load Property
```

while the mapper secretly causes:

```
load Agent
load Viewings
```

The database access pattern is hidden across layers.

A more explicit repository method:

```
findByIdWithAgentAndViewings(...)
```

or a projection query makes the use case's data requirements easier to see.

This is an important step from:

"The code works."

toward:

"The data-access behaviour is intentional."

## Open Session in View

You may encounter a Spring setting/concept called:

```
Open Session in View
```

or OSIV.

Its basic idea is that the persistence context remains available beyond the service transaction and into web request processing.

That can allow lazy loading during controller/serialization work.

For example:

```
Controller / JSON serialization
      ↓
property.getAgent()
      ↓
Hibernate can still issue query
```

This can make applications appear convenient.

But it can also hide database access outside carefully designed service/query boundaries.

You can end up with:

```
Controller
Mapper
Jackson serializer
```

unexpectedly causing SQL.

For our engineering model, prefer:

> Load the data the use case needs deliberately within the application/service query boundary rather than depending on accidental late lazy loading.

## Performance Isn't "Fewest Queries Wins"

Suppose option A uses:

```
2 small efficient queries
```

and option B uses:

```
1 enormous join
```

that produces 500,000 rows because several collections multiply each other.

Option B isn't necessarily better.

So don't reduce performance reasoning to:

```
1 query = good
many queries = bad
```

The real questions are:

```
How many queries?

How much data does each query return?

How many rows?

How many columns?

Are relationships duplicated?

Are indexes usable?

How often does this endpoint run?

How much memory is used?

What is the latency?
```

N+1 is bad because it creates repeated unnecessary round trips—not because SQL query count must always equal one.

## How Engineers Detect N+1

Don't guess.

Observe what Hibernate is doing.

During development, SQL logging can expose patterns like:

```
select ... from properties

select ... from agents where id=?
select ... from agents where id=?
select ... from agents where id=?
select ... from agents where id=?
```

That pattern should immediately make you ask:

Why is this relationship being loaded repeatedly?

Other tools in real systems include:

- Hibernate statistics
- application performance monitoring
- database query logs
- tracing
- integration tests that inspect query counts

The important habit is:

> ORM performance should be observed, not assumed.

## BrightMove Fetch Strategy

A sensible starting design could be:

```
@Entity
public class Property {

    @ManyToOne(fetch = FetchType.LAZY)
    private Agent agent;

    @OneToMany(
        mappedBy = "property",
        fetch = FetchType.LAZY
    )
    private List<Viewing> viewings;
}
```

Default:

```
relationships lazy
```

Then use-case-specific repository methods:

```
@Query("""
    SELECT p
    FROM Property p
    JOIN FETCH p.agent
    WHERE p.id = :id
""")
Optional<Property> findByIdWithAgent(UUID id);
```

or:

```
@EntityGraph(attributePaths = "viewings")
Optional<Property> findWithViewingsById(UUID id);
```

or projections:

```
@Query("""
    SELECT new com.brightmove.dto.PropertySummary(
        p.id,
        p.title,
        p.city,
        a.name
    )
    FROM Property p
    JOIN p.agent a
""")
List<PropertySummary> findPropertySummaries();
```

Now the entity model doesn't force one fetching strategy on every endpoint.

## Choosing the Right Tool

Use this framework.

- Do I need the relationship at all?

  No.

  ```
  Leave it lazy.
  ```

- Do I need related entities for business logic?

  Yes.

  ```
  JOIN FETCH
  or
  @EntityGraph
  ```

- Do I only need selected fields for a read endpoint?

  Yes.

  ```
  DTO projection
  ```

- Is a collection large?

  Be careful about eagerly fetching it.

- Am I returning many parent entities?

  Check for N+1.

- Am I fetching several collections together?

  Check for row explosion.

- Am I paginating?

  Be cautious with collection fetch joins.

## Common Mistakes

**Making everything EAGER**

This trades visible lazy-loading problems for hidden over-fetching.

---

**Assuming EAGER means one SQL query**

It doesn't.

It specifies when related state must be available, not the exact SQL implementation.

---

**Treating getters as free**

```
property.getAgent()
property.getViewings()
```

can trigger SQL.

---

**Solving N+1 by fetching the entire object graph**

You may replace many small queries with one huge result-set explosion.

---

**Mapping entities outside the persistence context**

If the mapper touches an uninitialized lazy relationship:

```
LazyInitializationException
```

may occur.

---

**Returning entities directly from controllers**

This can cause:

- uncontrolled lazy loading
- recursive relationships
- excessive serialization
- persistence details leaking into API contracts

DTOs remain preferable.

---

**Assuming first-level cache solves N+1**

It may reduce duplicate loads of identical entities, but it doesn't fix an inappropriate fetch plan.

## The Complete Mental Model

When Hibernate loads:

```
Property property = repository.findById(id);
```

don't imagine the entire object graph automatically exists in memory.

Think:

```
Property entity
│
├── basic fields
│      loaded
│
├── Agent
│      potentially lazy
│
└── Viewings
       potentially lazy
```

Then:

```
Access lazy relationship
       ↓
Persistence context available?
       │
       ├── YES
       │    ↓
       │  query if needed
       │
       └── NO
            ↓
     LazyInitializationException
```

When loading collections:

```
Load N parents
      ↓
access relationship for each
      ↓
N additional queries?
      ↓
N+1
```

Then choose:

```
Need entities + relationship
        ↓
JOIN FETCH / EntityGraph

Need only output data
        ↓
DTO Projection
```

## How This Connects to Module 10

In 10 we learned:

```
What data should I query?
```

In 11 we add:

```
How should Hibernate load that data?
```

These aren't separate concerns.

Together:

```
Use case
   ↓
What data is required?
   ↓
Entities or projection?
   ↓
Which relationships are required?
   ↓
What fetch strategy?
   ↓
How many queries / rows?
```

This is now genuine persistence design rather than simply knowing Spring Data annotations.

## Summary

The core distinction:

```
LAZY
→ relationship loaded when needed

EAGER
→ relationship must be loaded as part of entity loading
```

Lazy loading requires an appropriate persistence context:

```
lazy relationship
+
open persistence context
→ Hibernate can query

lazy relationship
+
detached entity
→ LazyInitializationException
```

The major performance danger:

```
1 parent query
+
N relationship queries
=
N+1
```

The main tools:

```
JOIN FETCH
→ explicitly fetch relationship in JPQL

@EntityGraph
→ define fetch plan separately from query

DTO projection
→ don't load entities when only selected data is needed
```

But:

```
more fetching
≠
better performance
```

because collection joins can multiply rows.

The practical design strategy is:

```
Relationships LAZY by default
          ↓
Understand each use case
          ↓
Need entities?
    ├── yes
    │    ↓
    │ JOIN FETCH / EntityGraph
    │
    └── no
         ↓
      DTO Projection
          ↓
Observe generated SQL
          ↓
Check for N+1 / over-fetching
```

And the most important engineering principle from this module is:

> Fetching should be designed around the data requirements of each use case, rather than allowing the entity relationship mapping to decide automatically what every request loads.

```
@Transactional / persistence context
→ makes lazy loading possible while entity is managed

JOIN FETCH / EntityGraph
→ controls how required relationships are fetched efficiently

Projection
→ avoids loading full entities when only read data is needed

---

Entity mapping:
@ManyToOne(fetch = LAZY)
@OneToMany(fetch = LAZY)

        ↓

Repository loads entity

        ↓

Inside active persistence context?

YES
→ entity is managed
→ lazy relationship can be loaded when accessed

NO
→ uninitialized lazy relationship cannot be loaded
→ LazyInitializationException may occur

        ↓

If use case already knows relationship is needed:

Need full related entities
→ JOIN FETCH / EntityGraph

Need only selected read data
→ DTO projection

---

Persistence context
→ determines whether lazy loading CAN happen

Fetch strategy
→ determines HOW/WHEN data SHOULD be loaded

Projection
→ determines whether entities need to be loaded at all
```
