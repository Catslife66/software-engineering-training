package javaOop.exercises.eCommerce;

public class StandardDelivery extends Delivery {

    @Override
    public double calculateFee(){
        return 3.99;
    }
}
