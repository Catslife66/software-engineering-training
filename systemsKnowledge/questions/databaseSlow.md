## Q: What happens to the API servers?

Load Balancer
↓
10 API Servers
↓
Database
Suddenly the database becomes slow.

## Chain reaction

- Step 1: Requests become slow
  API server → query database → wait → build response
  If the database is slow, every request spends more time waiting.
- Step 2: Waiting requests start piling up
  Now imagine requests keep arriving faster than old ones finish. So the API servers start accumulating many in-flight requests.
  This creates **backpressure**.
- Step 3: API server resources get consumed
  Even if the API server is not doing heavy CPU work, waiting requests still consume resources:
  open connections
  memory
  worker threads or event loop slots
  database connection pool slots
- Step 4: Timeouts and failures begin
  As the queue grows:
  some requests exceed timeout limits
  users may see 500s, 502s, 503s, or 504s
  retries may increase traffic even more
- Step 5: Entire system can look down
  Even if the API servers are technically still running, users may experience the product as unavailable.
  So a slow database can make the whole application feel broken.
