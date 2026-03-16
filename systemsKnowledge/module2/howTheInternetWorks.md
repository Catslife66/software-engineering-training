# How the Internet Works

When you type: https://google.com, what actually happens?

We will break this down step by step.

You will learn:

- DNS
- IP addresses
- routers
- packets
- TCP connections
- HTTP requests

## step 1 - Domain Name vs IP Address

Humans use domain names. But computers communicate using IP addresses.
An IP address is like a network address for a computer.

Your computer must translate the domain name into an IP address before connecting.

This translation system is called **DNS**.

## step 2 - Your Browser Sends a Request

Once the IP address is known, your browser can connect to the server.

This request travels across the internet as **data packets**.

## step 3 - Data Travels Through the Internet

The request does not go directly to the server.

It travels through many network devices called **routers**.

Example path:

```
Your Computer
     ↓
Home Router
     ↓
Internet Service Provider
     ↓
Multiple Internet Routers
     ↓
Data Center Network
     ↓
Web Server
```

Routers examine the destination IP and decide where to send the packet next.

Think of routers like **postal sorting centers** for internet traffic.

## step 4 - The Server Processes the Request

The request finally reaches the server.

The server might be running software like:

- Nginx
- Apache HTTP Server

Or an application server such as:

- Node.js

The server then:

```
receive request
run application code
generate response
```

Example response:

```
HTTP/1.1 200 OK
Content-Type: text/html
```

And the webpage data is sent back.

## step 5 — The Response Travels Back

The response packets travel back through the internet.

```
Server
   ↓
Internet routers
   ↓
ISP(Internet Service Provider)
   ↓
Home router
   ↓
Your computer
```

Your browser then renders the webpage.

## The Complete Flow

Here is the full simplified pipeline:

```
User types URL
      ↓
DNS finds IP address
      ↓
Browser connects to server
      ↓
HTTP request sent
      ↓
Server processes request
      ↓
HTTP response returned
      ↓
Browser renders page
```

## Mental Model

Think of the internet like a global postal system.

```
IP address = street address
routers = sorting centers
packets = envelopes
```

Each packet is forwarded until it reaches the destination.

## CHECKPOINTS

### Why can't computers directly use domain names?

Computers communicate using IP addresses, not human-readable names. The IP behind a domain may change

Reasons include:

- server migration
- load balancing
- CDN routing
- failover

Because of this, using domain names allows infrastructure to change **without breaking links**.

## Purpose of routers

Routers examine the destination IP address and decide the next hop for the packet.

Each router only knows **the next best direction**, not the entire path.

This makes the internet **scalable and resilient**.

### Do packets always return via the same path?

Packets may take different routes.

The internet uses dynamic routing protocols that choose paths based on factors like:

- network congestion
- link failures
- routing policies
- latency

Example:

```
Request path:
A → B → C → D

Response path:
D → E → F → A
```

This flexibility helps the internet **self-heal and reroute** traffic when parts of the network fail.
