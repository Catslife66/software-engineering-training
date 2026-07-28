# Update A Property

Endpoint:

```
PUT /properties/{id}
```

with

```
{
  "title": "Luxury Penthouse",
  "city": "Edinburgh",
  "price": 650000,
  "bedrooms": 3,
  "version": 2
}
```

## 1. DTOs

### Request DTO

| - dto/request/UpdatePropertyRequest.java

```
public class UpdatePropertyRequest {
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

    @NotNull
    @PositiveOrZero
    private Long version;

    public String getTitle(){
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
    public Long getVersion(){
        return version;
    }
}
```

### Response DTO

| - dto/response/UpdatePropertyResponse.java

```
public class UpdatePropertyResponse {
    private final UUID id;
    private final String title;
    private final String city;
    private final BigDecimal price;
    private final Integer bedrooms;
    private final PropertyStatus status;
    private final LocalDateTime createdAt;
    private final Long version;

    public UpdatePropertyResponse(
        UUID id,
        String title,
        String city,
        BigDecimal price,
        Integer bedrooms,
        PropertyStatus status,
        LocalDateTime createdAt,
        Long version
    ){
        this.id = id;
        this.title = title;
        this.city = city;
        this.price = price;
        this.bedrooms = bedrooms;
        this.status = status;
        this.createdAt = createdAt;
        this.verstion = version;
    }

    // constructor and getters
}
```

## 2. Mappers

| - mapper/PropertyMapper.java

```
...
@Component
public class PropertyMapper {
    ...

    public void UpdateEntity(
        UpdatePropertyRequest request,
        Property property
    ){
        property.setTitle(request.getTitel());
        property.setCity(request.getCity());
        property.setPrice(request.getPrice());
        property.setBedrooms(request.getBedrooms());
    }

    public PropertyResponse toResponse(Property property) {
    return new PropertyResponse(
            property.getId(),
            property.getTitle(),
            property.getCity(),
            property.getPrice(),
            property.getBedrooms(),
            property.getStatus(),
            property.getCreatedAt(),
            property.getUpdatedAt(),
            property.getVersion()
        );
    }

}

```

## 3. Service

| - service/PropertyService.java

```
...

@Service
public class PropertyService{
    ...
    @Transactional
    public PropertyResponse updateProperty(
        UUID id,
        UpdatePropertyRequest request
    ){
        Property property = propertyRepository.findById(id)
                            .orElseThrow(() -> new PropertyNotFoundException(id));

        if (!property.getVersion().equals(request.getVersion())){
            throw new PropertyVersionConflictException(
                id,
                request.getVersion(),
                property.getVersion()
            );
        }

        propertyMapper.updateEntity(request, property);

        property savedProperty = propertyRepository.save(property);

        return propertyMapper.toResponse(savedProperty);
    }

}
```

## 4. Controller

| - controller/PropertyController.java

```
...

@RestController
@RequestMapping("/properties")
public class PropertyController {
    ...

    @PutMapping("{/id}")
    public ResponseEntity<PropertyResponse> updateProperty(
        @PathVariable UUID id,
        @Valid
        @RequestBody UpdatePropertyRequest request
    ){
        PropertyResponse response = propertyService.updateProperty(id, request);

        return ResponseEntity.ok(response);
    }

}
```

## 5. Exception Handler

| - exception/PropertyVersionConflictException.java

```
public class PropertyVersionConflictException extends RuntimeException{
    public PropertyVersionConflictException(
        UUID id,
        Long requestedVersion,
        Long currentVersion
    ){
        super(
                "Property " + id
                        + " was modified by another request. "
                        + "Requested version: " + requestedVersion
                        + ", current version: " + currentVersion
        );
    }
}

```

| - exception/GlobalExceptionHandler.java

```
...
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    ...

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingFailure(
        ObjectOptimisticLockingFailureException exception
    ) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Version conflict");
        response.put(
                "message",
                "The property was modified by another request. "
                        + "Reload it and try again."
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(PropertyVersionConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflictUpdate(
        PropertyVersionConflictException exception
    ){
        Map<String, Object> response = new HashMap<>();

         response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT().value());
        response.put("error", "Version conflict");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
```
