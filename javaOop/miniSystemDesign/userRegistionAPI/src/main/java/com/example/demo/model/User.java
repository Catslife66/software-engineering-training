package main.java.com.example.demo.model; 

class User {
    private String email;
    private String password;

    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public String getEmail(){
        return email;
    }

    public String password(){
        return password;
    }
}