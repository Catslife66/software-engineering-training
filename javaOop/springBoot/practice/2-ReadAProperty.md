# Read A Property By ID

Endpoint:

```
GET /properties/{id}
```

Request Flow:

```
HTTP request
    ↓
Controller receives UUID
    ↓
Service asks repository for property
    ↓
Property found?
   ↙       ↘
 yes       no
 ↓         ↓
map DTO    throw PropertyNotFoundException
 ↓         ↓
200 OK     GlobalExceptionHandler → 404
```

## 1. DTOs

### Response DTO

| - dto/response/PropertyResponse.java

```
import com.brightmove.enums.PropertyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PropertyResponse {
    private final UUID id
    private final String title;
    private final String city;
    private final BigDecimal price;
    private final Integer bedrooms;
    private final PropertyStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PropertyResponse(
        UUID id,
        String title,
        String city,
        BigDecimal price,
        Integer bedrooms,
        PropertyStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.city = city;
        this.price = price;
        this.bedrooms = bedrooms;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
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
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
```

## 2. Exceptions

| - exception/PropertyNotFoundException.java

```
public class PropertyNotFoundException extends RuntimeException {
    public PropertyNotFoundException(UUID id){
        super("Property not found with id:" + id);
    }
}
```

| - exception/GlobalExceptionHandler.java

```
...

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
       ...
    }

    // catch invalid UUID request
    ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Invalid request");
        response.put(
                "message",
                "Invalid value for '" + exception.getName() + "': "+ exception.getValue()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    // catch property not found
    @ExceptionHandler(PropertyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyNotFound(
        PropertyNotFoundException exception
    ){
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND().value());
        response.put("error", "Property not found");
        response.put("message", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }



}
```

## 3. Mapper

| - mapper/PropertyMapper.java

```
import dto.request.CreatePropertyRequest;
import dto.response.CreatePropertyResponse;
import entity.Property;

@Component
public class PropertyMapper {

    public Property toEntity(CreatePropertyRequest request) {
       ...
    }

    public CreatePropertyResponse toCreateResponse(Property property) {
        ...
    }

    public PropertyResponse toResponse(Property property){
        return new PropertyResponse(
            property.getId(),
            property.getTitle(),
            property.getCity(),
            property.getPrice(),
            property.getBedrooms(),
            property.getStatus(),
            property.getCreatedAt(),
            property.getUpdatedAt()
        );
    }
}
```

## 4. Service

| - service/PropertyService.java

```
...

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

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID id){
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException(id));

        return propertyMapper.toResponse(property);
    }
}
```

## 5. Controller

| - controller/PropertyController.java

```
@RestController
@RequestMapping("/properties")
public class PropertyController {
    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }
    ...

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(
        @PathVariable UUID id
    ){
        PropertyResponse response = propertyService.getPropertyById(id);

        return ResponseEntity.ok(response);
    }

}
```
