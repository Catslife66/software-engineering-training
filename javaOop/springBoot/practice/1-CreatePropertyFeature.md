# Create Property Feature

Goal:

```
POST /properties
```

Request:

```
{
  "title": "Modern Flat",
  "city": "Edinburgh",
  "price": 250000,
  "bedrooms": 2
}
```

Backend structure:

```
controller
dto
entity
repository
service
exception
```

## 1. Create Entity

|- entity/Property.java

```
@Entity
@Table(name = "properties")
public class Property {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer bedrooms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status = PropertyStatus.AVAILABLE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected Property() {
    }

    public Property(String title, String city, BigDecimal price, Integer bedrooms) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.city = city;
        this.price = price;
        this.bedrooms = bedrooms;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getCity() { return city; }
    public BigDecimal getPrice() { return price; }
    public Integer getBedrooms() { return bedrooms; }
    public PropertyStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
```

|- enums/PropertyStatus.java

```
public enum PropertyStatus {

    AVAILABLE,
    SOLD,
    WITHDRAWN

}
```

Key points:

```
@Entity      → Java class maps to database table
@Table       → table name is properties
@Id          → primary key
@Column      → normal database columns
protected constructor → required by JPA
public constructor → used by our service
```

### Why we need protected constructor?

When Hibernate reads from the database:

```
Database
↓
Property Row
↓
Java Object
```

It needs to create:

```
new Property();
```

before filling in:

```
title
city
price
...
```

Hibernate cannot call:

```
new Property(title, city, ...)
```

because it doesn't know those values until after it has created the object.

So it needs:

```
protected Property() {
}
```

## 2. Request DTO

To define the shape and structural validation rules for POST /properties.

| - dto/request/CreatePropertyRequest.java

```
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreatePropertyRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String city;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotNull
    @Positive
    private Integer bedrooms;

    public String getTitle() {
        return title;
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }
}
```
