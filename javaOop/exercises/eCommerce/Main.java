package javaOop.exercises.eCommerce;

public class Main {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();

        service.pay(new CardPayment(), 34);
        service.pay(new CashPayment(), 50);

        Delivery d1 = new StandardDelivery();
        Delivery d2 = new ExpressDelivery();

        System.out.println(d1.calculateFee());
        System.out.println(d2.calculateFee());
    }
}
