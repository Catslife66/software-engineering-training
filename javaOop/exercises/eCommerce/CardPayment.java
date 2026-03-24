package javaOop.exercises.eCommerce;

public class CardPayment implements PaymentStrategy{
    @Override
    public void process(double amount){
        System.out.println("Paid by card: " + amount);
    }
}
