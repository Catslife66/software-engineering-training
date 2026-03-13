# Message Queues(Async Processing)

Example:

```
User request → queue job → respond immediately
               ↓
           background worker
```

Technologies used for this include:

- RabbitMQ
- Apache Kafka
- Redis

## The big system design idea

Queues help separate:
**must succeed now**
from
**can happen soon**

## Retries

Queues are powerful because background workers can retry temporary failures.

**Key engineering principle**

A queue improves systems by:

- reducing request latency
- isolating failures
- smoothing traffic spikes
- moving non-critical work out of the request path

## Backpressure

> when one part of the system cannot keep up, upstream parts must slow down, reject work, or degrade gracefully.
