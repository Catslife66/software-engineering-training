# Java Fluency for Spring Engineers

## 1 — Lambda Expressions

Lambda's responsibility

```
For ONE object,
tell me what to do.
```

Suppose I ask you to find the viewing with a specific ID.

Without lambdas, you already wrote something very close:

```
Viewing found = null;

for (Viewing viewing : property.getViewings()) {
    if (viewing.getId().equals(viewingId)) {
        found = viewing;
        break;
    }
}
```

Instead of writing the whole loop, modern Java lets us package that question into a tiny function.

```
item -> item.getId().equals(viewingId)
```

In programming, we constantly ask questions about objects - yes/no.

Modern Java packages those questions into reusable functions called **predicates**.

Predicate = lambda returning true/false

## 2 — Stream API

Stream's responsibility

```
Loop through every object.
```

Imagine this list:

```
List<Property> properties;
```

Suppose you want:

```
Find all properties with at least three bedrooms.
```

**Old Java**

```
List<Property> result = new ArrayList<>();

for (Property property : properties) {
    if (property.getBedrooms() >= 3) {
        result.add(property);
    }
}
```

**Modern Java**

```
properties.stream()
        .filter(property -> property.getBedrooms() >= 3)
```

A list of titles of all properties in Edinburgh:

```
properties.stream() // "I want to process it."
        .filter(property -> property.getCity().equals("Edinburgh")) // "I only want these."
        .map(property -> property.getTitle()) // "I only need this information."
        .toList(); // "Give me the final result."
```

`anyMatch()` - Need one YES.

```
boolean exists = properties.stream()
        .anyMatch(property ->
                property.getCity().equals("Edinburgh"));
```

`allMatch()` - Need every YES.

```
properties.stream()
        .allMatch(property ->
                property.getStatus()
                        == PropertyStatus.AVAILABLE);
```

`noneMatch()` - Need zero YES.

```
properties.stream()
        .noneMatch(property ->
                property.getStatus()
                        == PropertyStatus.SOLD);
```

| Business Question | Stream Operation |
| ----------------- | ---------------- |
| Keep some objects | `filter()`       |
| Transform objects | `map()`          |
| Find one object   | `findFirst()`    |
| Is there one?     | `anyMatch()`     |
| Are they all?     | `allMatch()`     |
| Are there none?   | `noneMatch()`    |

## 3 - Method References

Earlier you wrote:

```
property -> property.getTitle()
```

Later we'll write:

```
property -> property.getCity()
```

or

```
viewing -> viewing.getStatus()
```

These are all doing exactly the same thing: Call one method.

Modern Java has an even shorter syntax.

This is called a **method reference**.

Instead of:

```
property -> property.getTitle()
```

we can write:

```
Property::getTitle
```

```
Viewing::getStatus
```

For example:

```
properties.stream()
        .map(Property::getTitle)
        .toList();
```

**When can we NOT use a method reference?**

Suppose we write:

```
property ->
    property.getPrice()
            .compareTo(BigDecimal.valueOf(500000)) > 0
```

Can we replace this with:

```
Property::something
```

No.

Why?

Because we're not just calling one method.

We're doing:

```
getPrice()
compareTo(...)
return boolean
```

That's actual logic.

Method references only work when the lambda is essentially:

```
x -> x.someMethod()
```

or

```
x -> mapper.someMethod(x)
```

## 4 — Optional

Old style:

```
Property property = propertyRepository.findById(id);

if (property == null) {
    throw new PropertyNotFoundException(id);
}
```

Modern Java:

```
Property property =
        propertyRepository.findById(id)
                .orElseThrow(
                    () -> new PropertyNotFoundException(id)
                );
```

Same logic.

Much safer.
