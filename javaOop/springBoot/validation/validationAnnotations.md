# Validation Annotations

## Missing Values

Suppose we have:

```
private String title;
```

There are three commonly confused annotations:

```
@NotNull

@NotEmpty

@NotBlank
```

They are not the same.

Example

Suppose the client sends:

Case A

```
{
    "title": null
}
```

Case B

```
{
    "title": ""
}
```

Case C

```
{
    "title": "     "
}
```

### What Each Annotation Does

`@NotNull`

Rejects:

```
null
```

Allows:

```
""
"     "
"House"
```

Think:

```
The object must exist.
```

`@NotEmpty`

Rejects:

```
null
""
```

Allows:

```
"     "
"House"
```

Notice something surprising:

```
"     "
```

is **not empty**.

It contains five characters (spaces).

`@NotBlank`

Rejects:

```
null
""
"     "
```

Allows:

```
"House"
```

Before checking the length, it effectively trims whitespace.

**Summary**

| Value     | `@NotNull` | `@NotEmpty` | `@NotBlank` |
| --------- | :--------: | :---------: | :---------: |
| `null`    |     ❌     |     ❌      |     ❌      |
| `""`      |     ✅     |     ❌      |     ❌      |
| `"     "` |     ✅     |     ✅      |     ❌      |
| `"House"` |     ✅     |     ✅      |     ✅      |

### A new principle

Think about the Java type first.

| Java Type                                    | Common Annotation           |
| -------------------------------------------- | --------------------------- |
| `String`                                     | `@NotBlank`                 |
| `Collection` / `List`                        | `@NotEmpty`                 |
| `BigDecimal`, `Integer`, `UUID`, `LocalDate` | `@NotNull`                  |
| Numbers                                      | `@Positive`, `@Min`, `@Max` |

The annotation depends on what you're validating.

### Combining Annotations

Here's something you'll do all the time:

```
public class CreatePropertyRequest {

    @NotBlank
    private String title;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotBlank
    private String city;

    @NotNull
    @Positive
    private Integer bedrooms;

    @NotEmpty
    private List<String> imageUrls;
}
```

Notice that fields can have multiple validation rules.

Example:

- Email must be provided and must be a valid email address.
- Password must be provided and contain between 8 and 50 characters.
- Age is optional, but if provided, it must be 18 or greater.

```
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 50)
    private String password;

    @Min(18)
    private Integer age;
}
```

## Validation Toolbox

Tool 1 - String Validation

| Annotation  | Purpose                | Example                |
| ----------- | ---------------------- | ---------------------- |
| `@NotBlank` | Must contain real text | Title, City, Name      |
| `@Email`    | Valid email format     | Email                  |
| `@Size`     | Length constraint      | Password, Username     |
| `@Pattern`  | Must match a regex     | Phone number, Postcode |

Tool 2 - Number Validation

| Annotation        | Purpose          | Example        |
| ----------------- | ---------------- | -------------- |
| `@NotNull`        | Value must exist | Price          |
| `@Positive`       | > 0              | Property price |
| `@PositiveOrZero` | ≥ 0              | Stock quantity |
| `@Min`            | Minimum value    | Age ≥ 18       |
| `@Max`            | Maximum value    | Rating ≤ 5     |

Tool 3 - Collection Validation

| Annotation  | Purpose                                      |
| ----------- | -------------------------------------------- |
| `@NotEmpty` | Collection must contain at least one element |

Example:

```
public class CreateViewingRequest {
    @NotNull
    private UUID propertyId;

    @NotNull
    private LocalDate viewingDate;

    @NotBlank
    @Email
    private String visitorEmail;

    @NotNull
    @Min(1)
    @Max(6)
    private Integer numberOfVisitors;

    @Size(max=500)
    private String notes;
}
```
