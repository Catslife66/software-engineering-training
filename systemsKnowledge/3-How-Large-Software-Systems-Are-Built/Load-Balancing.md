# Load Balancing

Load balancing means:

> Distributing incoming requests across multiple servers

Basic Architecture

```
Users
  ↓
Load Balancer
  ↓
Server A   Server B   Server C
```

**Engineer Vocabulary**

```
distribute requests
application instances
resource utilization
health checks
remove from rotation
fault tolerance
high availability
horizontal scalability

Load Balancer

Request Distribution
Route Traffic

Application Instance
Healthy Instance
Unhealthy Instance

Health Check
Health Probe

Remove From Rotation

Failover

Fault Tolerance

High Availability

Horizontal Scalability

Resource Utilization

Single Point of Failure (SPOF)
```

A load balancer distributes incoming requests across multiple application instances to improve scalability, availability, and fault tolerance.

It continuously performs health checks and routes traffic only to healthy instances. If an instance becomes unhealthy, it is removed from rotation, allowing the remaining instances to continue serving requests.

This architecture enables horizontal scaling because additional server instances can be added behind the load balancer without changing the client.

## Why Load Balancing is Needed

Without it:

```
All traffic → 1 server → overload ❌
```

With it:

```
Traffic spread → multiple servers → stable system ✅
```

## How Load Balancer Decides

Common strategies:

1. Round Robin

```
Request 1 → A
Request 2 → B
Request 3 → C
```

2. Least Connections

```
Send request to server with fewest active requests
```

3. Weighted

```
Powerful server → more traffic
Weaker server → less traffic
```

## Real Tools

Examples used in production:

- Nginx
- HAProxy

## Health Checks

👇 Load balancer continuously checks:

```
Is server alive?
Is server responding?
```

👇
If a server fails:

```
Remove from rotation
```

👇
If a server recovers:

```
Add back to rotaion
```

## Key insight

Load balancing = scalability + fault tolerance

## Question practice

Question: Why does load balancing improve system reliability?

Answer:

```
// core idea
Load balancing distributes incoming traffic across multiple servers and monitors their health.

// how it works
By spreading requests, it prevents any signle server from becoming overloaded and by removing unhealthy or slow servers from rotation, it avoids sending traffic to failing instances.

// what it leads to
As a result, the system remains available and resilient, continuing to server requests even when some severs fail.
```
