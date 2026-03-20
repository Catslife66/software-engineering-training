package javaOop.exercises.eCommerce;

public class StandardDelivery implements Delivery {

    @Override
    public double calculateFee(){
        return 3.99;
    }
}
