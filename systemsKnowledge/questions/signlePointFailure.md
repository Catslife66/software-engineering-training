## Q: Can you truly call that system “highly available”?

Suppose you have only one API server and one database.

## A single point of Failure

That system would not be considered highly available. It has single points of failure.

A single point of failure (SPOF) is any component that, if it fails, causes the entire system to stop working.

## What Highly Available Systems Look Like

High availability means:

> The system continues working even if some components fail.

To achieve that, we add redundancy.
Example:

```
            Load Balancer
                 ↓
        API1    API2    API3
                 ↓
           Primary DB
              ↓
           Replica DB
```

- If an API server fails
  The load balancer stops sending traffic to it.
- If the primary database fails
  The system can promote the replica: a failover

## The Goal of High Availability

A highly available system tries to eliminate single points of failure.

Common examples of redundancy:
| Component | Redundancy Strategy |
| -------------- | ----------------------- |
| API servers | multiple instances |
| databases | replication |
| caches | clustered caches |
| load balancers | multiple load balancers |
| message queues | distributed brokers |

## Real Cloud Architecture

```
Users
  ↓
CDN
  ↓
Load Balancer
  ↓
API Servers (multiple)
  ↓
Cache Cluster
  ↓
Database Primary + Replicas
```

## A Famous Engineering Principle

Design for failure.

Assume components will eventually:

- crash
- slow down
- lose network connectivity
- run out of memory

## Q: Can the application continue operating immediately, or will there still be some disruption? Why?

Suppose we now have:

```
Load Balancer
     ↓
3 API servers
     ↓
Primary DB
     ↓
Replica DB
```

The primary database suddenly crashes.

## What Happens When the Primary Database Crashes

1. Primary DB crashes
   Suddenly: API → Primary DB → connection error
   At this moment the system doesn't instantly know what happened.
   Possible scenarios:

- temporary network issue
- slow response
- actual crash
  So the system must detect the failure first.

2. Failure detection
   Monitoring tools or cluster managers detect that the primary database is unavailable.
   This may take a few seconds.

3. Replica promotion
   Once the system confirms the failure, the replica is promoted: failover
   Replica → becomes new Primary

4. API servers reconnect
   Now the API servers must reconnect to the new primary database.
   This may require:
   - updating connection endpoints
   - reconnecting database pools
   - retrying failed queries

## Why This Still Counts as High Availability

High availability does not mean zero downtime.
Systems are often designed for 99.9% or 99.99% uptime.
