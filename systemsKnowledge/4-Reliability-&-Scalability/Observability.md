# Observability (Logs, Metrics, Tracing)

How engineers understand and debug real systems?

**Problem**

In real systems:

```
many servers
many services
many requests
```

When something goes wrong:

```
You cannot "see" the system directly
```

**Solution — Observability**

Observability helps you answer:

```
What is happening inside the system?
```

## 3 Pillars of Observability

### 1. Logs

Detailed records of events

Example:

```
"User 123 logged in"
"Payment failed"
"Error: DB timeout"
```

Purpose:

```
debug specific issues
```

### 2. Metrics

Numerical data over time

Examples:

```
CPU usage
request count
error rate
latency
```

Purpose:

```
monitor system health
```

### 3. Tracing

Track a request across services

Example:

```
Request → API → Service A → DB → Service B
```

Purpose:

```
understand request flow and bottlenecks
```

### Real Tools

Examples used in production:

```
Prometheus (metrics)
Grafana (visualization)
Jaeger (tracing)
```

## Why Observability Matters

Without it: system fails → no idea why ❌

With it: detect → diagnose → fix ✅

Real Scenario

```
User reports slow API
```

You check:

```
Metrics → high latency
Tracing → DB query slow
Logs → timeout errors
```

👉 Root cause found.

## Key Insight

Observability = visibility into system behavior

Mental Model

```
Logs → what happened
Metrics → how often / how much
Tracing → where it happened
```

## Checkpoint

**1️⃣ Why are logs not enough on their own?**
Logs provide detailed records of events, but they are isolated and hard to aggregate at scale.

They do not give a high-level view of system health (metrics) or show how a request flows across multiple services (tracing).

As a result, logs alone are insufficient for understanding system-wide issues.

**2️⃣ Why are metrics important for detecting issues early?**
Metrics provide aggregated, real-time data about system performance such as latency, error rates, and resource usage.

By monitoring trends and thresholds, engineers can detect anomalies and potential failures early, before they impact users.

**3️⃣ Why is tracing especially important in microservices?**
Tracing is important in microservices because a single request often passes through multiple services.

Tracing allows engineers to follow the entire request path across services and identify where latency or failure occurs.

Without tracing, it is difficult to pinpoint which service in the chain is causing the issue.

## Phase 4 mental model

```
More users
   ↓
Need scaling
   ↓
Scaling → distributed systems
   ↓
Distributed systems → failures
   ↓
Failures → tradeoffs (consistency vs availability)
   ↓
Need observability to debug
```

How everything connect

```
Load balancing → distribute traffic
Caching → reduce load
Queues → smooth spikes

Scaling → more servers
DB scaling → replicas + sharding

Failures → consistency vs availability

Observability → understand system behavior
```

1️⃣ Why does scaling introduce consistency problems?

Scaling introduces distributed systems, where data is stored and processed across multiple nodes.

Due to network delays and failures, it is difficult to keep all nodes perfectly synchronized at all times.

As a result, systems must choose between waiting for consistency (which reduces availability) or responding quickly with potentially stale data, creating a consistency vs availability tradeoff.

2️⃣ Why is a single database dangerous in a scaled system?

A single database becomes a bottleneck in a scaled system because all requests depend on it.

As traffic increases, the database can become overloaded, slowing down or failing under heavy load.

As a result, it acts as a single point of failure, where a database outage can bring down the entire system.

3️⃣ Why do we need observability in distributed systems?

In distributed systems, a single request can pass through multiple services, making issues difficult to diagnose.

Observability provides visibility through logs (what happened), metrics (system health), and tracing (request flow).

As a result, engineers can detect, diagnose, and resolve issues efficiently.
