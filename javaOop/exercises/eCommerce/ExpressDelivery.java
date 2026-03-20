package javaOop.exercises.eCommerce;

public class ExpressDelivery implements Delivery{

    @Override
    public double calculateFee(){
        return 7.99;
    }
}
