# FetchType

Consider this code:

```
Property property = propertyRepository.findById(id)
        .orElseThrow(...);
```

The Property has:

```
@ManyToOne(fetch = FetchType.LAZY)
private Agent agent;

@OneToMany(fetch = FetchType.LAZY)
private List<Viewing> viewings;
```

LAZY means:

```
Load the Property now.
Load Agent or Viewings only when they are actually accessed.
```

For example:

```
property.getTitle();
```

does not need the viewings.

But:

```
property.getViewings();
```

may trigger another database query.

## Why LAZY is usually safer

Imagine an endpoint:

```
GET /properties/{id}
```

that returns:

```
title
city
price
bedrooms
status
```

If Hibernate eagerly loads:

```
Agent
all Viewings
each Viewing's related data
```

we load much more than the endpoint needs.

That can cause:

```
more SQL queries
more memory usage
slower responses
```

So for our relationships, use:

```
@ManyToOne(fetch = FetchType.LAZY)
```

and:

```
@OneToMany(
    mappedBy = "property",
    fetch = FetchType.LAZY,
    cascade = CascadeType.ALL,
    orphanRemoval = true
)
```

`@OneToMany` is lazy by default, but writing it explicitly can make the design intention clearer.

## Important warning

Lazy loading only works while the persistence context is open.

Example:

```
@Transactional(readOnly = true)
public PropertyResponse getProperty(UUID id) {
    Property property = propertyRepository.findById(id)
            .orElseThrow(...);

    property.getViewings(); // can load here

    return mapper.toResponse(property);
}
```

Inside the transaction, the entity is managed.

But if you try to access an unloaded relationship later, after the transaction ends:

```
property.getViewings();
```

Hibernate may throw:

```
LazyInitializationException
```

because the entity is detached and the persistence context is gone.

## Design rule

Do not rely on controllers or JSON serialization to explore lazy relationships automatically.

Instead, decide in the service what data the endpoint needs and map that data into a DTO while the transaction is active.

For example:

```
PropertySummaryResponse
→ no viewings

PropertyDetailResponse
→ perhaps viewing count

PropertyViewingResponse
→ explicitly include viewings
```

## How to access property.getViewings()

Use the service:

```
@Transactional(readOnly = true)
public List<ViewingResponse> getPropertyViewings(UUID propertyId) {
    Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new PropertyNotFoundException(propertyId));

    return property.getViewings()
            .stream()
            .map(viewingMapper::toResponse)
            .toList();
}
```

Why in the service:

```
Service owns the read workflow.
Transaction keeps the persistence context open.
LAZY viewings can be loaded safely.
Mapper converts entities into response DTOs.
Controller receives finished API data.
```

## JSON recursion

Returning entities directly can cause infinite recursion:

```
Property
→ viewings
→ each Viewing.property
→ that Property.viewings
→ ...
```

This may lead to:

```
StackOverflowError
serialization failure
huge repeated JSON
```

A DTO breaks the cycle because we choose the exact response shape.

For example:

```
public class ViewingResponse {

    private final UUID id;
    private final LocalDateTime requestedDateTime;
    private final ViewingStatus status;

    public ViewingResponse(
            UUID id,
            LocalDateTime requestedDateTime,
            ViewingStatus status
    ) {
        this.id = id;
        this.requestedDateTime = requestedDateTime;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getRequestedDateTime() {
        return requestedDateTime;
    }

    public ViewingStatus getStatus() {
        return status;
    }
}
```

The endpoint can return:

```
[
  {
    "id": "...",
    "requestedDateTime": "2026-08-10T10:00:00",
    "status": "PENDING"
  }
]
```

So DTOs help with both:

```
API control
and
preventing recursive entity serialization
```
