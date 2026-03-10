package javaOop.exercises.temperatureConverter;

public class Main {
    public static void main(String[] args){
        TemperatureConverter temp = new TemperatureConverter();

        System.out.println("The temperature is " + temp.celsiusToFahrenheit(5) + "F");
        System.out.println("The temperature is " + temp.fahrenheitToCelsius(10) + "C");

        System.out.println(MathHelper.square(2));
        System.out.println(MathHelper.cube(3));
        System.out.println(MathHelper.max(5, 8));
    }
}
