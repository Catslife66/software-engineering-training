# Create a Viewing for a Property

Endpoint:

```
POST /properties/{propertyId}/viewings
```

Example request:

```
{
  "requestedDateTime": "2026-08-10T10:00:00"
}
```

The workflow is:

```
Receive propertyId + request
        ↓
Find Property
        ↓
If missing → 404
        ↓
Create Viewing
        ↓
Property.addViewing(viewing)
        ↓
Cascade persists Viewing
        ↓
Commit transaction
        ↓
Return ViewingResponse
```

## Step 1 - Request DTO

|- dto/request/CreateViewingRequest.java

```
public class CreateViewingRequest{
    @NotNull
    private LocalDateTime requestedDateTime;

    public CreateViewingRequest(LocalDateTime requestedDateTime){
        this.requestedDateTime = requestedDateTime;
    }
}
```

## Step 2 — Response DTO

|- dto/response/ViewingResponse.java

```
public class ViewingResponse{
    private final UUID id;
    private final LocalDateTime requestedDateTime;
    private final ViewingStatus status;

    public ViewingResponse(UUID id, LocalDateTime requestedDateTime, ViewingStatus status){
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

## Step 3 — Viewing mapper

|- mapper/ViewingMapper.java

```
public class ViewingMapper {
    public Viewing toEntity(CreateViewingRequest request){
        return new Viewing(request.getRequestedDateTime());
    }

    public ViewingResponse toResponse(Viewing viewing){
        return new CreateViewingResponse(
            viewing.getId(),
            viewing.getRequestedDateTime(),
            viewing.getStatus()
        )
    }
}
```

## Step 4 — Service workflow

| - service/ViewingService.java

```
@Service
public class ViewingService {
    private final PropertyRepository propertyRepository;
    private final ViewingMapper viewingMapper;

    public ViewingService(
        PropertyRepository propertyRepository,
        ViewingMapper viewingMapper
    ){
        this.propertyRepository = propertyRepository;
        this.viewingMapper = viewingMapper;
    }

    @Transactional
    public ViewingResponse createViewing(
        UUID propertyId,
        CreateViewingRequest request
    ){
        Property property = propertyRepository.findById(propertyId)
                            .orElseThrow(() -> new PropertyNotFoundException(propertyId));

        Viewing viewing = viewingMapper.toEntity(request);

        property.addViewing(viewing);

        return viewingMapper.toResponse(viewing);
    }
}
```

## Step 5 - Controller

|- controller/ViewingController.java

```
@RestController
public class ViewingController{
    private final ViewingService viewingService;

    public ViewingController(ViewingService viewingService){
        this.viewingService = viewingService;
    }

    @PostMapping("/{propertyId}/viewings")
    public ResponseEntity<ViewingResponse> createViewing(
        @PathVariable UUID propertyId,
        @Valid @RequestBody CreateViewingRequest request
    ){
        ResponseEntity<ViewingResponse> response = viewingService.createViewing(
            UUID propertyId,
            CreateViewingRequest request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
```
