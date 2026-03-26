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

