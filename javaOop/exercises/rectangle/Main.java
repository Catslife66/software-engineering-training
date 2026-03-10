package javaOop.exercises.rectangle;

public class Main {
    public static void main(String[] args){
        Rectangle rect1 = new Rectangle(5, 3);
        Rectangle rect2 = new Rectangle(4, 2.5);

        System.out.println("Rect1 Area: " + rect1.getArea());
        System.out.println("Rect1 Perimeter: " + rect1.getPerimeter());

        System.out.println("Rect2 Area: " + rect2.getArea());
        System.out.println("Rect2 Perimeter: " + rect2.getPerimeter());
        
    }
}
