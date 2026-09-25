# Spring Security

Spring Security protects an application by establishing trusted identity and enforcing access policy before and during business operations.

Security is not simply:

```
login
```

A real application needs to answer several different questions:

```
Who are you?
What operations may you perform?
May you perform this operation on this particular resource?
How is your authenticated identity recognised on later HTTP requests?
How should authentication and authorization failures be represented over HTTP?
```

These questions belong to different responsibilities.

For BrightMove, consider:

```
Sarah → AGENT
David → AGENT
Alice → ADMIN

Property 42 → owned by David
```

If Sarah requests:

```http
DELETE /properties/42
```

the application may establish:

```
Authentication:
Sarah is authenticated
→ PASS

Role authorization:
Sarah has ROLE_AGENT
→ PASS

Resource authorization:
Sarah does not own Property 42
→ FAIL

Final result:
403 Forbidden
```

Authentication, role authorization, and resource authorization are therefore separate stages.

The central engineering goal is:

> Establish identity from trusted authentication state, enforce policy at deliberate boundaries, and never allow client-supplied identity or privilege claims to substitute for trusted server-side facts.

## Core Concepts

### Authentication

Authentication asks:

> Who are you?

For username/password authentication:

```
email
+
password
↓
credential verification
↓
authenticated identity
```

Before authentication, an email is only a claim:

```
principal = sarah@example.com
credentials = secret123
authenticated = false
```

After successful authentication:

```
principal = Sarah
authorities = ROLE_AGENT
authenticated = true
```

The important distinction is that authentication establishes a trusted identity from acceptable evidence.

---

### Authorization

Authorization asks:

> What are you allowed to do?

For example:

```
POST /properties
→ requires ROLE_AGENT
```

An authenticated customer may therefore fail authorization:

```
Bob authenticated
ROLE_CUSTOMER
↓
POST /properties requires ROLE_AGENT
↓
403 Forbidden
```

Authentication can succeed while authorization fails.

---

### Resource / Ownership Authorization

Role authorization is often too broad to express a complete business policy.

BrightMove may define:

```
AGENT
→ may create properties
→ may edit/delete own properties

ADMIN
→ may edit/delete any property
```

Therefore:

```
Sarah = ROLE_AGENT
Property 42 owner = David
```

means:

```
ROLE_AGENT check
→ PASS

ownership check
→ FAIL
```

Resource authorization asks:

> May this authenticated user perform this operation on this particular object?

This is different from asking whether Agents in general may perform property deletion.

---

### `Authentication`

`Authentication` is Spring Security's representation of authentication information/state.

Conceptually:

```
Authentication
├── principal
├── credentials
├── authorities
└── authenticated
```

After successful login:

```
Authentication
├── principal → Sarah
├── authorities → ROLE_AGENT
└── authenticated → true
```

`Authentication` is not JWT.

A JWT may be used to establish an `Authentication` for a request.

---

### `AuthenticationManager`

`AuthenticationManager` coordinates authentication.

Typical usage:

```java
Authentication authentication =
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            email,
            password
        )
    );
```

Conceptually:

```
authentication request
↓
AuthenticationManager
↓
appropriate AuthenticationProvider
↓
successful Authentication
```

The manager does not normally query the user database directly.

---

### `AuthenticationProvider`

An `AuthenticationProvider` performs a particular authentication strategy.

For ordinary username/password authentication, a common provider is:

```
DaoAuthenticationProvider
```

It uses:

```
UserDetailsService
+
PasswordEncoder
```

Conceptually:

```
AuthenticationManager
        ↓
DaoAuthenticationProvider
       /                 \
      ↓                   ↓
UserDetailsService   PasswordEncoder
```

---

### `UserDetails`

BrightMove has its own domain `User`:

```java
@Entity
public class User {
    private UUID id;
    private String email;
    private String password;
    private UserRole role;
}
```

Spring Security requires a standard security representation.

That is the purpose of `UserDetails`.

Conceptually:

```
BrightMove User
↓
CustomUserDetails
↓
Spring Security
```

`UserDetails` exposes information such as:

```
username / login identifier
stored password hash
authorities
account enabled/locked/expired state
```

A separate adapter avoids coupling the JPA entity directly to Spring Security.

---

### `UserDetailsService`

`UserDetailsService` loads security user information by authentication identifier.

Its central method is:

```java
UserDetails loadUserByUsername(String username);
```

For BrightMove:

```
email
↓
CustomUserDetailsService
↓
UserRepository.findByEmail(...)
↓
PostgreSQL
↓
User
↓
CustomUserDetails
```

Important:

> `UserDetailsService` loads stored security information. It does not verify the submitted password.

It may load the stored password hash because the authentication provider needs it, but password verification belongs elsewhere.

---

### `PasswordEncoder`

Passwords must not be stored as plaintext.

Registration:

```
raw password
↓
PasswordEncoder.encode(...)
↓
encoded password
↓
database
```

Login:

```
submitted raw password
+
stored encoded password
↓
PasswordEncoder.matches(...)
```

A common implementation is:

```java
new BCryptPasswordEncoder()
```

BCrypt is intentionally computationally expensive and incorporates salt.

Therefore, the same raw password can produce different encoded strings.

Do not authenticate by doing:

```java
passwordEncoder.encode(raw)
    .equals(storedHash);
```

Use:

```java
passwordEncoder.matches(raw, storedHash);
```

---

### `SecurityContext`

`SecurityContext` holds the current `Authentication`.

Conceptually:

```
SecurityContext
└── Authentication
    ├── principal → Sarah
    ├── authorities → ROLE_AGENT
    └── authenticated → true
```

This gives downstream code access to trusted authenticated identity.

---

### `SecurityContextHolder`

Spring Security provides access to the current context through `SecurityContextHolder`.

For example:

```java
Authentication authentication =
    SecurityContextHolder
        .getContext()
        .getAuthentication();
```

Do not imagine this as one global user shared by the entire server.

Concurrent requests can have different security contexts:

```
Request A → Sarah
Request B → David
Request C → Alice
```

The useful mental model is:

> `SecurityContextHolder` gives code access to authentication associated with the current security processing context.

---

### Principal

The principal represents the authenticated identity.

With a custom `UserDetails` implementation:

```
User entity
↓
CustomUserDetails
↓
Authentication.principal
```

A principal may expose a stable application identifier:

```java
public UUID getId() {
    return user.getId();
}
```

This can avoid repeatedly treating email as the application identity.

---

### `GrantedAuthority`

Spring Security's general permission abstraction is:

```
GrantedAuthority
```

For example:

```java
new SimpleGrantedAuthority("ROLE_AGENT")
```

An authenticated Sarah may have:

```
Authentication
└── authorities
    └── ROLE_AGENT
```

Authorities may also represent finer-grained permissions:

```
PROPERTY_CREATE
PROPERTY_DELETE
VIEWING_APPROVE
USER_SUSPEND
```

---

## 2.14 Roles

Roles are conventionally represented as authorities with a `ROLE_` prefix.

Therefore:

```java
.hasRole("AGENT")
```

conceptually checks for:

```text
ROLE_AGENT
```

Compare:

```java
.hasRole("AGENT")
```

with:

```java
.hasAuthority("ROLE_AGENT")
```

The first uses the role convention; the second names the authority directly.

Useful mental model:

```text
ROLE
→ broad user category

AUTHORITY
→ general permission representation
```

---

## 2.15 `SecurityFilterChain`

Spring Security runs before the controller through the servlet filter infrastructure.

Conceptually:

```text
HTTP Request
↓
Spring Security Filters
↓
Controller
↓
Service
↓
Repository
```

The filter chain can establish authentication and enforce request-level authorization before application code is reached.

Example policy:

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/properties/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/properties/**").hasRole("AGENT")
    .requestMatchers(HttpMethod.DELETE, "/properties/**")
        .hasAnyRole("AGENT", "ADMIN")
    .anyRequest().authenticated()
);
```

Specific rules should appear before broad catch-all rules.

---

## 2.16 `permitAll()`

`permitAll()` means:

> Existing authentication is not required to reach this route.

It does not mean:

```text
do no validation
do no authentication work
trust all submitted data
```

For example:

```text
POST /auth/login
→ permitAll
```

must be public because an unauthenticated user needs to log in.

The login attempt can still fail because credentials are invalid.

---

## 2.17 Current User Identity

A client must not be trusted to submit its own identity for authorization.

Dangerous:

```java
@DeleteMapping("/properties/{propertyId}")
public void deleteProperty(
        @PathVariable UUID propertyId,
        @RequestParam UUID agentId) {
    propertyService.delete(propertyId, agentId);
}
```

An authenticated Sarah could submit David's ID.

A correct ownership decision compares:

```text
authenticated principal identity
vs
resource owner identity
```

not:

```text
client-supplied user ID
vs
resource owner identity
```

Trusted identity may be obtained through:

```text
SecurityContextHolder
@AuthenticationPrincipal
Authentication controller parameter
application CurrentUser abstraction
```

---

## 2.18 `CurrentUser` Abstraction

Instead of scattering:

```java
SecurityContextHolder.getContext().getAuthentication()
```

through every service, an application may introduce:

```java
public interface CurrentUser {
    UUID getUserId();
    boolean isAdmin();
}
```

with a Spring Security implementation.

Conceptually:

```text
PropertyService
↓ depends on
CurrentUser
↑ implemented by
SpringSecurityCurrentUser
↓
SecurityContextHolder
```

Advantages:

```text
clear application boundary
less direct framework coupling
easier unit testing
```

Cost:

```text
extra abstraction and code
```

It is useful when the responsibility is genuinely reused.

---

## 2.19 Sessions

HTTP requests are independent.

After login finishes, a later request needs a mechanism to associate itself with authenticated identity.

With server-side sessions:

```text
login
↓
server session
ABC123 → Sarah
↓
browser receives session identifier
JSESSIONID=ABC123
```

Later:

```text
browser sends ABC123
↓
server resolves session
↓
Sarah's authentication/security state
↓
SecurityContext
```

The browser normally carries the session identifier, while the server maintains session state.

---

## 2.20 JWT

JWT stands for JSON Web Token.

A signed JWT can carry claims such as:

```json
{
  "sub": "user-id",
  "role": "AGENT",
  "exp": 1234567890
}
```

A JWT commonly has:

```text
HEADER.PAYLOAD.SIGNATURE
```

A normal signed JWT is not automatically encrypted.

Therefore:

```text
encoded
≠
encrypted
```

Do not put passwords or secrets into an ordinary JWT payload.

The signature provides integrity/authenticity for the signed content.

Before trusting claims, the server must validate relevant properties such as:

```text
signature
expiration
other required claims
```

Decoding a token is not the same as validating it.

---

## 2.21 JWT and Spring `Authentication`

JWT is not Spring Security's `Authentication`.

Correct relationship:

```text
JWT arrives
↓
security filter validates token
↓
Authentication constructed/established
↓
SecurityContext populated
↓
authorization
```

Therefore:

```text
JWT ≠ Authentication
JWT ≠ SecurityContext
JWT ≠ authorization
```

JWT is one mechanism for carrying information used to re-establish authenticated identity across requests.

---

## 2.22 401 vs 403

Use this mental model:

```text
401 Unauthorized
→ acceptable authentication is missing/invalid
→ "I do not have an accepted authenticated identity for you."

403 Forbidden
→ authentication exists but authorization fails
→ "I know who you are, but you may not do this."
```

Examples:

```text
missing JWT
→ 401

invalid JWT
→ 401

expired JWT
→ 401

authenticated CUSTOMER calls AGENT endpoint
→ 403

authenticated AGENT tries to delete another Agent's property
→ 403
```

---

## 2.23 `AuthenticationEntryPoint`

At the Spring Security request boundary, unauthenticated access to a protected resource can be handled by:

```text
AuthenticationEntryPoint
```

Conceptually:

```text
protected request
↓
acceptable authentication unavailable
↓
AuthenticationEntryPoint
↓
401
```

For a REST API, it should normally return an API-appropriate response rather than an HTML login page.

---

## 2.24 `AccessDeniedHandler`

Request-level authorization failure can be handled by:

```text
AccessDeniedHandler
```

Conceptually:

```text
authenticated request
↓
insufficient authority
↓
AccessDeniedHandler
↓
403
```

Security-filter error handling and MVC/application exception handling are different boundaries.

---

# 3. Mental Model

## 3.1 Complete Authentication Model

```text
email + raw password
        │
        ▼
Authentication
(untrusted authentication request)
        │
        ▼
AuthenticationManager
        │
        ▼
DaoAuthenticationProvider
       /                    \
      ▼                      ▼
UserDetailsService      PasswordEncoder
      │                 matches(raw, hash)
      ▼
UserRepository
      │
      ▼
PostgreSQL
      │
      ▼
User
      │
      ▼
CustomUserDetails
       \                    /
        └─────────┬────────┘
                  ▼
        successful Authentication
        ├── principal
        ├── authorities
        └── authenticated = true
```

Responsibility breakdown:

```text
AuthenticationManager
→ coordinator

DaoAuthenticationProvider
→ username/password authentication strategy

UserDetailsService
→ load stored security user

PasswordEncoder
→ verify credentials

Authentication
→ resulting authentication state
```

---

## 3.2 Current Request Model

```text
HTTP Request
↓
Spring Security
↓
authentication established/restored
↓
SecurityContext
└── Authentication
    ├── principal
    └── authorities
↓
request authorization
↓
Controller
↓
Service
```

The `SecurityContext` does not itself perform ownership authorization.

It supplies trusted identity and authorities that authorization logic can use.

---

## 3.3 Three Authorization Questions

Think in three layers:

```text
1. Authentication
"Who are you?"

2. Role / authority authorization
"May users with your permissions perform this type of operation?"

3. Resource authorization
"May you perform this operation on this particular object?"
```

Example:

```text
Sarah
↓
authenticated?
YES
↓
ROLE_AGENT?
YES
↓
owns Property 42?
NO
↓
DENY
```

---

## 3.4 Trusted Identity Boundary

The client may control:

```text
propertyId
title
price
description
search parameters
```

These are untrusted request inputs.

The client must not be trusted to establish:

```text
who the current user is
which role the current user has
whether the current user owns a resource
```

Those facts should come from:

```text
authenticated principal
trusted authority data
database/domain ownership state
```

The secure comparison is:

```text
authenticated user ID
vs
resource owner ID
```

---

## 3.5 Authentication Across Requests

HTTP itself does not remember that two requests came from the same authenticated user.

Question:

```text
Request 1:
Sarah logs in

Request ends

Request 2:
DELETE /properties/42

How is Sarah recognised?
```

Two common answers:

```text
server-side session
or
token such as JWT
```

This is a different problem from checking Sarah's password.

---

## 3.6 Session Mental Model

```text
LOGIN

Sarah authenticates
↓
server stores session state
ABC123 → Sarah
↓
client receives session ID
```

Later:

```text
REQUEST

client sends ABC123
↓
server-side session lookup
↓
restore authentication/security state
↓
SecurityContext
```

---

## 3.7 JWT Mental Model

```text
LOGIN

email + password
↓
AuthenticationManager
↓
successful Authentication
↓
generate signed JWT
↓
client receives token
```

Later:

```text
REQUEST

Authorization: Bearer <JWT>
↓
JWT security filter
↓
validate signature/expiry/etc.
↓
establish Authentication
↓
SecurityContext
↓
authorization
↓
Controller
```

JWT does not replace `Authentication` or `SecurityContext`.

---

## 3.8 Error Boundary Model

```text
                    HTTP REQUEST
                         │
                         ▼
               SPRING SECURITY FILTERS
                         │
              ┌──────────┴──────────┐
              │                     │
      authentication failure   authorization failure
              │                     │
              ▼                     ▼
 AuthenticationEntryPoint    AccessDeniedHandler
              │                     │
             401                   403
```

If the request passes security:

```text
Controller
↓
Service
↓
Repository
```

application exceptions may be translated through application exception handling such as:

```text
@RestControllerAdvice
```

The external API can still use one consistent error contract.

---

# 4. Spring / Java Implementation

## 4.1 Domain User

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    protected User() {
    }

    // constructors/getters...
}
```

```java
public enum UserRole {
    CUSTOMER,
    AGENT,
    ADMIN
}
```

The `password` field stores an encoded password.

---

## 4.2 User Repository

```java
public interface UserRepository
        extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
}
```

Responsibility:

```text
persistence access
```

The repository does not authenticate users.

---

## 4.3 Custom `UserDetails`

```java
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public UUID getId() {
        return user.getId();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority>
            getAuthorities() {

        return List.of(
            new SimpleGrantedAuthority(
                "ROLE_" + user.getRole().name()
            )
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

In a real application, account-state methods can map real domain account status instead of always returning `true`.

---

## 4.4 Custom `UserDetailsService`

```java
@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username) {

        User user = userRepository
            .findByEmail(username)
            .orElseThrow(
                () -> new UsernameNotFoundException(
                    "User not found"
                )
            );

        return new CustomUserDetails(user);
    }
}
```

The submitted `username` can be an email address.

---

## 4.5 Password Encoder

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Registration:

```java
String encodedPassword =
    passwordEncoder.encode(request.password());
```

Authentication providers use:

```java
passwordEncoder.matches(
    submittedRawPassword,
    storedEncodedPassword
);
```

---

## 4.6 Security Configuration

Representative configuration:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http.authorizeHttpRequests(auth -> auth

            .requestMatchers("/auth/**")
                .permitAll()

            .requestMatchers(
                HttpMethod.GET,
                "/properties/**"
            )
                .permitAll()

            .requestMatchers(
                HttpMethod.POST,
                "/properties/**"
            )
                .hasRole("AGENT")

            .requestMatchers(
                HttpMethod.DELETE,
                "/properties/**"
            )
                .hasAnyRole("AGENT", "ADMIN")

            .anyRequest()
                .authenticated()
        );

        return http.build();
    }
}
```

This code expresses request-level policy.

It does not express all resource ownership rules.

---

## 4.7 Method Security

With:

```java
@EnableMethodSecurity
```

methods may use annotations such as:

```java
@PreAuthorize("hasRole('AGENT')")
public void someAgentOperation() {
    // ...
}
```

Method-level security protects method invocation rather than only an HTTP route.

It is useful when authorization should stay attached to a sensitive operation across multiple entry points.

Do not mechanically duplicate every route rule as a method annotation.

Choose deliberate boundaries.

---

## 4.8 Authentication Manager Usage

Login may delegate to:

```java
Authentication authentication =
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()
        )
    );
```

The initial token represents submitted authentication information.

After successful authentication, the returned `Authentication` represents authenticated state.

---

## 4.9 Auth Service

Representative design:

```java
@Service
public class AuthService {

    private final AuthenticationManager
        authenticationManager;

    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.authenticationManager =
            authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(
            LoginRequest request) {

        Authentication authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );

        String token =
            jwtService.generateToken(authentication);

        return new LoginResponse(token);
    }
}
```

The exact JWT library/API is an implementation choice.

The architecture is the important part.

---

## 4.10 Registration Service

Representative flow:

```java
public UserResponse register(
        RegisterRequest request) {

    if (userRepository.findByEmail(request.email())
            .isPresent()) {
        throw new EmailAlreadyExistsException();
    }

    String encodedPassword =
        passwordEncoder.encode(request.password());

    User user = new User(
        request.email(),
        encodedPassword,
        UserRole.CUSTOMER
    );

    return userMapper.toResponse(
        userRepository.save(user)
    );
}
```

The public request must not be allowed to choose a privileged role.

For example, do not blindly accept:

```json
{
  "email": "attacker@example.com",
  "password": "...",
  "role": "ADMIN"
}
```

and persist the submitted role.

Privileged role assignment requires trusted server policy and, where applicable, separately authorized administration.

---

## 4.11 Accessing Current Authentication

Direct access:

```java
Authentication authentication =
    SecurityContextHolder
        .getContext()
        .getAuthentication();
```

Name:

```java
String email = authentication.getName();
```

Custom principal:

```java
CustomUserDetails principal =
    (CustomUserDetails)
        authentication.getPrincipal();

UUID userId = principal.getId();
```

---

## 4.12 `@AuthenticationPrincipal`

A controller may receive the authenticated principal:

```java
@DeleteMapping("/properties/{id}")
public void deleteProperty(
        @PathVariable UUID id,
        @AuthenticationPrincipal
        CustomUserDetails currentUser) {

    propertyService.deleteProperty(
        id,
        currentUser.getId()
    );
}
```

This parameter comes from Spring Security, not from client request data.

Compare:

```java
@RequestParam UUID userId
```

with:

```java
@AuthenticationPrincipal CustomUserDetails currentUser
```

Their trust origins are completely different.

---

## 4.13 Current User Abstraction

```java
public interface CurrentUser {

    UUID getUserId();

    boolean isAdmin();
}
```

Possible implementation:

```java
@Component
public class SpringSecurityCurrentUser
        implements CurrentUser {

    @Override
    public UUID getUserId() {

        Authentication authentication =
            SecurityContextHolder
                .getContext()
                .getAuthentication();

        CustomUserDetails principal =
            (CustomUserDetails)
                authentication.getPrincipal();

        return principal.getId();
    }

    @Override
    public boolean isAdmin() {

        Authentication authentication =
            SecurityContextHolder
                .getContext()
                .getAuthentication();

        return authentication.getAuthorities()
            .stream()
            .anyMatch(authority ->
                authority.getAuthority()
                    .equals("ROLE_ADMIN")
            );
    }
}
```

A production implementation should deliberately handle assumptions such as anonymous authentication and unexpected principal types.

---

## 4.14 Resource Authorization in Service

```java
@Transactional
public void deleteProperty(UUID propertyId) {

    Property property =
        propertyRepository.findById(propertyId)
            .orElseThrow(
                () -> new PropertyNotFoundException(
                    propertyId
                )
            );

    UUID currentUserId =
        currentUser.getUserId();

    boolean ownsProperty =
        property.getAgent()
            .getId()
            .equals(currentUserId);

    if (!currentUser.isAdmin()
            && !ownsProperty) {

        throw new AccessDeniedException(
            "Access denied"
        );
    }

    propertyRepository.delete(property);
}
```

Policy:

```text
ADMIN
→ may delete any property

AGENT
→ must own property
```

This is explicit business/security policy.

---

## 4.15 Extracting Reusable Authorization Policy

If many operations repeat:

```text
is admin?
or
owns property?
```

a dedicated component may emerge:

```java
@Component
public class PropertyAuthorization {

    public boolean canModify(
            Property property,
            CurrentUser currentUser) {

        if (currentUser.isAdmin()) {
            return true;
        }

        return property.getAgent()
            .getId()
            .equals(currentUser.getUserId());
    }
}
```

Do not create this abstraction merely because it might be useful someday.

Extract it when authorization policy becomes a real reusable responsibility.

---

## 4.16 JWT Filter Concept

A JWT authentication filter conceptually performs:

```text
HTTP request
↓
extract Bearer token
↓
validate token
↓
identify principal / authorities
↓
construct Authentication
↓
place Authentication in SecurityContext
↓
continue filter chain
```

It acts as an adapter between:

```text
HTTP token representation
```

and:

```text
Spring Security Authentication
```

JWT parsing should not be repeated inside controllers.

---

## 4.17 Security Error Configuration

Conceptually:

```java
http.exceptionHandling(exceptions -> exceptions
    .authenticationEntryPoint(
        authenticationEntryPoint
    )
    .accessDeniedHandler(
        accessDeniedHandler
    )
);
```

For a REST API:

```text
AuthenticationEntryPoint
→ JSON 401 response

AccessDeniedHandler
→ JSON 403 response
```

Application exceptions may still use:

```java
@RestControllerAdvice
```

for errors arising after the security filter boundary.

---

# 5. Request & Data Flow

## 5.1 Registration Flow

```text
POST /auth/register
↓
permitAll
↓
RegisterRequest
↓
DTO validation
↓
AuthService / registration service
↓
business validation
├── email uniqueness
└── allowed registration policy
↓
PasswordEncoder.encode(raw password)
↓
server chooses safe role
↓
UserRepository.save(...)
↓
Hibernate/JPA
↓
PostgreSQL
```

Key rule:

```text
client input does not decide privileged role
```

---

## 5.2 Username/Password Login Flow

```text
POST /auth/login
↓
permitAll
↓
email + raw password
↓
AuthenticationManager
↓
DaoAuthenticationProvider
├── UserDetailsService
│   ↓
│   UserRepository
│   ↓
│   PostgreSQL
│   ↓
│   CustomUserDetails
│
└── PasswordEncoder
    ↓
    matches(raw, storedHash)
↓
successful Authentication
↓
generate session/JWT mechanism
↓
response
```

`permitAll()` permits access to the login operation.

It does not mean credentials are automatically accepted.

---

## 5.3 Session-Based Later Request

```text
HTTP Request
Cookie: JSESSIONID=ABC123
↓
Spring Security
↓
resolve server-side session
↓
restore authentication/security state
↓
SecurityContext
↓
request authorization
↓
Controller
```

If the session is lost or invalid, authenticated state cannot be restored from that session identifier.

---

## 5.4 JWT-Based Later Request

```text
DELETE /properties/42
Authorization: Bearer <JWT>
↓
JWT authentication filter
↓
validate signature
↓
validate expiry / required claims
↓
construct Authentication
Sarah / ROLE_AGENT
↓
SecurityContext
↓
request authorization
↓
Controller
↓
Service
↓
resource authorization
```

---

## 5.5 Property Delete Flow

Assume:

```text
Sarah = ROLE_AGENT
Property 42 owner = David
```

Flow:

```text
DELETE /properties/42
↓
JWT/session establishes Sarah
↓
SecurityContext
↓
DELETE allows AGENT or ADMIN
↓
PASS
↓
PropertyController
↓
PropertyService
↓
PropertyRepository.findById(42)
↓
Property owner = David
↓
CurrentUser = Sarah
↓
Sarah is ADMIN?
NO
↓
Sarah owns Property 42?
NO
↓
Access denied
↓
403
```

---

## 5.6 Property Not Found Flow

Suppose David is authenticated and authorized to attempt deletion, but the requested property does not exist:

```text
DELETE /properties/42
↓
authentication
PASS
↓
role authorization
PASS
↓
PropertyRepository.findById(42)
↓
not found
↓
404
```

There is no resource on which to evaluate ownership.

Later checks depend on facts established by earlier stages.

---

## 5.7 Security Failure Flow

Unauthenticated:

```text
protected request
↓
no acceptable Authentication
↓
AuthenticationEntryPoint
↓
401
```

Wrong role:

```text
authenticated request
↓
required authority absent
↓
AccessDeniedHandler
↓
403
```

Ownership failure in application/service logic:

```text
request-level security
PASS
↓
Controller
↓
Service
↓
ownership policy fails
↓
application/security exception translation
↓
403
```

The exact handler path differs by boundary even when the final HTTP status is the same.

---

# 6. BrightMove Example

## 6.1 Security Requirements

BrightMove uses:

```text
CUSTOMER
AGENT
ADMIN
```

Simplified policy:

```text
PUBLIC
- register
- login
- browse properties

CUSTOMER
- authenticated customer operations
- cannot create/edit/delete properties

AGENT
- create properties
- edit/delete own properties

ADMIN
- administrative operations
- edit/delete any property
```

---

## 6.2 Request-Level Policy

Representative filter-chain rules:

```text
/auth/**
→ public

GET /properties/**
→ public

POST /properties/**
→ ROLE_AGENT

DELETE /properties/**
→ ROLE_AGENT or ROLE_ADMIN

other protected endpoints
→ authenticated
```

This policy answers:

> Which categories of authenticated users may reach this type of operation?

It does not answer:

> Does this Agent own this Property?

---

## 6.3 Secure Property Creation

Bad design:

```java
@PostMapping("/properties")
public PropertyResponse createProperty(
        @RequestBody CreatePropertyRequest request,
        @RequestParam UUID userId,
        @RequestParam String role,
        @RequestHeader("Authorization") String token) {

    if (!jwtService.isValid(token)) {
        throw new RuntimeException("Invalid token");
    }

    if (!role.equals("AGENT")) {
        throw new RuntimeException("Not allowed");
    }

    User user = userRepository
        .findById(userId)
        .orElseThrow();

    // ...
}
```

Problems:

```text
controller manually validates JWT
client supplies identity
client supplies role
controller performs authorization
controller accesses repositories directly
controller owns business workflow
generic RuntimeException loses error semantics
```

Better boundary:

```java
@PostMapping("/properties")
public PropertyResponse createProperty(
        @Valid @RequestBody
        CreatePropertyRequest request) {

    return propertyService.createProperty(request);
}
```

Then:

```text
Spring Security
→ validates/restores authentication
→ verifies ROLE_AGENT

PropertyService
→ obtains trusted current Agent identity
→ applies business rules
→ creates Property associated with authenticated Agent

PropertyRepository
→ persists
```

The client controls property data, not the identity of the Agent who owns the new property.

---

## 6.4 Secure Property Deletion

Controller:

```java
@DeleteMapping("/properties/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteProperty(
        @PathVariable UUID id) {

    propertyService.deleteProperty(id);
}
```

Service:

```java
@Transactional
public void deleteProperty(UUID propertyId) {

    Property property =
        propertyRepository.findById(propertyId)
            .orElseThrow(
                () -> new PropertyNotFoundException(
                    propertyId
                )
            );

    boolean ownsProperty =
        property.getAgent()
            .getId()
            .equals(currentUser.getUserId());

    if (!currentUser.isAdmin()
            && !ownsProperty) {
        throw new AccessDeniedException(
            "Access denied"
        );
    }

    propertyRepository.delete(property);
}
```

Responsibilities:

```text
SecurityFilterChain
→ Agent/Admin may attempt deletion

PropertyService
→ resource-specific authorization

PropertyRepository
→ persistence

CurrentUser
→ trusted authenticated identity
```

---

## 6.5 PostgreSQL and Ownership

Ownership ultimately comes from persisted domain data.

For example:

```text
properties
-------------------------------------
id      title          agent_id
P42     Flat 42        DAVID_ID
```

The service compares:

```text
current authenticated user ID
vs
properties.agent_id
```

Security therefore interacts with persistence:

```text
Spring Security
→ who is requesting?

PostgreSQL/domain state
→ who owns the resource?

service authorization policy
→ is this operation permitted?
```

---

## 6.6 Alternative Ownership Query

A repository could expose:

```java
Optional<Property> findByIdAndAgentId(
    UUID propertyId,
    UUID agentId
);
```

For an Agent:

```text
SELECT property
WHERE property.id = ?
AND property.agent.id = ?
```

Advantages:

```text
ownership filtering pushed into query
may avoid exposing resources not owned by user
can reduce unnecessary data access
```

Trade-off:

If nothing is returned, the application cannot directly distinguish:

```text
property does not exist
```

from:

```text
property exists but belongs to another Agent
```

Sometimes collapsing these cases is desirable for security.

Sometimes the API needs distinct semantics.

---

## 6.7 BrightMove Error Contract

Representative mapping:

```text
malformed request
→ 400

Bean Validation failure
→ 400

invalid login credentials
→ 401

missing/invalid/expired authentication
→ 401

authenticated but wrong role
→ 403

authenticated but ownership policy fails
→ 403

resource not found
→ 404

optimistic locking conflict
→ 409

unexpected server error
→ 500
```

A consistent error DTO may be returned even though different infrastructure components create the responses.

---

# 7. Common Mistakes

## 7.1 Trusting Client-Supplied User ID

Wrong:

```java
@RequestParam UUID agentId
```

used to decide ownership.

Why wrong:

```text
client controls agentId
↓
client can impersonate another resource owner
```

Correct source:

```text
authenticated principal
```

---

## 7.2 Trusting Client-Supplied Role

Wrong:

```java
@RequestParam String role
```

followed by:

```java
if (role.equals("AGENT")) {
    // allow
}
```

The client is supplying the answer to the authorization question.

Use trusted authorities from `Authentication`.

---

## 7.3 Parsing JWT in Every Controller

Wrong architecture:

```text
Controller A → parse JWT
Controller B → parse JWT
Controller C → parse JWT
```

JWT authentication belongs in the security pipeline.

Controllers should operate after authentication has already been established.

---

## 7.4 Confusing JWT With `Authentication`

Wrong:

```text
JWT = Authentication
```

Correct:

```text
JWT
↓ validated
Authentication
↓ stored for current processing
SecurityContext
```

---

## 7.5 Confusing `UserDetailsService` With Authentication

Wrong:

```text
UserDetailsService authenticates user
```

Correct:

```text
UserDetailsService
→ loads stored security user information

PasswordEncoder
→ verifies submitted password against stored hash

DaoAuthenticationProvider
→ coordinates username/password authentication
```

---

## 7.6 Comparing Encoded Password Strings

Wrong:

```java
passwordEncoder.encode(rawPassword)
    .equals(storedPassword);
```

BCrypt uses salt, so repeated encodings need not be identical.

Correct:

```java
passwordEncoder.matches(
    rawPassword,
    storedPassword
);
```

---

## 7.7 Storing Plaintext Passwords

Never store:

```text
secret123
```

as the database password value.

Store a suitable password hash produced by a password encoder.

---

## 7.8 Thinking Role Means Ownership

Wrong:

```text
Sarah has ROLE_AGENT
therefore Sarah may delete every Property
```

Correct:

```text
ROLE_AGENT
→ may be allowed to attempt deletion

ownership/resource policy
→ determines which Properties Sarah may delete
```

---

## 7.9 Performing Ownership Check Before Resource Existence Is Established

If:

```text
Property 42 does not exist
```

there is no owner to compare.

Typical flow:

```text
load resource
↓
not found?
→ 404

otherwise
↓
evaluate resource authorization
```

Exact policy can vary when existence itself must be hidden.

---

## 7.10 Assuming `SecurityContextHolder` Is One Global User

Wrong mental model:

```text
server.currentUser = Sarah
```

The server processes concurrent users.

Think in terms of authentication associated with the current security/request processing context.

---

## 7.11 Scattering `SecurityContextHolder` Everywhere

Technically possible:

```text
PropertyService
ViewingService
OfferService
MessageService
↓
all directly use SecurityContextHolder
```

This creates widespread Spring Security coupling.

If current-user access becomes a repeated application responsibility, consider an application-level abstraction such as `CurrentUser`.

Do not introduce the abstraction prematurely.

---

## 7.12 Returning 403 for Missing Authentication

Use the conceptual distinction:

```text
authentication missing/invalid
→ 401

authenticated but forbidden
→ 403
```

---

## 7.13 Returning 401 for Every Security Failure

An authenticated user with insufficient authority is not an authentication failure.

Example:

```text
ROLE_CUSTOMER
→ POST /properties
→ 403
```

---

## 7.14 Assuming `@RestControllerAdvice` Handles Every Security Failure

Some failures happen before the controller:

```text
Request
↓
Spring Security Filter
↓
rejected
```

These may use Spring Security mechanisms such as:

```text
AuthenticationEntryPoint
AccessDeniedHandler
```

Application exceptions and filter-chain security failures are different boundaries.

---

## 7.15 Throwing Generic `RuntimeException`

Wrong:

```java
throw new RuntimeException("Not allowed");
```

This loses semantic information.

Prefer meaningful error categories so the API can deliberately produce:

```text
401
403
404
409
...
```

---

## 7.16 Leaking Login Information

Avoid unnecessarily revealing:

```text
"This email exists but the password is wrong."
```

Generic external responses such as:

```text
Invalid email or password
```

can reduce user-enumeration information leakage.

Internal logs can retain appropriate diagnostic information.

---

## 7.17 Assuming JWT Payload Is Secret

A signed JWT payload is generally inspectable by the client.

Do not place sensitive secrets in it merely because it appears encoded.

---

## 7.18 Decoding JWT Without Validating It

Wrong:

```text
decode
↓
read role
↓
trust role
```

Correct:

```text
validate signature
validate expiry
validate required claims
↓
trust relevant claims
```

---

## 7.19 Letting Public Registration Assign `ADMIN`

Public input must not directly control privileged authorization state.

Role assignment is server policy.

---

## 7.20 Assuming JWT Makes the Entire System Stateless

Self-contained JWT access tokens can remove the need for a server-side session lookup on each request.

Real systems may still maintain state for:

```text
refresh tokens
revocation
logout
key management
account status
security auditing
```

"JWT is stateless" is an architectural simplification, not a universal description of the entire security system.

---

# 8. Engineering Trade-offs

## 8.1 Sessions vs JWT

### Server-Side Session

Client carries:

```text
session ID
```

Server maintains:

```text
session ID → authentication/session state
```

Advantages may include:

```text
central invalidation
straightforward server-side state changes
mature browser/session support
```

Costs may include:

```text
shared session storage in distributed deployments
or sticky-session routing
server-side session lifecycle management
```

### Self-Contained JWT

Client carries:

```text
signed claims
```

Server can often validate locally.

Advantages may include:

```text
convenient distributed validation
reduced need for per-request shared-session lookup
useful for APIs and distributed services
```

Costs include:

```text
revocation complexity
stale embedded role/permission claims
token/key lifecycle concerns
cryptographic validation cost
```

Neither is universally better.

Choose according to system requirements.

---

## 8.2 Distributed Session Example

Three servers:

```text
              Load Balancer
             /      |      \
            ↓       ↓       ↓
        Server A Server B Server C
```

If sessions exist only in each server's local memory:

```text
Server A:
ABC123 → Sarah

Server B:
no ABC123
```

a request routed to Server B cannot resolve Server A's local session.

Possible strategies:

```text
shared session store
sticky sessions
token-based approaches
```

Each introduces different operational trade-offs.

---

## 8.3 JWT and Performance

A JWT may avoid:

```text
application server
↓
remote shared session store
↓
session lookup
```

for each request.

However:

```text
JWT validation
→ cryptographic computation

Redis/session stores
→ can be extremely fast

real token systems
→ may still perform state lookups
```

Therefore:

> Do not conclude that JWT is inherently faster than sessions.

Measure the architecture that actually exists.

---

## 8.4 JWT and Role Changes

Suppose a JWT contains:

```text
ROLE_AGENT
```

and PostgreSQL later changes Sarah to:

```text
ROLE_CUSTOMER
```

A self-contained JWT may continue carrying the old role until expiry unless the architecture includes additional validation/revocation mechanisms.

Trade-off:

```text
less per-request server lookup
↔
potentially staler authorization claims
```

---

## 8.5 JWT Revocation

If a self-contained token is valid for one hour, immediate revocation is not automatic merely because the database changed.

Possible architectural responses include:

```text
short token lifetimes
revocation state
token versioning
refresh-token strategies
account-state checks
```

Each reintroduces different amounts of server-side state and complexity.

---

## 8.6 Request-Level vs Method-Level Authorization

Request-level:

```text
SecurityFilterChain
→ protects HTTP route
```

Method-level:

```text
@PreAuthorize
→ protects method invocation
```

Request-level authorization is clear for HTTP API policy.

Method-level authorization can keep protection attached to sensitive operations used from multiple entry points.

Too much duplicated authorization can make policy difficult to understand.

Choose deliberate ownership.

---

## 8.7 Explicit Service Ownership Check vs Authorization Component

Inline service rule:

```java
if (!currentUser.isAdmin()
        && !property.getAgent().getId()
            .equals(currentUser.getUserId())) {
    throw new AccessDeniedException(...);
}
```

Advantages:

```text
visible
easy to understand
good for small number of rules
```

Dedicated authorization component:

```text
PropertyAuthorization.canModify(...)
```

Advantages:

```text
reusable policy
centralized domain authorization logic
less duplication
```

Cost:

```text
more indirection
another abstraction to understand
```

Extract when a real reusable responsibility appears.

---

## 8.8 Direct `SecurityContextHolder` vs `CurrentUser`

Direct:

```java
SecurityContextHolder
    .getContext()
    .getAuthentication();
```

Advantages:

```text
simple
less code
```

Costs:

```text
Spring Security coupling inside application services
repeated extraction/casting logic
harder isolated testing
```

Application abstraction:

```text
CurrentUser
```

Advantages:

```text
clear boundary
testability
centralized principal interpretation
```

Cost:

```text
additional abstraction
```

Use proportional design.

---

## 8.9 Load Then Authorize vs Ownership Query

Approach A:

```text
findById(propertyId)
↓
resource exists?
↓
compare owner
```

Allows explicit distinction:

```text
404 not found
vs
403 forbidden
```

Approach B:

```text
findByIdAndAgentId(propertyId, currentUserId)
```

Can collapse:

```text
not found
and
not owned
```

Advantages may include reduced information leakage and query-level filtering.

Trade-off is loss of distinction.

Security semantics should be intentional.

---

## 8.10 403 vs 404 for Hidden Resources

Some applications return:

```text
403
```

when a resource exists but the authenticated user lacks permission.

Others deliberately return:

```text
404
```

to avoid revealing that the resource exists.

This is a security/API policy decision.

For the BrightMove learning model:

```text
ownership denial
→ 403
```

unless requirements explicitly choose otherwise.

---

## 8.11 Domain Entity Implements `UserDetails` vs Adapter

Direct coupling:

```text
User implements UserDetails
```

Advantages:

```text
less mapping code
```

Costs:

```text
domain model coupled to Spring Security
security framework concerns enter persistence/domain entity
```

Adapter:

```text
User
↓
CustomUserDetails
```

Advantages:

```text
clear boundary
domain remains framework-independent
```

Cost:

```text
additional class
```

For BrightMove training, the adapter makes responsibilities easier to see.

---

# 9. Summary

## 9.1 Core Questions

```text
Authentication
→ Who are you?

Authorization
→ What operations may you perform?

Resource authorization
→ May you perform this operation on this object?
```

---

## 9.2 Core Spring Components

```text
Authentication
→ identity + authorities + authentication state

AuthenticationManager
→ coordinates authentication

AuthenticationProvider
→ performs authentication strategy

UserDetailsService
→ loads stored security user information

UserDetails
→ Spring Security representation of user

PasswordEncoder
→ hashes/verifies passwords

SecurityContext
→ holds current Authentication

SecurityContextHolder
→ provides access to current context

SecurityFilterChain
→ request-level security policy

AuthenticationEntryPoint
→ handles unauthenticated protected requests

AccessDeniedHandler
→ handles request-level access denial
```

---

## 9.3 Password Model

```text
REGISTRATION

raw password
↓
PasswordEncoder.encode
↓
stored hash
```

```text
LOGIN

raw submitted password
+
stored hash
↓
PasswordEncoder.matches
↓
valid / invalid
```

Never store plaintext passwords.

---

## 9.4 Roles and Authorities

```text
hasRole("AGENT")
```

conventionally checks:

```text
ROLE_AGENT
```

Roles are broad categories represented through authorities.

Authorities can also express finer-grained permissions.

Role authorization does not prove resource ownership.

---

## 9.5 Trusted Identity Rule

Never base authorization on client claims such as:

```text
userId
agentId
role
owner
```

when those values are supposed to represent the authenticated user.

Use:

```text
authenticated principal
trusted authorities
persisted domain ownership
```

The fundamental comparison is:

```text
authenticated user
vs
resource owner
```

---

## 9.6 Session / JWT Model

Session:

```text
client session ID
↓
server-side session state
↓
Authentication
↓
SecurityContext
```

JWT:

```text
client signed token
↓
validate token
↓
Authentication
↓
SecurityContext
```

Different across-request mechanisms converge on the same Spring Security abstraction.

---

## 9.7 JWT Rules

```text
JWT ≠ Authentication
JWT ≠ SecurityContext
JWT ≠ authorization
```

A signed JWT payload is not automatically secret.

Validate before trusting:

```text
signature
expiry
required claims
```

---

## 9.8 Error Model

```text
401
→ acceptable authentication missing/invalid

403
→ authenticated but not authorized

404
→ resource not found
```

Typical BrightMove examples:

```text
missing JWT
→ 401

expired JWT
→ 401

CUSTOMER calls AGENT operation
→ 403

AGENT modifies another Agent's property
→ 403

Property does not exist
→ 404
```

---

## 9.9 Responsibility Map

```text
User entity
→ persisted/domain account state

UserRepository
→ persistence

CustomUserDetails
→ domain-to-security adapter

UserDetailsService
→ load security user

PasswordEncoder
→ encode/verify password

DaoAuthenticationProvider
→ username/password authentication strategy

AuthenticationManager
→ authentication coordinator

Authentication
→ authenticated identity and authorities

session / JWT
→ authentication continuity across requests

SecurityContext
→ current Authentication

SecurityFilterChain
→ HTTP security policy

CurrentUser
→ trusted identity exposed to application

PropertyService
→ business workflow + resource authorization

PropertyRepository
→ property persistence

AuthenticationEntryPoint
→ 401 security-boundary response

AccessDeniedHandler
→ 403 request-security response
```

---

## 9.10 Final Engineering Model

```text
                     CLIENT
                       │
                       ▼
                  HTTP REQUEST
                       │
                       ▼
             SPRING SECURITY FILTERS
                       │
             session / JWT handling
                       │
                       ▼
                Authentication
             /                    \
       principal                authorities
          │                         │
          └────────────┬────────────┘
                       ▼
                SecurityContext
                       │
                       ▼
             request authorization
                       │
               ┌───────┴───────┐
               │               │
             FAIL             PASS
               │               │
             401/403            ▼
                            Controller
                                │
                                ▼
                             Service
                                │
                      resource authorization
                                │
                         ┌──────┴──────┐
                         │             │
                       FAIL          PASS
                         │             │
                        403            ▼
                               business workflow
                                       │
                                       ▼
                                   Repository
                                       │
                                       ▼
                                  PostgreSQL
```

The central principle to retain is:

> **Spring Security provides mechanisms for establishing identity and enforcing access, but the application defines its security policy. Trusted identity comes from authentication, permissions come from trusted authorities and domain state, and resource ownership must never be inferred from client-supplied claims.**

---

# End of Module 3.15 — Spring Security
