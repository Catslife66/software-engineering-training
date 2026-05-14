package main.java.com.example.demo.service;

public interface TokenService {
    String generateToken(User user);
    boolean validateToken(String token);
}
