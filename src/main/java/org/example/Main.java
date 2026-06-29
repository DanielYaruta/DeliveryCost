package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        DeliveryCalculator calculator = new DeliveryCalculator();

        printCost(calculator, 15.0,  "small", false, "normal");
        printCost(calculator, 31.0,  "large", false, "elevated");
        printCost(calculator, 20.0,  "small", true,  "high");
        printCost(calculator,  1.0,  "small", false, "normal");

        System.out.println();
        try {
            calculator.calculate(50.0, "small", true, "normal");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void printCost(DeliveryCalculator calc,
                                   double distance, String size,
                                   boolean fragile, String load) {
        double cost = calc.calculate(distance, size, fragile, load);
        System.out.printf("distance=%.1f, size=%s, fragile=%b, load=%s -> cost=%.2f руб.%n",
                distance, size, fragile, load, cost);
    }
}
