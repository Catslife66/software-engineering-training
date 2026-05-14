package main.java.com.example.demo.service;

@Service
public class BcryptPasswordService implements PasswordService{
    @Override
    public String hashPassword(String password){
        return "xxxxx";
    }    
    @Override
    public boolean matches(String rawPassword, String hashedPassword){
        return true;
    }
}
