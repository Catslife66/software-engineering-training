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

## 2. DTOs

### Request DTO

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

### Response DTO

| - dto/response/CreatePropertyResponse.java

```
public class CreatePropertyResponse {
    private final UUID id;
    private final String title;
    private final String city;
    private final BigDecimal price;
    private final Integer bedrooms;
    private final PropertyStatus status;
    private final LocalDateTime createdAt;

    public CreatePropertyResponse(
        UUID id,
        String title,
        String city,
        BigDecimal price,
        Integer bedrooms,
        PropertyStatus status,
        LocalDateTime createdAt,
    ){
        this.id = id;
        this.title = title;
        this.city = city;
        this.price = price;
        this.bedrooms = bedrooms;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId(){
        return id;
    }
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
    public PropertyStatus getStatus() {
        return status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
```

## 3. Mappers

| - mapper/PropertyMapper.java

```
import dto.request.CreatePropertyRequest;
import dto.response.CreatePropertyResponse;
import entity.Property;

@Component
public class PropertyMapper {

    public Property toEntity(CreatePropertyRequest request) {
        return new Property(
            request.getTitle(),
            request.getCity(),
            request.getPrice(),
            request.getBedrooms()
        );
    }

    public CreatePropertyResponse toCreateResponse(Property property) {
        return new CreatePropertyResponse(
            property.getId(),
            property.getTitle(),
            property.getCity(),
            property.getPrice(),
            property.getBedrooms(),
            property.getStatus(),
            property.getCreatedAt()
        );
    }
}
```

## 4. Service

1. Receive an already validated CreatePropertyRequest.
2. Construct a new Property entity.
3. Save it through PropertyRepository.
4. Map the saved entity to CreatePropertyResponse.
5. Return the response.

| - service/PropertyService.java

```
import dto.request.CreatePropertyRequest;
import dto.response.CreatePropertyResponse;
import entity.Property;
import mapper.PropertyMapper;
import repository.PropertyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PropertyService{
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    public PropertyService(
        PropertyRepository propertyRepository,
        PropertyMapper propertyMapper
    ){
        this.propertyRepository = propertyRepository;
        this.propertyMapper = propertyMapper;
    }

    @Transactional
    public CreatePropertyResponse createProperty(
        CreatePropertyRequest request
    ){
        Property property = propertyMapper.toEntity(request);
        Property savedProperty = propertyRepository.save(property);

        return propertyMapper.toCreateResponse(savedProperty);
    }

}
```

## 5. Repository

| - repository/PropertyRepository.java

```
import entity.Property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertyRepository
        extends JpaRepository<Property, UUID>,
                JpaSpecificationExecutor<Property>{

}

```

## 6. Controller

```
@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<CreatePropertyResponse> createProperty(
            @Valid @RequestBody CreatePropertyRequest request
    ) {
        CreatePropertyResponse response =
                propertyService.createProperty(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
```

## The complete request path

```
POST /properties
        ↓
@RequestBody converts JSON into CreatePropertyRequest
        ↓
@Valid performs structural validation
        ↓
PropertyController
        ↓
PropertyService.createProperty()
        ↓
PropertyMapper converts request DTO to entity
        ↓
PropertyRepository saves entity
        ↓
PropertyMapper converts saved entity to response DTO
        ↓
Controller returns 201 Created
```

## 7. Global Exception Handler

| - exception/GlobalExceptionHandler.java

```
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> validationErrors = new HashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            validationErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");
        response.put("fieldErrors", validationErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
```
