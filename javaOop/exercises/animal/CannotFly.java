package javaOop.exercises.animal;

public class CannotFly implements FlyBehaviour {
    @Override
    public void fly(){
        System.out.println("Cannot fly");
    }
}
