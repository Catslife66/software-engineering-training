package javaOop.exercises.eCommerce;

public class Product {
    private String name;
    private double price;

    public Product(String name, double price){
        this.name = name;
        setPrice(price);
    }

    public String getName(){
        return name;
    }

    public double getPrice(){
        return price;
    }

    public void setPrice(double price){
        if(price < 0){
            System.out.println("Invalid price.");
            return;
        }
        this.price = price;
    }
}
