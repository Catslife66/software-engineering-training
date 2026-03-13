## If one API server becomes slow or unhealthy behind a load balancer, what should the load balancer ideally do?

> The load balancer should continuously health-check backend instances and remove unhealthy ones from rotation so requests are routed only to healthy servers. That improves availability and prevents a failing server from degrading the entire system.

## What a good load balancer does

1. Health checks servers

A load balancer usually checks whether each backend server is healthy.
For example, it may call: /health or try to open a TCP connection.

If a server responds normally, it stays in rotation.

If it fails or becomes too unhealthy, the load balancer marks it as unavailable.

2. Removes unhealthy servers from rotation

3. Can gradually reintroduce the server
   Once the server recovers, the load balancer may start sending traffic to it again.
   healthy → receive traffic
   unhealthy → removed
   recovered → added back

So the load balancer improves both:

- availability
- fault isolation
