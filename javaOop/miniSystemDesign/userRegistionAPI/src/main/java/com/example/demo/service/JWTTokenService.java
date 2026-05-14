package main.java.com.example.demo.service;

@Service
public class JWTTokenService implements TokenService {

    @Override
    public String generateToken(User user) {
        return "some-token";
    }

    @Override
    public boolean validateToken(String token) {
        return true;
    }
}
