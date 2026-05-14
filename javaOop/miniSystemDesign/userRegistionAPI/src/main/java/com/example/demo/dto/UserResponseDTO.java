package main.java.com.example.demo.dto;

import java.util.UUID;

public class UserResponseDTO {
    private UUID id;
    private String email;

    public UserResponseDTO(UUID id, String email) {
        this.id = id;
        this.email = email;
    }
    
    public UUID getId(){
        return id;
    }
    public String getEmail(){
        return email;
    }
}
