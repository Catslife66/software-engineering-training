# User Registration

**Problem**

Design a simple User Registration API

```
POST /users/register
```

**Requirements**
user submits:

- email
- password

system should:

- validate input
- check if email already exists
- create user
- store user
- return success/failure

Answer these step by step:

1. What classes do you need?

   Think:
   - Controller
   - Service
   - Repository
   - DTOs
   - Model

2. What does each layer do?

   Explain responsibilities:
   - Controller
   - Service
   - Repository

3. What does the request look like?

   JSON request body

4. What does the service logic do?

   Step-by-step flow

5. What should the service return?

   (Result structure)

6. Any validation rules?

   Examples:
   - email format
   - password rules
   - duplicate email

### Model

public class User {
private String id;
private String email;
private String password;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

}

### DTOs

public class UserInDTO {
private String email;
private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

}

### Result

public class Result {
private Boolean success;
private String code;
private String message;

    public Result(Boolean success, String code, String message){
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public static Result success(String message){
        return new Result(true, "SUCCESS", message);
    }

    public static Result failure(String code, String message){
        return new Result(false, code, message);
    }

    public boolean isSuccess(){
        return success;
    }
    public String getCode(){
        return code;
    }
    public String getMessage(){
        return message;
    }

}

### Repository

// Handling querying user info from database and creating user data to database

@Repository
public interface UserRepository {
User findByEmail(String email);
void save(User user);
}

### Service

// Validate user input and check if user email is been registered
// Return success outcome when the checks are through and user instance is created
// Return failure outcome when the checks are failed

import org.springframework.stereotype.Service;

@Service
public class UserService {
private final UserRepository userRepository;

    public UserServive(
        UserRepository userRepository
    ){
        this.userRepository = userRepository;
    }

    public Result registerUser(String email, String password){
         if (email == null || email.isBlank()) {
            return Result.failure("INVALID_INPUT", "Email is required.");
        }

        if (password == null || password.isBlank()) {
            return Result.failure("INVALID_INPUT", "Password is required.");
        }

        if (password.length() < 8) {
            return Result.failure("INVALID_INPUT", "Password must be at least 8 characters.");
        }

        if (!password.matches(".*[A-Z].*")) {
            return Result.failure("INVALID_INPUT", "Password must contain at least one uppercase letter.");
        }

        if (!password.matches(".*\\d.*")) {
            return Result.failure("INVALID_INPUT", "Password must contain at least one number.");
        }

        User existingUser = userRepository.findByEmail(email);
        if(existingUser != null){
            return Result.failure("USER_CONFLICT", "This email has been registered.")
        }

        User user = new User(email, password)
        userRepository.save(user)

        retrun Result.success("You are registered successfully.")
    }

}

### Controller

// Handle receiving requests and returning responses

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.\*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/register")
    public ResponseEntity<Result> register(
            @RequestBody UserInDTO request
    ) {
        Result result = userService.registerUser(request.getEmail(), request.getPassword());

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }

        if ("USER_CONFLICT".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }

        if ("INVALID_INPUT".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

}

### Request body format

{
"email": "abc@example.com",
"password": "abcd1234"
}

## CHECKPINTS

> Why should UserService depend on UserRepository, but not on UserController?

```
UserService should depend on UserRepository because saving and retrieving user data is part of the service’s business workflow, and the repository provides the data-access capability it needs. It should not depend on UserController because the controller belongs to the web layer and is only responsible for handling HTTP requests and responses. Making the service depend on the controller would break layer separation and violate SRP.
```

> Why is UserInDTO different from User? Why not just use User as @RequestBody?

```
UserInDTO is used to define the structure of incoming request data, while User represents the internal domain model. We should not use User as @RequestBody because it exposes internal structure and may include fields that should not be controlled or visible to clients, such as id or sensitive data. Using a DTO keeps the API layer separate from the domain model, improving security, flexibility, and maintainability.
```

> After registering a user, should the API return:
>
> A) only Result
> B) Result + UserOutDTO

```
I would usually return only Result for a registration endpoint if the client only needs confirmation of success or failure. If the client needs the created user data immediately, I would return a UserOutDTO, but never the internal User entity.
```
