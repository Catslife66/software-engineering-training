package javaOop.exercises.student;

public class Student {
    private final String name;
    private final String course;
    private double grade;

    public Student(String name, String course){
        this(name, course, 0.0);
    }

    public Student(String name, String course, double grade){
        this.name = name;
        this.course = course;
        this.grade = grade;
    }

    public void displayStudentInfo(){
        System.out.println("Name|Course|Grade");
        System.out.println(name + "|" + course + "|" + grade);
    }
}
