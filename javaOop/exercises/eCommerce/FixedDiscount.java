package javaOop.exercises.eCommerce;

public class FixedDiscount implements Discount{
    @Override
    public double applyDiscount(double price){
        return price - 5;
    }
}