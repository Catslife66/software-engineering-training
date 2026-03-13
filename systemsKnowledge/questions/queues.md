## Q: Suppose 50,000 users sign up during a promotion.

Why is a queue especially useful in that situation?

What happens during 50,000 signups

The essential path is already expensive enough:

```
User signup
   ↓
Validate request
   ↓
Write user to DB
   ↓
Return success
```

Now imagine adding extra synchronous work inside the same request:

- send welcome email
- notify analytics
- create onboarding tasks
- call third-party CRM
- generate default content

Then each request becomes heavier and slower.

## Why the queue helps

A queue lets the API do this:

```
User signup
   ↓
Create user in DB
   ↓
Push background jobs to queue
   ↓
Return success fast
```

And then workers handle the extra tasks later:

```
Queue
   ↓
Worker 1 sends email
Worker 2 sends analytics
Worker 3 creates onboarding data
```

1. It keeps the request path short

This is the most important point.

Users only wait for the essential work.

That protects response time even during spikes.

2. It smooths burst traffic

50,000 users may arrive very quickly, but workers can process the background jobs steadily.
This is called **traffic smoothing** or **buffering**.

3. It isolates failures

If the email provider or analytics service becomes slow, signup can still succeed.

Without a queue, a non-critical dependency can damage a critical user action.

With a queue, the failure is contained in the background processing layer.

## Interview-quality answer

> A queue is useful during a signup spike because it keeps the critical path small. The API only performs the essential work needed to create the account, then offloads non-critical tasks like emails and analytics to background workers. That improves response time, isolates failures, and smooths burst traffic so the system stays stable under load.
