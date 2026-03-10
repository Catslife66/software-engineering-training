package javaOop.exercises.book;

public class Main{
    public static void main(String[] args){
        Book book1 = new Book("Harry Porter", "JK Rowling", 12.99);
        Book book2 = new Book("ABC", "Unknown", 8.99);

        book1.displayInfo();
        book2.displayInfo();
    }
}