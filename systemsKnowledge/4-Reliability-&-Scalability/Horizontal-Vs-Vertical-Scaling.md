# Horizontal vs Vertical Scaling

> Understand how systems handle growth in users and traffic

## Vertical Scaling (Scale Up)

> increases the capacity of a single machine by adding more resources such as CPU or memory

```
Make one machine more powerful
```

Example:

```
4GB RAM → 32GB RAM
2 CPU → 16 CPU
```

**Advantages**

```
✔ simple
✔ no architecture change
```

**Disadvantages**

```
✖ hardware limits
✖ expensive
✖ still single point of failure -> because system depends on one machine
```

## Horizontal Scaling (Scale Out)

```
Add more machines
```

Example:

```
1 server → 10 servers → 100 servers
```

**Advantages**

```
✔ scalable
✔ fault tolerant
✔ flexible
```

**Disadvantages**

```
✖ more complex
✖ requires load balancing
✖ distributed system challenges
```

## Visual Comparison

**Vertical**

```
1 bigger server
```

**Horizontal**

```
Load balancer
   ↓
Multiple servers
```

## Key Insight

Most modern systems use horizontal scaling, because it supports growth and fault tolerance.

Horizontal scaling requires:

```
stateless services
load balancing
distributed systems design
```

Vertical scaling = limited and simple

Horizontal scaling = scalable but complex
