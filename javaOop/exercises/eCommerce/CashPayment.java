package javaOop.exercises.eCommerce;

public class CashPayment implements PaymentStrategy {
    @Override
    public void process(double amount){
        System.out.println("Paid by cash: " + amount);
    }
}
