# Validation

Validation protects the application boundary from invalid input.

Suppose BrightMove receives:

```
{
  "title": "",
  "city": "",
  "price": -50000
}
```

Technically, this is valid JSON. Spring can deserialize it successfully.

But it is not valid business input.

We want the application to reject it before it reaches the service:

```
HTTP Request
     ↓
Deserialize JSON
     ↓
Request DTO
     ↓
VALIDATION
     ↓
Controller
     ↓
Service
```

**Reject structurally invalid input at the application boundary before business logic executes.**

## Core Concepts

Spring Boot validation is normally built on **Jakarta Bean Validation**.

We describe constraints directly on DTO fields:

```
public class CreatePropertyRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String city;

    @Positive
    private BigDecimal price;
}
```

Then the controller activates validation:

```
@PostMapping
public ResponseEntity<PropertyResponse> createProperty(
        @Valid @RequestBody CreatePropertyRequest request
) {
    ...
}
```

The two pieces have different responsibilities:

```
Validation annotations
        ↓
Define the rules

@Valid
        ↓
Trigger validation
```

## Mental Model

There are three different questions we must keep separate.

**Question 1 — Can Spring understand the request?**

```
{
  "price": "hello"
}
```

If price should be a BigDecimal, this is a **deserialization/type conversion problem**.

---

**Question 2 — Is the request structurally valid?**

```
{
  "title": "",
  "price": -100
}
```

Spring understands it, but the values violate our DTO constraints.

This is **Bean Validation**.

---

**Question 3 — Is the operation allowed by the business?**

Suppose:

```
property.status = SOLD
```

and somebody requests a new viewing.

The JSON may be perfectly valid.

But the business rule says:

```
A sold property cannot receive new viewings.
```

That belongs in the service, not DTO validation.

So:

```
Malformed/type-invalid input
↓
HTTP conversion layer

Structurally invalid input
↓
DTO validation

Business-invalid operation
↓
Service
```

This distinction is extremely important.

## Spring / Java Implementation

**Dependency**

Modern Spring Boot applications normally use:

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

This provides **Jakarta Bean Validation** support.

Modern imports use:

```
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
```

Older projects may contain javax.validation, but modern Spring Boot uses the `jakarta` namespace.

---

`@NotNull`

```
@NotNull
private BigDecimal price;
```

Requires:

```
price != null
```

But it doesn't apply string-specific rules.

For example, for a String, @NotNull alone would still allow:

```
""
"   "
```

---

`@NotEmpty`

For strings and collections:

```
@NotEmpty
private String title;
```

Rejects:

```
null
""
```

But whitespace such as:

```
"   "
```

can still pass.

---

`@NotBlank`

Designed for strings:

```
@NotBlank
private String title;
```

Rejects:

```
null
""
"   "
```

Therefore names, titles and cities commonly use @NotBlank.

---

`@Positive`

```
@Positive
private BigDecimal price;
```

Requires:

```
price > 0
```

There is also:

```
@PositiveOrZero
```

for:

```
value >= 0
```

---

`@Min` and `@Max`

For numeric bounds:

```
@Min(1)
@Max(20)
private Integer bedrooms;
```

---

`@Size`

Controls the size of strings, collections, arrays and similar values.

```
@Size(min = 5, max = 100)
private String title;
```

Or:

```
@Size(max = 10)
private List<String> imageUrls;
```

It concerns **size**, not numeric value.

---

`@Email`

```
@Email
private String email;
```

Checks whether the value has a valid email-like format.

Usually combine it with:

```
@NotBlank
@Email
private String email;
```

because these constraints answer different questions.

---

**Other useful constraints**

Examples include:

```
@Past
private LocalDate dateOfBirth;
```

```
@Future
private LocalDateTime viewingTime;
```

```
@Pattern(regexp = "...")
private String postcode;
```

```
@DecimalMin("0.01")
private BigDecimal price;
```

The important principle isn't memorizing every annotation.

It is understanding that constraints **describe the valid shape of input**.

---

**Custom Messages**

Instead of relying on default messages:

```
@NotBlank(message = "Title is required")
private String title;

@Positive(message = "Price must be greater than zero")
private BigDecimal price;
```

These messages can later be included in the API's error response.

---

`@Valid`

Defining constraints isn't enough.

Given:

```
public class CreatePropertyRequest {

    @NotBlank
    private String title;
}
```

the controller should activate validation:

```
@PostMapping
public ResponseEntity<PropertyResponse> createProperty(
        @Valid @RequestBody CreatePropertyRequest request
) {
    ...
}
```

Conceptually:

```
@RequestBody
     ↓
Create Java object

@Valid
     ↓
Validate Java object
```

If validation succeeds:

```
Controller method executes
        ↓
Service executes
```

If validation fails:

```
Validation exception
        ↓
Controller method does NOT execute normally
        ↓
Service does NOT receive invalid request
```

This is why validation belongs at the boundary.

---

**Nested Validation**

Suppose:

```
public class CreatePropertyRequest {

    @NotBlank
    private String title;

    @Valid
    @NotNull
    private AddressRequest address;
}
```

and:

```
public class AddressRequest {

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String postcode;
}
```

The `@Valid` on address tells the validator:

Also validate the object inside this field.

Without nested @Valid, validation does not necessarily cascade into the nested DTO.

So:

```
CreatePropertyRequest
       ↓
    address
       ↓
AddressRequest constraints
```

---

**Collection Validation**

The same principle applies to collections.

```
@Valid
@NotEmpty
private List<ImageRequest> images;
```

This can validate the contained `ImageRequest` objects as well.

---

**Custom Validation**

Standard annotations can't describe every rule.

Suppose BrightMove requires a property reference like:

```
BM-123456
```

We could create:

```
@ValidPropertyReference
private String reference;
```

A custom constraint normally consists of:

```
Custom annotation
       +
ConstraintValidator
```

The validator contains the validation logic.

Custom validators are useful when the rule concerns the structure/value of the input itself.

They should not become a substitute for service-layer business logic.

---

**Cross-Field Validation**

Sometimes one field cannot be validated independently.

Imagine a search request:

```
public class PriceRangeRequest {

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
```

Individually:

```
minPrice = 500000
maxPrice = 200000
```

might both be positive.

But together the request makes no sense:

```
minPrice > maxPrice
```

This is a cross-field constraint.

A class-level custom validator can check the relationship between the two fields.

Conceptually:

```
@ValidPriceRange
public class PriceRangeRequest {
    ...
}
```

The important distinction is that this still validates the request itself.

---

**DTO Validation vs Business Validation**

This deserves special attention.

Suppose we receive:

```
{
  "viewingTime": "2026-09-01T14:00:00"
}
```

We can validate that:

```
@Future
private LocalDateTime viewingTime;
```

That's DTO validation.

But suppose another customer has already booked that slot.

Checking that requires database/application state:

```
Does another Viewing exist at this time?

That belongs in the service.
```

For example:

```
if (viewingRepository.existsByPropertyIdAndTime(
        propertyId,
        request.getViewingTime())) {

    throw new ViewingSlotUnavailableException();
}
```

A useful rule is:

**If validating the rule requires application/database state, it usually belongs in the service rather than a DTO annotation.**

## Request & Data Flow

Let's trace an invalid BrightMove request.

Client sends:

```
{
  "title": "",
  "city": "Edinburgh",
  "price": -100
}
```

Spring/Jackson first deserializes it:

```
JSON
 ↓
CreatePropertyRequest
```

The resulting Java object conceptually contains:

```
title = ""
city = "Edinburgh"
price = -100
```

Then:

```
@Valid
```

triggers Bean Validation.

It discovers:

```
@NotBlank title
        ❌

@NotBlank city
        ✅

@Positive price
        ❌
```

Validation therefore fails.

The request does not continue into:

```
propertyService.createProperty(...)
```

Instead Spring raises a validation exception.

For `@Valid @RequestBody`, this is commonly:

```
MethodArgumentNotValidException
```

That exception can then be converted into a clean HTTP error response.

## BrightMove Example

A request DTO might be:

```
public class CreatePropertyRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "City is required")
    private String city;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Bedroom count is required")
    @Positive(message = "Bedroom count must be greater than zero")
    private Integer bedrooms;

    // getters/setters
}
```

Controller:

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
            @Valid @RequestBody CreatePropertyRequest request
    ) {
        PropertyResponse response =
                propertyService.createProperty(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
```

Notice how little validation code exists inside the controller itself.

We don't write:

```
if (request.getTitle() == null) {
    ...
}

if (request.getTitle().isBlank()) {
    ...
}

if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
    ...
}
```

Instead, the DTO declares its constraints and the validation framework enforces them.

## Common Mistakes

**Forgetting @Valid**

This:

```
@RequestBody CreatePropertyRequest request
```

deserializes the JSON but does not activate the expected request-body Bean Validation flow by itself.

Use:

```
@Valid @RequestBody CreatePropertyRequest request
```

---

**Using only @NotNull for strings**

```
@NotNull
private String title;
```

allows:

```
""
"   "
```

For required human-readable text, `@NotBlank` is normally more appropriate.

---

**Putting every business rule into annotations**

Avoid trying to validate database-dependent rules such as:

```
Property must exist.

Viewing slot must still be available.

Current user must own property.
```

Those are service/business concerns.

---

**Manual validation inside the controller**

Avoid:

```
if (request.getPrice() == null) {
    return ResponseEntity.badRequest()...
}
```

when Bean Validation can express the rule declaratively.

---

**Validating entities as the API contract**

Prefer:

```
Request DTO
↓
Validation
↓
Service
↓
Entity
```

rather than exposing a JPA entity directly as `@RequestBody`.

The entity models persistence/domain state.

The DTO models the API contract.

They are different responsibilities.

## Engineering Trade-offs

Declarative validation gives us:

- less repetitive code
- consistent constraints
- thin controllers
- reusable validation rules
- clear API contracts

But not every rule belongs there.

The most important architectural decision is deciding **where validation belongs**.

A useful three-layer model is:

```
HTTP / conversion validation
"Can I understand this input?"

        ↓

DTO validation
"Is this input structurally acceptable?"

        ↓

Service/business validation
"Is this operation allowed right now?"
```

For example:

```
"abc" supplied as price
→ HTTP conversion/deserialization

price = -500
→ DTO validation

property already sold
→ business validation

current user doesn't own property
→ authorization/business rule
```

This separation prevents controllers and DTO validators from gradually turning into business-logic layers.

## Summary

Spring validation protects the application boundary.

The core pattern is:

```
public class CreatePropertyRequest {

    @NotBlank
    private String title;

    @NotNull
    @Positive
    private BigDecimal price;
}
```

combined with:

```
public ResponseEntity<PropertyResponse> createProperty(
        @Valid @RequestBody CreatePropertyRequest request
)
```

The most important annotations we've covered are:

```
@NotNull
@NotEmpty
@NotBlank
@Positive
@PositiveOrZero
@Min / @Max
@Size
@Email
@Past / @Future
@Pattern
@Valid
```

The mental model to keep is:

```
Request
   ↓
Deserialize
   ↓
Validate DTO
   ↓
Controller
   ↓
Service
   ↓
Business rules
```

And the key architectural rule is:

**DTO validation protects the shape and basic validity of input; the service protects business invariants.**

When validation fails, Spring raises an exception rather than allowing invalid input to proceed into the service. How we convert that exception—and all our other application exceptions—into consistent HTTP responses belongs to the next fixed module.
