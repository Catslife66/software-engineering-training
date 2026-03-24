package javaOop.exercises.notification;

public class Main {
    public static void main(String[] args) {

        NotificationService service1 = new NotificationService(new EmailSender());
        NotificationService service2 = new NotificationService(new SMSSender());

        service1.nofifyUser("Hello");
        service2.nofifyUser("Hi");
    }
}
