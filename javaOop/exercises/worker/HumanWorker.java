package javaOop.exercises.worker;

public class HumanWorker implements Eatable, Sleepable, Workable{
    @Override
    public void eat(){
        System.out.println("I am eating...");
    }
    @Override
    public void sleep(){
        System.out.println("I am sleeping...");
    }
    @Override
    public void work(){
        System.out.println("I am working...");
    }
}
