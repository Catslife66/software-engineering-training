package main.java.com.example.demo.service;

import main.java.com.example.demo.dto.LoginResponseDTO;
import main.java.com.example.demo.dto.Result;
import main.java.com.example.demo.model.User;
import main.java.com.example.demo.repository.UserRepository;

import main.java.com.example.demo.dto.UserResponseDTO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserService {
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

    public Result<Void> register(String email, String password){
        if (email == null || email.isBlank()){
            return Result.failure("INVALID_INPUT", "Email is required.");
        }
        if(password == null || password.isBlank()){
            return Result.failure("INVALID_INPUT", "Password is required.");
        }

        User existingUser = userRepository.findByEmail(email);

        if(existingUser != null){
            return Result.failure("USER_CONFLICT", "This email has been registered.");
        }
        
        String hashedPassword = passwordService.hashPassword(password);

        User user = new User(email, hashedPassword);

        userRepository.save(user);

        return Result.success("User registered.", null);
    }

    public Result<LoginResponseDTO> login(String email, String password){
        if (email == null || email.isBlank()){
            return Result.failure("INVALID_INPUT", "Email is required.");
        }
        if(password == null || password.isBlank()){
            return Result.failure("INVALID_INPUT", "Password is required.");
        }

        User user = userRepository.findByEmail(email);

        if(user == null){
            return Result.failure("UNAUTHORIZED", "Invalid credentials.");
        }

        if(!passwordService.matches(password, user.getPassword())){
            return Result.failure("UNAUTHORIZED", "Invalid credentials.");
        }

        String token = tokenService.generateToken(user);

        LoginResponseDTO loginResponse = new LoginResponseDTO(token);

        return Result.success("You are logged in.", loginResponse);

    }

    public Result<UserResponseDTO> getByEmail(String email){
        if (email == null || email.isBlank()){
            return Result.failure("INVALID_INPUT", "Email is required.");
        }

        User existingUser = userRepository.findByEmail(email);

        if(existingUser == null){
            return Result.failure("USER_NOT_FOUND", "This user is not found.");
        }

        UserResponseDTO userResponse = new UserResponseDTO(
            existingUser.getId(),
            existingUser.getEmail()
        );

        return Result.success("User is found.", userResponse);
    }

    public Result<List<UserResponseDTO>> getAll(){
        List<User> users = userRepository.findAll();

        List<UserResponseDTO> userResponseDTOs = new ArrayList<>();

        for (User user : users) {
            userResponseDTOs.add(
                new UserResponseDTO(
                    user.getId(),
                    user.getEmail()
                )
            );
        }

        return Result.success("All users", userResponseDTOs)
    }
}
