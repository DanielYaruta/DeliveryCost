package org.example;

public class DeliveryCalculator {

    private static final double MIN_COST = 400.0;

    public static double calculate(double distance, String size, boolean fragile, String load) {
        if (fragile && distance > 30) {
            throw new IllegalArgumentException(
                    "Хрупкие грузы нельзя доставлять на расстояние более 30 км");
        }

        double cost = distanceCost(distance) + sizeCost(size) + fragilityCost(fragile);
        cost *= loadMultiplier(load);

        return Math.max(cost, MIN_COST);
    }

    private static double distanceCost(double distance) {
        if (distance > 30) return 300;
        if (distance > 10) return 200;
        if (distance > 2)  return 100;
        return 50;
    }

    private static double sizeCost(String size) {
        return switch (size.toLowerCase()) {
            case "large" -> 200;
            case "small" -> 100;
            default -> throw new IllegalArgumentException("Неизвестный габарит: " + size);
        };
    }

    private static double fragilityCost(boolean fragile) {
        return fragile ? 300 : 0;
    }

    private static double loadMultiplier(String load) {
        return switch (load.toLowerCase()) {
            case "very_high" -> 1.6;
            case "high"      -> 1.4;
            case "elevated"  -> 1.2;
            case "normal"    -> 1.0;
            default -> throw new IllegalArgumentException("Неизвестная загруженность: " + load);
        };
    }
}
