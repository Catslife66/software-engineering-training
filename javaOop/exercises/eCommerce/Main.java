package javaOop.exercises.eCommerce;

public class Main {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();

        service.pay(new CardPayment(), 34);
        service.pay(new CashPayment(), 50);
    }
}
