package javaOop.exercises.person;

public class Main {
    public static void main(String[] args){
        Manager m = new Manager("Collin", 50000);
        Developer d = new Developer("Lily", 35000);

        m.displayInfo();
        m.manageTeam();

        d.displayInfo();
        d.writeCode();
    }

}
