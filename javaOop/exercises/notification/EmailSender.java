package javaOop.exercises.notification;

public class EmailSender implements NotificationSender {
    @Override
    public void send(String message){
        System.out.println("Email message: " + message);
    }
}
