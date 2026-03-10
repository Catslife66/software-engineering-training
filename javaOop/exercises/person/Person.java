package javaOop.exercises.person;

public class Person {
    private String name;
    private int age;

    public Person(String name, int age){
        this.name = name;
        setAge(age);
    }

    public String getName(){
        return name;
    }
    public int getAge(){
        return age;
    }
    public void setAge(int age){
        if(age < 0){
            System.out.println("age cannot be negative.");
            return;
        }
        if(age > 150){
            System.out.println("age cannot be greater than 150.");
            return;
        }
        this.age = age;
    }

}
