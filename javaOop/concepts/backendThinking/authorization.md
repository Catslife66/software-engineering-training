# Authorization (Permissions & Roles)

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

## Role checking

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

## Flow refinement

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

## Role-based authorization vs Ownership-based authorization

### Senario 1

```
PUT /properties/{propertyId}
```

Only the agent who owns this property can edit it.

1. Controller

- receives propertyId from path
- receives propertyUpdate DTO from request body
- gets authenticated userId from security context
- calls propertyService.updateProperty(userId, propertyId, request)
- maps Result to HTTP response

2. Service

- validate request body
- find property by propertyId
- if missing → PROPERTY_NOT_FOUND
- find authenticated user by userId
- if missing → USER_NOT_FOUND
- check property.agentId equals user.id
- if not → FORBIDDEN
- update allowed fields
- save property
- return success Result

3. Repository

- UserRepository.findById(userId) // can skip this
- PropertyRepository.findById(propertyId)
- PropertyRepository.save(property)

In a real system, if the JWT already represents a valid authenticated user, sometimes you may skip UserRepository.findById(userId) and just check: property.agentId == authenticatedUserId

4. Authorization

Ownership authorization belongs in the service layer because it depends on domain data.

5. Possible Results

- invalid token -> 401 UNAUTHORIZED
- property not found -> 404 PROPERTY_NOT_FOUND
- authenticated user is not the owning agent -> 403 FORBIDDEN
- success -> 200 OK

### Scenario 2

An ADMIN should also be allowed to edit any property.

```
An agent can edit their own property.
An admin can edit any property.
```

1. Service

- validate request body
- find property by propertyId
- if missing -> PROPERTY_NOT_FOUND

- get authenticated userId and role from security context/controller input

- if role == ADMIN:
  allow update

- else if property.agentId == authenticatedUserId:
  allow update

- else:
  return FORBIDDEN

- update allowed fields
- save property
- return success
