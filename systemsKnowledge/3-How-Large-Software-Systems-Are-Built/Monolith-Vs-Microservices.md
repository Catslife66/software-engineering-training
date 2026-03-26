# Monolith vs Microservices

**Monolith** 👉🏻 You deploy everything together

```
Client → One App → Database
```

**Microservices** 👉🏻 You deploy services independently

```
Client
  ↓
API Gateway
  ↓
User Service
Payment Service
Search Service
```

Microservices = independent services that enable scalability, flexibility, and isolation

## What Problems Monoliths Struggle With

1. Scaling specific parts

   ```
   App
   ├ User
   ├ Payment
   ├ Search
   ```

   If only Search is heavy:

   You must scale the entire app ❌

2. Codebase complexity

   As the system grows:
   - huge codebase
   - tight coupling
   - hard to understand

3. Team coordination

   Many developers working on one codebase leads to:
   - merge conflicts
   - slow deployments
   - coordination overhead

4. Deployment risk

   small change → redeploy entire system

   This increases risk.

## What Microservices Solve

1. Independent scaling
   ```
   Search Service → scale x10
   Payment Service → scale x2
   ```
2. Separation of concerns

   each service = one responsibility

   Cleaner architecture.

3. Team ownership

   ```
   Team A → User service
   Team B → Payment service
   ```

   Teams work independently.

4. Fault isolation

   If one service fails:

   Payment fails → Users still browse products

## Why Microservices Are a Bad Fit for Small Systems

1. Network Complexity

   Monolith:

   ```
   function call → instant
   ```

   Microservices:

   ```
   HTTP call → network latency + failure risk
   ```

2. Deployment Complexity

   Monolith:

   ```
   1 app → 1 deployment
   ```

   Microservices:

   ```
   multiple services → multiple deployments
   ```

   You now need:
   - service orchestration
   - environment management
   - version coordination

3. Debugging Becomes Harder

   Monolith:

   ```
   error → trace in one codebase
   ```

   Microservices:

   ```
   error → trace across multiple services
   ```

   This requires:
   - logging systems
   - tracing tools

4. Infrastructure Overhead

   You now need:
   - load balancer
   - service discovery
   - API gateway
   - monitoring

   That’s a lot for a small app.

## Real-World Rule of Thumb

```
Small system → monolith
Growing system → modular monolith
Large system → microservices
```
