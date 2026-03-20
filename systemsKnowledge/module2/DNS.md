# DNS (Domain Name System)

DNS is essentially the phonebook of the internet.

## Step 1 — DNS Lookup Begins

When you enter: https://example.com

Your computer first checks: **local DNS cache**

Your system may already remember the IP.

If it does: **cache hit → use stored IP**

If not: **cache miss → query DNS server**

## Step 2 — Recursive DNS Resolver

Your computer sends a query to a DNS resolver.

This is usually operated by:

- your ISP
- a public DNS provider

Examples include:

- Google Public DNS
- Cloudflare DNS

## Step 3 — DNS Hierarchy

DNS works as a distributed hierarchical system.

The resolver may ask several servers.

```
Root DNS servers
      ↓
Top-Level Domain servers (.com, .org)
      ↓
Authoritative DNS servers
```

Example flow:

```
Resolver → Root server
"Who handles .com?"
      ↓
Resolver → .com server
"Who handles example.com?"
      ↓
Resolver → authoritative server
"What is example.com's IP?"
```

Finally the resolver gets:

```
93.184.216.34
```

The hierarchical system allows:

```
distribution of responsibility
parallel lookups
global scalability
fault tolerance
```

## Step 4 — Return the IP Address

The resolver sends the IP back to your computer.

Your computer stores it in cache for some time.

Example:

```
example.com → 93.184.216.34
```

Now the browser can connect to the server.

## Why DNS Is Distributed

If there were one global DNS server:

```
billions of requests → single server
```

That would never scale.

Instead DNS is globally distributed, which provides:

- scalability
- reliability
- redundancy

## Real Systems Use DNS for More Than Lookup

DNS is also used for:

- load balancing
- traffic routing
- failover
- CDN routing

For example, 'example.com' might resolve to different IPs depending on:

- your country
- network conditions
- server health

## Mental Model

DNS is like a multi-level phonebook system.

```
Root servers → know domain registries
TLD(Top-Level Domain) servers → know domain owners
Authoritative servers → know exact IPs
```

## Full Workflow

**step 1 - DNS resolves the IP**

```
Your Computer
↓
DNS Resolver (ISP / Cloudflare / Google)
↓
Authoritative DNS Server
↓
Returns IP address (e.g. 93.184.216.34)
```

At this point:

```
You ONLY know where the server is
(no website data yet)
```

**Step 2 - Contact the Website Server**

```
Your Browser
↓
Connect to IP (93.184.216.34)
↓
Web Server (e.g. Nginx / Node)
↓
Returns HTML / JSON / data
```

Now you actually get the website.

## DNS cache TTL

DNS results can be cached at multiple levels:

1. Your browser
2. Your operating system
3. Your router
4. Your ISP DNS resolver
5. Other DNS servers

Each layer respects the same TTL.

Example

```
example.com → 93.184.216.34
TTL = 300 seconds
```

This means: "you can cache this for 5 minutes"

So:

- your computer caches it
- DNS resolver caches it
- maybe even intermediate systems cache it

## What Happens When IP Address Changes?

**Before Change**

```
example.com → 1.1.1.1
TTL = 300s
```

Users around the world cache this.

**After Change**

The DNS record is updated:

```
example.com → 2.2.2.2
```

**Now the world is split**

Because of caching:

Some users → still use 1.1.1.1 (cached)

Some users → get 2.2.2.2 (new lookup)

This is called **DNS propagation**

## Real Deployment Strategy

Good systems handle this carefully. And engineers often lower TTL before migrating servers.

Typical approach:

1. Deploy new server (2.2.2.2)
2. Keep old server running (1.1.1.1)
3. Update DNS
4. Wait for TTL to expire
5. Shut down old server

This avoids downtime.
