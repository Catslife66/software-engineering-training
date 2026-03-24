package javaOop.exercises.eCommerce;

public class PaymentService {
    private PaymentStrategy paymentStrategy;

    public PaymentService(PaymentStrategy paymentStrategy){
        this.paymentStrategy = paymentStrategy;
    }
    
    public void pay(double amount){
        paymentStrategy.process(amount);
    };
}
