# REST API

The purpose of this module is to understand how Spring Boot exposes application functionality through HTTP.

A REST controller sits at the **boundary of our application**:

```
Client
   ↓
HTTP Request
   ↓
Controller
   ↓
Application
   ↓
Controller
   ↓
HTTP Response
   ↓
Client
```

The controller's main responsibilities are:

- receive HTTP requests
- extract input from the request
- pass that input to the appropriate service
- translate the service result into an HTTP response

It should not contain the application's business logic.

## Core Concepts

**REST API**

A REST API exposes resources through HTTP.

In BrightMove, examples of resources might be:

```
Property
Agent
Viewing
```

We identify those resources using URLs:

```
/properties
/properties/{id}
/agents
/viewings
```

HTTP methods describe what operation we want to perform.

| HTTP Method | Typical Meaning | Example         |
| ----------- | --------------- | --------------- |
| `GET`       | Read            | Get property    |
| `POST`      | Create          | Create property |
| `PUT`       | Replace/update  | Update property |
| `DELETE`    | Delete          | Delete property |

A typical CRUD API therefore looks like:

```
POST   /properties
GET    /properties
GET    /properties/{id}
PUT    /properties/{id}
DELETE /properties/{id}
```

## Mental Model

Think of a controller as an adapter between HTTP and Java.

The outside world speaks HTTP:

```
POST /properties

Content-Type: application/json

{
    "title": "City Centre Flat",
    "city": "Edinburgh",
    "price": 250000
}
```

Our service speaks Java:

```
propertyService.createProperty(request);
```

Spring MVC(Model-View-Controller) performs much of the translation between these worlds.

```
HTTP
│
│ JSON
▼
Controller
│
│ Java objects
▼
Service
```

And on the way back:

```
Service
│
│ Java object
▼
Controller
│
│ JSON + HTTP status
▼
Client
```

## Spring / Java Implementation

`@RestController`

A REST controller is declared using:

```
@RestController
public class PropertyController {
}
```

@RestController tells Spring:

```
This class contains HTTP endpoints, and returned values should normally be written into the HTTP response body.
```

Conceptually, it combines:

```
@Controller
@ResponseBody
```

`@RequestMapping`

We normally give the controller a base URL:

```
@RestController
@RequestMapping("/properties")
public class PropertyController {
}
```

Every endpoint inside this controller now starts with:

```
/properties
```

`@GetMapping`

Used for GET requests.

```
@GetMapping
public List<PropertyResponse> getProperties() {
    return propertyService.getProperties();
}
```

This handles:

```
GET /properties
```

`@PostMapping`

Used to create resources.

```
@PostMapping
public ResponseEntity<PropertyResponse> createProperty(
        @RequestBody CreatePropertyRequest request
) {
    PropertyResponse response =
            propertyService.createProperty(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
}
```

This handles:

```
POST /properties
```

`@PutMapping`

A typical update endpoint might look like:

```
@PutMapping("/{id}")
public PropertyResponse updateProperty(
        @PathVariable UUID id,
        @RequestBody UpdatePropertyRequest request
) {
    return propertyService.updateProperty(id, request);
}
```

Here we have information coming from two different parts of HTTP:

```
PUT /properties/{id}
                ↓
          @PathVariable

JSON body
    ↓
@RequestBody
```

Both become normal Java values before entering the service.

`@DeleteMapping`

Example:

```
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteProperty(
        @PathVariable UUID id
) {
    propertyService.deleteProperty(id);

    return ResponseEntity.noContent().build();
}
```

The response is:

```
204 No Content
```

`Void` means there is no response body.

`@PathVariable`

Sometimes information is part of the URL itself.

For example:

```
GET /properties/550e8400-e29b-41d4-a716-446655440000
```

The ID identifies which property we want.

Controller:

```
@GetMapping("/{id}")
public PropertyResponse getProperty(
        @PathVariable UUID id
) {
    return propertyService.getPropertyById(id);
}
```

Spring extracts the value from the URL and converts it into a UUID.

Conceptually:

```
/properties/{id}
              ↓
           UUID id
```

You can also explicitly specify the name:

```
@PathVariable("id") UUID propertyId
```

`@RequestParam`

Query parameters are generally used to modify or filter a request.

Example:

```
GET /properties?city=Edinburgh
```

Controller:

```
@GetMapping
public List<PropertyResponse> getProperties(
        @RequestParam String city
) {
    return propertyService.findByCity(city);
}
```

Spring extracts:

```
city=Edinburgh
```

into:

```
String city
```

Query parameters can also be optional:

```
@RequestParam(required = false) String city
```

`@RequestBody`

@RequestBody tells Spring to deserialize the HTTP request body into a Java object.

Suppose the client sends:

```
{
  "title": "City Centre Flat",
  "city": "Edinburgh",
  "price": 250000
}
```

We have:

```
public class CreatePropertyRequest {

    private String title;
    private String city;
    private BigDecimal price;

    // getters/setters
}
```

Then:

```
@PostMapping
public ResponseEntity<PropertyResponse> createProperty(
        @RequestBody CreatePropertyRequest request
) {
    ...
}
```

Spring uses Jackson to convert:

```
JSON
↓
CreatePropertyRequest
```

This is called **deserialization**.

For the response, the reverse happens:

```
PropertyResponse
↓
JSON
```

which is **serialization**.

## DTOs at the HTTP Boundary

We generally don't want:

```
@PostMapping
public Property createProperty(
        @RequestBody Property property
)
```

where Property is our JPA entity.

Instead:

```
@RequestBody CreatePropertyRequest request
```

and return:

```
PropertyResponse
```

This separates:

```
API representation
        ↓
DTO

Database representation
        ↓
Entity
```

The API should not be tightly coupled to the database model.

**ResponseEntity**

Sometimes returning an object directly is enough:

```
@GetMapping("/{id}")
public PropertyResponse getProperty(
        @PathVariable UUID id
) {
    return propertyService.getPropertyById(id);
}
```

Spring will normally return:

```
200 OK
```

But sometimes we want explicit control over the HTTP response.

That's where `ResponseEntity<T>` is useful.

Example:

```
@PostMapping
public ResponseEntity<PropertyResponse> createProperty(
        @RequestBody CreatePropertyRequest request
) {
    PropertyResponse response =
            propertyService.createProperty(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
}
```

Now we're explicitly returning:

```
HTTP 201 Created
+
PropertyResponse
```

## HTTP Status Codes

A REST API should communicate not only data but also the result of the operation.

Important codes include:

| Status                      | Meaning                                  |
| --------------------------- | ---------------------------------------- |
| `200 OK`                    | Successful request                       |
| `201 Created`               | Resource successfully created            |
| `204 No Content`            | Successful request with no response body |
| `400 Bad Request`           | Invalid request                          |
| `401 Unauthorized`          | Authentication required/failed           |
| `403 Forbidden`             | Authenticated but not permitted          |
| `404 Not Found`             | Resource does not exist                  |
| `409 Conflict`              | Request conflicts with current state     |
| `500 Internal Server Error` | Unexpected server failure                |

For example:

```
POST /properties
→ 201 Created

GET /properties/{id}
→ 200 OK

DELETE /properties/{id}
→ 204 No Content
```

## Request & Data Flow

Let's trace a complete create request.

Client sends:

```
POST /properties

{
    "title": "City Centre Flat",
    "city": "Edinburgh",
    "price": 250000
}
```

Spring receives the HTTP request.

The request mapping system finds:

```
@PostMapping
```

Spring/Jackson converts the JSON into:

```
CreatePropertyRequest
```

The controller calls:

```
propertyService.createProperty(request);
```

The service performs the application workflow.

Eventually it returns:

```
PropertyResponse
```

The controller wraps it:

```
ResponseEntity
    .status(HttpStatus.CREATED)
    .body(response);
```

Jackson serializes the DTO to JSON.

The client receives:

```
HTTP 201 Created

{
    "id": "...",
    "title": "City Centre Flat",
    "city": "Edinburgh",
    "price": 250000
}
```

So the entire boundary flow is:

```
HTTP request
    ↓
URL + method select controller method
    ↓
JSON deserialization
    ↓
Request DTO
    ↓
Controller
    ↓
Service
    ↓
Response DTO
    ↓
Controller
    ↓
JSON serialization
    ↓
HTTP response
```

## BrightMove Example

A clean controller could look like:

```
@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(
            PropertyService propertyService
    ) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(
            @RequestBody CreatePropertyRequest request
    ) {
        PropertyResponse response = propertyService.createProperty(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getProperty(
            @PathVariable UUID id
    ) {
        PropertyResponse response = propertyService.getPropertyById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable UUID id,
            @RequestBody UpdatePropertyRequest request
    ) {
        PropertyResponse response = propertyService.updateProperty(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable UUID id
    ) {
        propertyService.deleteProperty(id);

        return ResponseEntity.noContent().build();
    }
}
```

Notice what is not here:

```
SQL
repository calls
business rules
entity manipulation
transaction logic
```

The controller is thin.

Its job is primarily:

```
HTTP
↕
Application
```

## Common Mistakes

**Business logic inside controllers**

Avoid:

```
@PostMapping
public PropertyResponse createProperty(...) {

    if (...) {
        ...
    }

    propertyRepository.save(...);

    ...
}
```

That mixes HTTP handling, business logic and persistence.

Prefer:

```
propertyService.createProperty(request);
```

**Controller calling repository directly**

Avoid:

```
Controller
    ↓
Repository
```

Our normal architecture is:

```
Controller
    ↓
Service
    ↓
Repository
```

The service owns the business workflow.

**Returning entities directly**

Avoid exposing:

```
Property
```

as the API contract.

Prefer:

```
PropertyResponse
```

This prevents persistence concerns and relationships from leaking into the API.

**Returning ResponseEntity from the service**

Avoid:

```
public ResponseEntity<PropertyResponse> createProperty(...)
```

inside PropertyService.

ResponseEntity is an HTTP concern.

The service should normally return:

```
PropertyResponse
```

Then the controller decides:

```
201 Created
200 OK
204 No Content
```

This preserves the boundary between:

```
Service → application/business concerns
Controller → HTTP concerns
```

## Engineering Trade-offs

A very thin controller gives us strong separation of concerns.

The controller knows about:

```
HTTP
DTOs
status codes
```

The service knows about:

```
business workflows
business rules
repositories
```

This improves:

```
testability
maintainability
reuse
separation of concerns
```

There is a trade-off: for very tiny applications, several layers can initially look like unnecessary boilerplate.

But as business complexity grows, those boundaries become increasingly valuable.

## Summary

The controller is the application's HTTP boundary.

The key annotations are:

```
@RestController
@RequestMapping
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
@RequestBody
@PathVariable
@RequestParam
```

The main mental model is:

```
HTTP
   ↓
Controller
   ↓
Service
   ↓
Application
```

and back:

```
Application
   ↓
Service result
   ↓
Controller
   ↓
HTTP response
```

`@RequestBody` converts JSON into Java objects.

`@PathVariable` extracts values from the URL path.

`@RequestParam` extracts query parameters.

`ResponseEntity` gives the controller explicit control over HTTP status and response body.

`DTOs` protect the API boundary from the persistence model.
