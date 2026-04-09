# Exception Handling

## 1. Expected outcomes (business logic)

> Normal situations that can happen

Examples:

- user not found during search
- property already in wishlist
- invalid login credentials

👉 These are **NOT system errors**

## 2. Unexpected errors (system problems)

> Something went wrong technically

Examples:

- database connection failed
- null pointer bug
- external API failure

👉 These are **exceptions**

## ❌ Why throwing exception for "User Not Found" is not ideal?

```
throw new NotFoundException(...)
```

Problems:

- too heavy for normal flow ❌
- mixes business logic with error handling ❌
- harder to read/control ❌

## When SHOULD we use exceptions?

Use exceptions when:

```
The system cannot continue normally / Unexpected technical/system failure
```

Example:

```
if (dbConnection == null) {
    throw new DatabaseException("Connection failed");
}
```

## Clean rule

```
Expected → return Result
Unexpected → throw Exception
```

## Real backend mapping

In real systems (Spring Boot):

- Service may return Result OR throw exceptions
- Controller maps:
  - Result → 200 / 400
  - Exception → 500 / 404
