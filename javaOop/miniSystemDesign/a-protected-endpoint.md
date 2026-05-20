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
- service check user with security context
- service find user profile with user id
- client receives a success response

Possible Results

- invalid token -> 401 UNAUTHORIZED
- user not found -> 404 USER_NOT_FOUND
- profile not found -> 404 PROFILE_NOT_FOUND
- success -> 200 OK
- unexpected DB/server failure -> exception / 500

## Authorization (Permissions & Roles)

```
Authentication = Who are you?

Authorization = What are you allowed to do?
```

**Scenario**

We now add an admin-only endpoint:

```
GET /admin/users
```

**Goal**

```
Only ADMIN users can access all users
Normal users should receive 403 FORBIDDEN
```

JWT now contains role information

Example token claims:

```
{
  "userId": "123",
  "email": "abc@example.com",
  "role": "ADMIN"
}
```

After JWT validation:

```
Security context now contains:
- userId
- email
- role
```

### Role checking

In real Spring systems there are two common levels:

**Option A — Security layer authorization**

This is often preferred for simple role checks:

```
/admin/users requires ADMIN
```

The security layer can block non-admin users before the controller runs.

In Spring this is often done with method/security rules like:

```
@PreAuthorize("hasRole('ADMIN')")
```

or security configuration.

**Option B — Service layer authorization**

This is useful when the rule is more business-specific:

```
Only the owner agent of this property can edit it
```

That kind of rule often needs database/domain checks, so service layer makes sense.

So the refined answer is:

```
Simple role checks → security layer
Business/domain permission checks → service layer
```

### Flow refinement

For admin endpoint, the best flow can be:

```
Client sends JWT
→ JWT filter validates token
→ Security context contains userId/role
→ Authorization rule checks ADMIN role
→ if not admin: 403 before controller
→ if admin: controller calls service
→ service gets all users - userService.getAllUsers()
→ repository returns users
→ service maps User entities to UserResponseDTO list
→ controller returns 200
```
