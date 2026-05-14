package main.java.com.example.demo.reposiotry;

import java.util.List;

import com.example.demo.model.User;

public interface UserRepository {
    User findByEmail(String email);
    List<User> findAll();
    void save(User user);
} 