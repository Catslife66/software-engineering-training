package javaOop.exercises.eCommerce;

public class StandardDelivery implements Delivery {
    @Override
    public double calculateFee(double subtotal){
        if(subtotal >= 50){
            return 0;
        }
        return 3.99;
    }
}
