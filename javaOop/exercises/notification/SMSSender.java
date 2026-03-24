package javaOop.exercises.notification;

public class SMSSender implements NotificationSender{
    @Override
    public void send(String message){
        System.out.println("SMS message: " + message);
    }    
}
