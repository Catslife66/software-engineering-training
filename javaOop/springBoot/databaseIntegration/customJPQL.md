# Custom JPQL

JPQL looks like SQL, but it uses entity names and field names, not table names and column names.

SQL thinks:

```
SELECT p.title, p.price, u.name
FROM properties p
JOIN users u ON p.owner_id = u.id
```

JPQL thinks:

```
SELECT p.title, p.price, p.owner.name
FROM Property p
```

Because `owner` is already a Java relationship.

## DTO projection with @Query

Suppose we have:

```
public class PropertyListDTO {
    private String title;
    private BigDecimal price;
    private String city;
    private String ownerName;

    public PropertyListDTO(String title, BigDecimal price, String city, String ownerName) {
        this.title = title;
        this.price = price;
        this.city = city;
        this.ownerName = ownerName;
    }

    // getters
}
```

Repository:

```
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    @Query("""
        SELECT new com.example.demo.dto.PropertyListDTO(
            p.title,
            p.price,
            p.city,
            p.owner.name
        )
        FROM Property p
        WHERE p.status = :status
    """)
    List<PropertyListDTO> findPropertyListByStatus(String status);
}
```

Important idea:

```
SELECT new ...DTO(...)
```

means Hibernate creates DTO objects directly from selected fields.

No full `Property` + full `User` entity loading needed.

## Why use @Query?

Use it when:

- query derivation becomes too long
- you need joins
- you need DTO projections
- you need aggregation/counts
- you want precise control over fetched data

## Mental Model

```
SQL navigates tables.
JPQL navigates objects.

SQL navigates foreign keys.
JPQL navigates object relationships.
```

## Aggregation Queries

```
How many properties does each agent own?
How many viewings does a property have?
How many bookings were made this month?
```

These use:

```
COUNT
SUM
AVG
GROUP BY
```

Java return type

```
COUNT(p) -> Long

AVG(p.price) -> BigDecimal
```

So the repository method would usually be:

```
@Query("""
    SELECT COUNT(p)
    FROM Property p
""")
Long countProperties();
```

## First Aggregation Query

Suppose we want:

How many properties does each owner have?

Conceptually:

```
Alice -> 3
Bob   -> 5
Carol -> 1
```

JPQL:

```
SELECT p.owner, COUNT(p)
FROM Property p
GROUP BY p.owner
```

Notice something interesting:

We're no longer returning:

```
Property

or

List<Property>
```

We're returning:

```
Owner + Count
```

multiple values together.

What Is The Query Returning? 👇

## Modern Approach: DTO Projection

We usually create:

```
public class OwnerPropertyCountDTO {

    private String ownerEmail;
    private Long propertyCount;

    public OwnerPropertyCountDTO(
        String ownerEmail,
        Long propertyCount
    ) {
        this.ownerEmail = ownerEmail;
        this.propertyCount = propertyCount;
    }
}
```

Then:

```
@Query("""
    SELECT new com.example.dto.OwnerPropertyCountDTO(
        p.owner.email,
        COUNT(p)
    )
    FROM Property p
    GROUP BY p.owner.email
""")
List<OwnerPropertyCountDTO> countPropertiesPerOwner();
```

Now every result row becomes:

```
OwnerPropertyCountDTO
```

which is much cleaner.

Example:

We want this result:

```
City          Property Count
----------------------------
London        120
Manchester    85
Edinburgh     40
```

So we create a DTO.

```
public class CityPropertyCountDTO {

    private String city;
    private Long propertyCount;

    public CityPropertyCountDTO(String city, Long propertyCount) {
        this.city = city;
        this.propertyCount = propertyCount;
    }

    public String getCity() {
        return city;
    }

    public Long getPropertyCount() {
        return propertyCount;
    }
}
```

Repository:

```
@Query("""
    SELECT new com.example.demo.dto.CityPropertyCountDTO(
        p.city,
        COUNT(p)
    )
    FROM Property p
    GROUP BY p.city
""")
List<CityPropertyCountDTO> countPropertiesByCity();
```

More Repository example:

```
@Query("""
    SELECT new CityAveragePriceDTO(
        p.city,
        AVG(p.price)
    )
    FROM Property p
    GROUP BY p.city
    HAVING AVG(p.price) > :minAverage
""")
List<CityAveragePriceDTO>
findCitiesWithAveragePriceAbove(
    BigDecimal minAverage
);
```

## Important Engineering Lesson

Whenever a query returns:

```
Entity
```

you can return:

```
Property
User
Viewing
```

Whenever a query returns:

```
Custom shape
```

such as:

```
Owner + Count
City + Average Price
Status + Count
```

you usually create:

```
DTO
```
