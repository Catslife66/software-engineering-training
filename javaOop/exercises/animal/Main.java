package javaOop.exercises.animal;

public class Main {
    public static void main(String[] args) {
        Bird sparrow = new Bird(new CanFly());
        Bird penguin = new Bird(new CannotFly());

        sparrow.performFly();
        penguin.performFly();
    }
}
