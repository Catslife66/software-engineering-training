package main.java.com.example.demo.controller;

import main.java.com.example.demo.dto.LoginRequestDTO;
import main.java.com.example.demo.dto.LoginResponseDTO;
import main.java.com.example.demo.dto.Result;
import main.java.com.example.demo.dto.UserRegisterRequestDTO;
import main.java.com.example.demo.dto.UserResponseDTO;
import main.java.com.example.demo.service.UserService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Result<List<UserResponseDTO>>> getAll(){
        Result<List<UserResponseDTO>> result = userService.getAll();

        if(result.isSuccess()){
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @GetMapping
    public ResponseEntity<Result<UserResponseDTO>> getByEmail(@RequestParam String email){
        Result<UserResponseDTO> result = userService.getByEmail(email);

        if(result.isSuccess()){
            return ResponseEntity.ok(result);
        }

        if ("USER_NOT_FOUND".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }

        if ("INVALID_INPUT".equals(result.getCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<Result<Void>> register(
            @RequestBody UserRegisterRequestDTO request
    ) {
        Result<Void> result = userService.register(
                request.getEmail(),
                request.getPassword()
        );
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

    @PostMapping("/login")
    public ResponseEntity<Result<LoginResponseDTO>> login(
        @RequestBody LoginRequestDTO request
    ){
        Result<LoginResponseDTO> result = userService.login(request.getEmail(), request.getPassword());

        if(result.isSuccess()){
            return ResponseEntity.ok(result);
        }
        if("INVALID_INPUT".equals(result.getCode())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        if("UNAUTHORIZED".equals(result.getCode())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
    
}
