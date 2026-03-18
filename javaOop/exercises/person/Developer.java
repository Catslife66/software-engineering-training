package javaOop.exercises.person;

public class Developer extends Employee{
    public Developer(String name, double salary){
        super(name, salary);
    }
    public void writeCode(){
        System.out.println("Writes code.");
    }
    @Override
    public void displayInfo(){
        System.out.println("Developer: " + getName() + ", Salary: £" + getSalary());
    }

}
