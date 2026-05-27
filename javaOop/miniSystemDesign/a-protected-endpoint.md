# A Protected Endpoint

**Cross-cutting Concern**

A cross-cutting concern is logic that applies across many parts of the application, such as:

- authentication
- logging
- error handling
- request tracing

These are often handled outside normal service methods.

Endpoint:

```
GET /users/me
```

Goal:

```
Return the currently authenticated user's profile
```

## JWT Filter Flow

**Step 1 — Client sends JWT**

```
Authorization: Bearer eyJhbGciOi...
```

**Step 2 — JWT filter validates token**

Filter:

- extracts token
- validates token
- extracts identity from token

Example extracted claims:

```
userId
email
role
```

**Step 3 — Security context is populated**

The filter usually stores authenticated user info into a security/authentication context.

Conceptually:

```
Request is now marked as authenticated
```

**Step 4 — Controller/service accesses authenticated user**

Then controller/service can access:

```
current authenticated user
```

from the security context.

So endpoint becomes:

```
GET /users/me
```

NOT:

```
GET /users/me?userId=123
```

because the identity already comes from JWT.

## Conceptual Flow

```
Client sends JWT
→ JWT filter validates token
→ filter extracts userId/email
→ request becomes authenticated
→ controller gets current authenticated user
→ service loads user data
→ controller returns UserResponseDTO
```

Controller:

- receives no userId from client
- gets authenticated userId from security context
- calls userService.getProfile(userId)
- maps Result to HTTP response

Service:

- uses userId to find user
- if user missing -> USER_NOT_FOUND
- uses userId to find profile
- if profile missing -> PROFILE_NOT_FOUND
- returns Result<UserProfileResponseDTO>

Repository:

- UserRepository.findById(userId)
- UserProfileRepository.findByUserId(userId)

DTOs

- UserProfileResponseDTO {name, email...}

Flow

- client sends JWT token
- JWT filter validates token and security context is populated
- controller receives client request and call UserService
- service check user with user id
- service find user profile with user id
- client receives a success response

Possible Results

- invalid token -> 401 UNAUTHORIZED
- user not found -> 404 USER_NOT_FOUND
- profile not found -> 404 PROFILE_NOT_FOUND
- success -> 200 OK
- unexpected DB/server failure -> exception / 500
