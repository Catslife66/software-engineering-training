# Cancel and Remove a Viewing

Business requirement:

```
A viewing is cancelled permanently.
It should no longer exist in the database.
The property itself must remain.
```

## Service

| - service/ViewingService.java

```
@Service
public class ViewingService {
    ...

    @Transactional
    public void deleteViewing(UUID propertyId, UUID viewingId){
        Property property = propertyRepository.findById(propertyId)
                            .orElseThrow(() -> new PropertyNotFoundException(propertyId));

        View viewing = property.getViewings()
                                .stream()
                                .filter(item -> item.getId().equals(viewingId))
                                .findFirst()
                                .orElseThrow(() -> new ViewingNotFoundException(viewingId));

        property.removeViewing(viewing);
    }
}
```

## Controller

|- controller/ViewingController.java

```
@RestController
public class ViewingController{
    ...

    @DeleteMapping("/{propertyId}/viewings/{viewingId}")
    public ResponseEntity<Void> deleteViewing(
            @PathVariable UUID propertyId,
            @PathVariable UUID viewingId
    ) {
        viewingService.deleteViewing(propertyId, viewingId);

        return ResponseEntity.noContent().build();
    }
}
```

## Exception Handler

| - exception/ViewingNotFoundException.java

```
public class ViewingNotFoundException extends RuntimeException {

    public ViewingNotFoundException(UUID id) {
        super("Viewing not found with id: " + id);
    }
}
```

| - exception/GlobalExceptionHandler.java

```
@RestControllerAdvice
public class GlobalExceptionHandler {
    ...

    @ExceptionHandler(ViewingNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleViewingNotFound(
        ViewingNotFoundException exception
    ) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Viewing not found");
        response.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
}
```
