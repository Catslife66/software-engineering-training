# Validation

Suppose BrightMove has this endpoint:

```
POST /properties
```

Request:

```
{
    "title": "",
    "price": -500,
    "city": null
}
```

Question:

Should this request reach the Service layer?

Answer: 👉🏻 Controller

## Two Types of Validation

This distinction is extremely important.

**1. Request Validation**

Structural Validation -> Checks whether the request is structurally valid.

Examples:

```
Email format
Title not blank
Price positive
Phone length
Password length
```

These are about:

```
Is the request well-formed?
```

Usually handled by the controller.

**2. Business Validation**

Business Validation -> Checks whether the requested action is allowed.

Examples:

```
Property belongs to agent
Viewing slot available
Property already sold
Booking already exists
User has permission
```

These are about:

```
Is this business action allowed?
```

These belong in the service.

## @Valid

Suppose we have:

```
public class CreatePropertyRequest {

    private String title;

    private BigDecimal price;

    private String city;
}
```

Question:

How do we tell Spring:

```
title cannot be blank

price must be positive

city is required
```

The answer is:

```
Bean Validation
```

using annotations such as:

```
@NotBlank
@NotNull
@Positive
@Email
@Size
```

Now We Translate Rules into Annotations

Spring's Bean Validation gives us a direct mapping.

```
public class CreatePropertyRequest {

    @NotBlank
    private String title;

    @Positive
    private BigDecimal price;

    @NotBlank
    private String city;

    @Positive
    private Integer bedrooms;
}
```

**Without @Valid**

Suppose we have:

```
@PostMapping
public void createProperty(
    CreatePropertyRequest request
) {
    propertyService.create(request);
}
```

And the DTO is:

```
public class CreatePropertyRequest {

    @NotBlank
    private String title;

    @Positive
    private BigDecimal price;
}
```

Request:

```
{
    "title": "",
    "price": -100
}
```

What happens?

```
Spring creates the DTO.

No validation runs.

Controller executes.

Service executes.
```

The annotations are simply ignored.

**With @Valid**

Now we write:

```
@PostMapping
public void createProperty(
    @Valid CreatePropertyRequest request
) {
    propertyService.create(request);
}
```

Now the flow becomes:

```
HTTP Request
      ↓
Spring creates DTO
      ↓
@Valid
      ↓
Bean Validation runs
      ↓
Any violations?
```

If No:

↓

```
Controller executes
```

If Yes:

↓

```
Controller is NOT called
```

The request is rejected immediately.

**One Small Detail**

You'll often see:

```
@PostMapping
public ResponseEntity<PropertyDTO> create(
    @Valid @RequestBody CreatePropertyRequest request
)
```

Notice:

```
@Valid
@RequestBody
```

They do different jobs.

`@RequestBody` says:

Convert the JSON request into a Java object.

`@Valid` says:

Now validate that Java object using the annotations on it.

These annotations work together, but they have completely different responsibilities.

## Request Lifecycle

```
Client
    │
    ▼
HTTP Request
    │
    ▼
@RequestBody converts JSON into DTO
    │
    ▼
@Valid runs structual validation
    │
    ▼
Validation Failed
    │
    ▼
400 Bad Request
```

**Real Example**

Client sends:

```
{
    "email": "abc",
    "password": "12"
}
```

Server responds:

```
400 Bad Request
```

Body:

```
{
    "code": "VALIDATION_ERROR",
    "errors": [
        {
            "field": "email",
            "message": "must be a valid email address"
        },
        {
            "field": "password",
            "message": "must be at least 8 characters"
        }
    ]
}
```

## Where Does This Response Come From?

This is the interesting part.

Our controller is simply:

```
@PostMapping
public void createProperty(
    @Valid @RequestBody CreatePropertyRequest request
) {
    propertyService.create(request);
}
```

There is no code like:

```
if (validationFailed) {
    return ResponseEntity.badRequest();
}
```

So who creates the `400 Bad Request`?

The answer is:

```
Spring throws a validation exception.
```

Then:

```
Exception Handling
```

takes over.

This is the perfect bridge to the next topic.
