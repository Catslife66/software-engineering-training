package javaOop.exercises.book;

public class Book{
    private final String title;
    private final String author;
    private double price;

    public Book(String title, String author, double price){
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public void displayInfo(){
        System.out.println(title + " by " + author + " - £" + price);
    }
}