package main.java.com.example.demo.reposiotry;

import com.example.demo.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository{
    private final List<User> users = new ArrayList<>();

    @Override
    public User findByEmail(String email){
        for(User user: users){
            if(user.getEmail().equals(email)){
                return user;
            }
        }
        return null;
    }

    @Override
    public List<User> findAll(){
        return users;
    }

    @Override
    public void save(User user){
        users.add(user);
    }
    
} 