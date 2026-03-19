package javaOop.exercises.eCommerce;

public class PercentageDiscount implements Discount {
    @Override
    public double applyDiscount(double price){
        return price * 0.9;
    }
}
