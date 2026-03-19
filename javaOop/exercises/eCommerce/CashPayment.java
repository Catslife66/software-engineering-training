package javaOop.exercises.eCommerce;

public class CashPayment implements Payment {
    @Override
    public void process(double amount){
        System.out.println("Paid by cash: " + amount);
    }
}
