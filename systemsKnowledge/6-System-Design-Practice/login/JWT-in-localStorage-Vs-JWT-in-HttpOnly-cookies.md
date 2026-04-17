# JWT in localStorage vs JWT in HttpOnly cookies

| Aspect                       | JWT in localStorage                          | JWT in HttpOnly cookie                     |
| ---------------------------- | -------------------------------------------- | ------------------------------------------ |
| How it is sent               | Frontend reads token and manually adds it    | Browser sends cookie automatically         |
|                              | to `Authorization` header                    |                                            |
| JavaScript access            | Yes                                          | No                                         |
| Main security risk           | XSS                                          | CSRF                                       |
| Safer against XSS?           | No                                           | Yes                                        |
| Safer against CSRF?          | Yes, by default not auto-sent                | No, needs protection                       |
| Frontend control             | High                                         | Lower                                      |
| Cross-domain/API flexibility | Often easier                                 | Can be trickier because of CORS,           |
|                              |                                              | `credentials`, `SameSite`, `Secure`        |
| Logout/revocation UX         | App can delete token easily on client, but   | App can clear cookie, but server still has |
|                              | server still cannot revoke issued JWT easily | same JWT revocation issue unless extra     |
|                              |                                              | controls exist                             |
| Best fit                     | SPA/frontend-heavy apps where team wants     | Web apps prioritizing protection from      |
|                              | explicit control                             | token theft via JavaScript                 |

## Core difference

### JWT in localStorage

Frontend controls the token

Flow:

```
read token from localStorage
→ attach Authorization header
→ send request
```

Good side:

```
simple mental model
easy to attach to APIs
good control in frontend code
```

Bad side:

```
if attacker runs JavaScript through XSS, they can steal the token
```

### JWT in HttpOnly cookie

Browser controls sending the token

Flow:

```
browser stores cookie
→ browser automatically sends cookie with requests
```

Good side:

```
JavaScript cannot read the token
```

So if there is XSS:

attacker cannot easily steal the token value

Bad side:

```
because cookies are auto-sent,
CSRF becomes a risk
```

So you need:

```
CSRF token
SameSite strategy
Secure
```

## Security tradeoff

localStorage

```
Main danger = token theft through XSS
```

HttpOnly cookie

```
Main danger = forged authenticated requests through CSRF
```

## Practical decision guide

**Prefer HttpOnly cookies when:**

- same-site or controlled web app
- security is high priority
- you can set up CSRF protection properly

**localStorage is often chosen when:**

- frontend and backend are separate
- SPA architecture
- team wants simple Authorization header flow

But security-wise, many teams still prefer:

```
HttpOnly cookie + CSRF protection
```

because stolen tokens are often worse than CSRF in practice.

## Best mental model

```
localStorage = easier developer control, weaker token protection
HttpOnly cookie = stronger token protection, more browser/security complexity
```
