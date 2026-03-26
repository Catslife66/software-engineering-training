# APIs and Service Communication

> service-to-service communication

## How Services Communicate

Microservices communicate using **APIs**.

Just like frontend → backend:
```
Frontend → HTTP → Backend
```
Services do the same:
```
Service A → HTTP → Service B
```

Example:

User Service → get user info

Payment Service → process payment
```
Payment Service
      ↓
GET /users/123
      ↓
User Service
      ↓
returns user data
```

## Two Main Communication Styles
1. Synchronous (Request–Response)
    ```
    Service A → request → Service B
    Service B → response → Service A
    ```
    Example:
    ```
    Payment → User Service (check user)
    ```
    This is like HTTP API calls.

2. Asynchronous (via Queue)
    ```
    Service A → send message → queue
    Worker → process later
    ```

    Example:
    ```
    Order created → send email job
    ```
    We already covered this with queues.

### Tradeoffs
**Synchronous**
```
✔ simple
✔ immediate response
✖ blocking (must wait)
✖ dependent on other service
```
**Asynchronous**
```
✔ decoupled
✔ more scalable
✔ resilient
✖ more complex
✖ eventual consistency
```

### Real System Example
```
User signs up
   ↓
User Service → create user
   ↓
Send event → queue
   ↓
Email Service → send welcome email
```

## Key insight
```
Distributed systems = dealing with unreliable networks
```
That is why microservices are powerful but harder.