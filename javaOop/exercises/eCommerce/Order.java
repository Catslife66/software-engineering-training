package javaOop.exercises.eCommerce;

public class Order {
    private double subtotal;
    private String status;
    private Delivery delivery;

    public Order(double subtotal, String status, Delivery delivery){
        setPrice(subtotal);
        setStatus(status);
        this.delivery = delivery;
    }
    public double getTotalPrice() {
        return subtotal + getDeliveryFee();
    }

    public String getStatus() {
        return status;
    }

    public Double getDeliveryFee(){
        return delivery.calculateFee(subtotal);
    }

    public void setPrice(double subtotal){
        if(subtotal < 0){
            System.out.println("Price cannot be negative");
            return;
        }
        this.subtotal = subtotal;
    }

    public void setStatus(String status){
        // check for null too
        // isBlank() to catch " "
        if(status == null || status.isBlank()){
            System.out.println("Status cannot be empty");
            return;
        }
        this.status = status;
    }


}

