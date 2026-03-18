package javaOop.exercises.person;

public class Manager extends Employee{
    public Manager(String name, double salary){
        super(name, salary);
    }

    public void manageTeam(){
        System.out.println("Manages team.");
    }

    @Override
    public void displayInfo(){
        System.out.println("Manager: " + getName() + ", Salary: £" + getSalary());
    }
}
