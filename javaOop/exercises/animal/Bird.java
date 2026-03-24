package javaOop.exercises.animal;

public class Bird {
    private FlyBehaviour flyBehaviour;

    public Bird(FlyBehaviour flyBehaviour){
        this.flyBehaviour = flyBehaviour;
    }

    public void performFly(){
        flyBehaviour.fly();
    }
}
