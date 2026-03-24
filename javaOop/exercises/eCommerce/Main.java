package javaOop.exercises.eCommerce;

public class Main {
    public static void main(String[] args) {
        PaymentService service1 = new PaymentService(new CardPayment());
        PaymentService service2 = new PaymentService(new CashPayment());

        service1.pay(34);
        service2.pay(50);

        Delivery d1 = new StandardDelivery();
        Delivery d2 = new ExpressDelivery();

        Order order1 = new Order(50, "pending", d1);
        Order order2 = new Order(50, "pending", d2);

        System.out.println(order1.getTotalPrice());
        System.out.println(order2.getTotalPrice());
    }
}
