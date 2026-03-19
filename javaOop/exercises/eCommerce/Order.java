package javaOop.exercises.eCommerce;

public class Order {
    private double totalPrice;
    private String status;

    public Order(double totalPrice, String status){
        setPrice(totalPrice);
        setStatus(status);
    }
    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setPrice(double totalPrice){
        if(totalPrice < 0){
            System.out.println("Price cannot be negative");
            return;
        }
        this.totalPrice = totalPrice;
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

