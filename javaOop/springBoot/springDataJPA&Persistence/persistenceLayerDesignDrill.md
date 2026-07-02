# Persistence Layer Design Drill

## Drill 1 - Manage my properties

**Feature**

```
Agent dashboard: Manage my properties
```

**Endpoint:**

```
GET /agent/properties
```

**Requirement:**

```
Authenticated agent can view only their own properties.
Supports optional filters:
- city
- status
- minPrice
- maxPrice
Supports pagination and sorting.
Returns DTO list, not entities.
```

**Task**

Design the architecture.

1. Controller

- gets authenticated userId
- receives PropertySearchRequestDTO
- receives Pageable
- calls propertyService.getAgentProperties(userId, searchRequest, pageable)

2. Service

- builds Specification:
  owner.id = userId
  AND city if provided
  AND status if provided
  AND price >= minPrice if provided
  AND price <= maxPrice if provided
- calls propertyRepository.findAll(spec, pageable)
- maps entities to PropertyListDTO
- returns Page<PropertyListDTO>

```
@Service
public class PropertySerive{
    ...

    public Page<PropertyListDTO> getAgentProperties(
        UUID agentId,
        PropertySearchRequestDTO request,
        Pageable pageable
    ) {
        Specification<Property> spec =
                PropertySpecifications.ownerIdEquals(agentId);

        if (request.getCity() != null) {
            spec = spec.and(PropertySpecifications.cityEquals(request.getCity()));
        }

        if (request.getStatus() != null) {
            spec = spec.and(PropertySpecifications.statusEquals(request.getStatus()));
        }

        if (request.getMinPrice() != null) {
            spec = spec.and(PropertySpecifications.priceGreaterThanOrEqual(request.getMinPrice()));
        }

        if (request.getMaxPrice() != null) {
            spec = spec.and(PropertySpecifications.priceLessThanOrEqual(request.getMaxPrice()));
        }

        Page<Property> properties =
                propertyRepository.findAll(spec, pageable);

        return properties.map(property -> new PropertyListDTO(
                property.getId(),
                property.getTitle(),
                property.getCity(),
                property.getPrice(),
                property.getStatus()
        ));
    }
}
```

3. Repository

```
public interface PropertyRepository
        extends JpaRepository<Property, UUID>,
                JpaSpecificationExecutor<Property> {

    propertyRepository.findAll(spec, pageable);
}
```

4. Entity relationship

```
@Entity
class Property{
    ...
    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;

}
```

```
@Entity
class User {
    ...
    @OneToMany(mappedBy="owner")
    private List<Property> properties;
}
```

5. Return type

Service should service return: Page<PropertyListDTO>, because API should return a controlled response shape, not JPA entities.

```
Repository returns Page<Property>
Service returns Page<PropertyListDTO>
Controller returns Page<PropertyListDTO>
```

6. Search DTO

```
public class PropertySearchDTO{
    private String city;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // getters
}
```

## Drill 2 - Managing viewings

**Feature**

```
Agent Dashboard - Viewings Management
```

An agent wants to see viewing requests for properties they own.

Endpoint:

```
GET /agent/viewings
```

**Requirements**

```
Only show viewings for properties owned by the authenticated agent.

Optional filters:
- status
- fromDate
- toDate

Support:
- pagination
- sorting

Return DTOs only.
```

**Domain Model**

Assume:

```
User
↓
owns many properties

Property
↓
has many Viewing
```

```
Agent A
 ├── Property 1
 │     ├── Viewing 1
 │     └── Viewing 2
 │
 └── Property 2
       └── Viewing 3
```

Agent A should see:

```
Viewing 1
Viewing 2
Viewing 3
```

but NOT viewings belonging to:

```
Agent B's properties
```

**Task**

1. DTO Design

```
public class ViewingSearchRequestDTO {
    private String status;
    private LocalDate fromDate;
    private LocalDate toDate;

    // getters
}
```

2. Controller

- gets authenticated agentId
- receives ViewingSearchRequestDTO
- receives Pageable
- calls viewingService.getAgentViewings(agentId, request, pageable)

```
@GetMapping("/agent/viewings")
public Page<ViewingListDTO> searchViewings(
    UUID userId,
    ViewingSearchRequestDTO request,
    Pageable pageable
){
    Page<ViewingListDTO> results = viewingService.getAgentPropertyViewings(userId, request, pageable);
}
```

3. Service

- base spec: viewing.property.owner.id = agentId
- add status if provided
- add requestedDateTime >= fromDate if provided
- add requestedDateTime <= toDate if provided
- call viewingRepository.findAll(spec, pageable)
- map Page<Viewing> to Page<ViewingListDTO>

```
@Service
public class ViewingService{
    ....

    public Page<ViewingListDTO> getAgentPropertyViewings(
        UUID userId,
        ViewingSearchRequestDTO request,
        Pageable pageable
    ){
        Specification<Viewing> spec = ViewingSpecifications.propertyOwnerIdEquals(userId);

        if(request.getStatus() != null){
            spec = spec.and(ViewingSpecifications.statusEquals(request.getStatus()));
        }

        if(request.getFromDate() != null){
            spec = spec.and(ViewingSpecifications.dateGreaterThanOrEquals(request.getFromDate()));
        }

        if(request.getToDate() != null){
            spec = spec.and(ViewingSpecifications.dateLessThanOrEquals(request.getToDate()));
        }

        Page<Viewing> viewings = viewingRepository.findAll(spec, pageable);

        return viewsing.map(viewing -> new ViewingListDTO(
            viewing.getId(),
            viewing.getProperty().getTitle(),
            viewing.getUser().getEmail(),
            viewing.requestedDateTime(),
            viewing.getStatus()
        ))
    }
}
```

4. Repository

- extends JpaRepository<Viewing, UUID>
- extends JpaSpecificationExecutor<Viewing>

```
public interface ViewingRepository
        extends JpaRepository<Viewing, UUID>,
                JpaSpecificationExecutor<Viewing> {
}
```

5. Entity Navigation

Viewing → Property → Owner(User)

```
@Entity
public class Viewing {
    ...
    @ManyToOne
    @JoinColumn(name="property_id")
    private Property property;
}

@Entity
public class Property {
    ...
    @OneToMany(mappedBy="property")
    Private List<Viewing> viewings;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;
}

@Entity
public class User {
    ...
    @OneToMany(mappedBy="owner")
    Private List<Property> properties;
}
```

6. Return Type

```
Page<ViewingListDTO>
```
