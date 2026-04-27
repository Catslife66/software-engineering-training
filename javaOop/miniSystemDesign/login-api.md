# Login API

Design:

```
POST /users/login
```

**Requirements**

input:

- email
- password
  system:
- validate input
- check user exists
- verify password
- return success/failure

Same structure as before:

- Model (if needed)
- DTOs
- Repository
- Service logic (step-by-step)
- Result handling
- Controller responsibility

Model

reuse User entity

## DTOs

```
public class LoginRequestDTO {
    private String email;
    private String password;

    public getEmail(){
        return email;
    }
    public getPassword(){
        return password;
    }
}

public class LoginResponseDTO{
    private String token;

    public LoginResponseDTO(String token){
        this.token = token;
    }

    public getToken(){
        return token;
    }
}
```

Repository

@Repository
public interface UserRepository {
User findByEmail(String email);
void save(User user);
}

## Result

```
public class Result<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;

    public Result(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, "SUCCESS", message, data);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(false, code, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
```

## Service

- user send login request {email, password}
- controller receives request and passes to service
- service validates input data
  check if user exists
  verify password
  if checks are past, create access token, return success
  if checks are failed, return failure
- controller sends success result with token to client or failure result to client

```
@Service
public class UserService{
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final TokenService tokenService;

    public UserService(
        UserRepository userRepository,
        PasswordService passwordService,
        TokenService tokenService
        ){
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.tokenService = tokenService;
    }

    public Result registerUser(String email, String password){
        ....
    }

    public Result<LoginResponseDTO> loginUser(String email, String password){
         if (email == null || email.isBlank()) {
            return Result.failure("INVALID_INPUT", "Email is required.");
        }

        if (password == null || password.isBlank()) {
            return Result.failure("INVALID_INPUT", "Password is required.");
        }

        User user = userRepository.findByEmail(email);

        if(user == null){
            return Result.failure("UNAUTHORIZED", "Invalid credentials.")
        }

        boolean matches = passwordService.matches(password, user.getPassword())

        if(!matches){
            return Result.failure("UNAUTHORIZED", "Invalid credentials.")
        }

        String token = tokenService.generateToken(user);

        return Result.success(
            "You have logged in.",
            new LoginResponseDTO(token)
        )
    }
}

public interface TokenService {
    String generateToken(User user);
}

@Service
public class JwtTokenService implements TokenService {
    @Override
    public String generateToken(User user) {
        // generate JWT here
        return "jwt-token";
    }
}
```

```
UserService = orchestrator
PasswordService = password verification
TokenService = token generation
```

## Controller

```
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/login")
    public ResponseEntity<Result<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO request
    ) {
        Result<LoginResponseDTO> result =
            userService.loginUser(request.getEmail(), request.getPassword());

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }

        if ("UNAUTHORIZED".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        if ("INVALID_INPUT".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
```

## Request body

```
{
    "email": "abc@example.com",
    "password": "1234abcd"
}
```

## After login, every request must be authenticated using JWT.

How does the client use the token?

After login, the client stores the JWT and sends it in future requests using the Authorization header:

```
Authorization: Bearer <token>
```

Where does the backend check the token?

- a security filter / authentication layer checks the token before the controller runs
- if token is valid, request continues
- if token is invalid, request is blocked early

```
Security layer extracts and validates token before request reaches controller
```

Which layer should handle token validation?

- security filter reads the header
- TokenService validates the JWT
- if valid, the request is treated as authenticated

```
Filter = request interception
TokenService = token validation logic
```

What happens if the token is invalid?

If token is invalid or missing for a protected route:

- backend stops request
- returns an authentication/authorization error

```
401 Unauthorized → token missing/invalid
403 Forbidden → token valid, but user lacks permission
```

## CHECKPOINT

> Why is PasswordService a better dependency for UserService than hashing the password directly inside UserService?

```
PasswordService is better because it separates sensitive password logic from business logic, improves maintainability, allows different implementations (e.g., hashing algorithms), and makes the system easier to test.
```

> Why should login return a token instead of user password?

```
Login should return a token, not a password, because passwords are sensitive and must never be exposed. The token lets the client prove authentication in future requests without sending credentials again.
```

> Why should login return the same error message for:
>
> user not found
> wrong password

```
Login should return the same error message for “user not found” and “wrong password” to avoid giving attackers information about which emails are valid. This reduces account enumeration risk.
```

> Which layer should generate the JWT token?
>
> Controller
> Service
> Repository

```
The service layer should coordinate login and call a dedicated TokenService to generate the JWT after authentication succeeds. The controller should not generate tokens because it only handles HTTP concerns, and the repository should not generate tokens because it only handles data access.
```

> Why should UserService call TokenService instead of generating JWT directly?

```
UserService should call TokenService instead of generating JWT directly because it keeps token-generation responsibility separate from login business flow. This improves maintainability, testability, and makes it easier to change token implementations later.
```

> If JWT format changes later, which class should ideally change?

```
TokenService or JwtTokenService should ideally change, because JWT generation belongs to that component, not to UserService.
```

> Is TokenService more similar to:

> Repository
> Controller
> Helper/Domain service

```
TokenService is most similar to a helper/domain service because it provides a specific piece of application logic — generating tokens — without handling HTTP requests or database access. It supports the service layer but is not itself a controller or repository.
```

> What is the difference between UserService and UserRepository in a login system?

```
UserService handles business logic and decides how the login process works, while UserRepository handles data access. The service uses the repository to retrieve user data, but the repository does not contain business logic.
```

> Why should UserRepository NOT contain password verification logic?

```
UserRepository should not contain password verification logic because it is responsible only for data access. Password verification is business logic and belongs in the service layer. Keeping this separation follows SRP and keeps the repository simple, reusable, and independent of business rules.
```

> What is the difference between authentication and authorization?

```
Authentication is verifying who the user is.
Authorization is deciding what the authenticated user is allowed to do.
```
