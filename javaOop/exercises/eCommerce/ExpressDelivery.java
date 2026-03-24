package javaOop.exercises.eCommerce;

public class ExpressDelivery implements Delivery{
    @Override
    public double calculateFee(double subtotal){
        if(subtotal >= 100){
            return 0;
        }
        return 7.99;
    }
}
