package javaOop.exercises.notification;

public class NotificationService {
    private NotificationSender notificationSender;

    public NotificationService(NotificationSender notificationSender){
        this.notificationSender = notificationSender;
    }

    public void nofifyUser(String message){
        notificationSender.send(message);
    }   
}
