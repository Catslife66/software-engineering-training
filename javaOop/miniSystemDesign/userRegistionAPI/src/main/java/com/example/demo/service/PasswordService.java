package main.java.com.example.demo.service;

public interface PasswordService {
    String hashPassword(String password);
    boolean matches(String rawPassword, String hashedPassword);
}
