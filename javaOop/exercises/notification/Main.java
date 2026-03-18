package javaOop.exercises.notification;

public class Main {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();

        service.nofifyUser(new EmailNotification());
        service.nofifyUser(new SMSNotification());
    }
}
