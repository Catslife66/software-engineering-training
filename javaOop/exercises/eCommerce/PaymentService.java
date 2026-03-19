package javaOop.exercises.eCommerce;

public class PaymentService {
    public void pay(Payment payment, double amount){
        payment.process(amount);
    };
}
