package org.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Расчёт стоимости доставки")
class DeliveryCalculatorTest {

    private final DeliveryCalculator calculator = new DeliveryCalculator();

    // -------------------------------------------------------------------------
    // Расстояние
    // -------------------------------------------------------------------------

    @Nested
    @Tag("basic")
    @DisplayName("Слагаемое за расстояние")
    class DistanceCost {

        @Test
        @DisplayName("≤2 км → 50 руб.")
        void upTo2km() {
            // small + normal load, not fragile: 50 + 100 = 150 → min 400
            double cost = calculator.calculate(2.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName(">2 и ≤10 км → 100 руб.")
        void from2to10km() {
            // 100 (dist) + 100 (small) = 200 → min 400
            double cost = calculator.calculate(5.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName(">10 и ≤30 км → 200 руб.")
        void from10to30km() {
            // 200 (dist) + 100 (small) = 300 → min 400
            double cost = calculator.calculate(15.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName(">30 км → 300 руб.")
        void over30km() {
            // 300 (dist) + 100 (small) = 400 → ровно минимум
            double cost = calculator.calculate(31.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName("Граница: distance = 0 → слагаемое 50")
        void zeroDistance() {
            double cost = calculator.calculate(0.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);   // 50+100=150 < 400
        }

        @Test
        @DisplayName("Граница: distance = 30.0 → слагаемое 200 (не >30)")
        void exactly30km() {
            // 200 + 100 = 300 → min 400
            double cost = calculator.calculate(30.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName("Граница: distance = 30.1 → слагаемое 300")
        void justOver30km() {
            // 300 + 100 = 400 → ровно минимум
            double cost = calculator.calculate(30.1, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName("Граница: distance = 10.0 → слагаемое 100 (не >10)")
        void exactly10km() {
            // 100 + 100 = 200 → min 400
            double cost = calculator.calculate(10.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }
    }

    // -------------------------------------------------------------------------
    // Габариты
    // -------------------------------------------------------------------------

    @Nested
    @Tag("basic")
    @DisplayName("Слагаемое за габариты")
    class SizeCost {

        @Test
        @DisplayName("large → +200 руб.")
        void largeSizeAdds200() {
            // 300 (dist>30) + 200 (large) = 500
            double cost = calculator.calculate(31.0, "large", false, "normal");
            assertEquals(500.0, cost, 0.001);
        }

        @Test
        @DisplayName("small → +100 руб.")
        void smallSizeAdds100() {
            // 300 + 100 = 400
            double cost = calculator.calculate(31.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @ParameterizedTest(name = "size=''{0}'' → expected={1}")
        @CsvSource({
            "LARGE, 500.0",
            "Large, 500.0",
            "SMALL, 400.0",
            "Small, 400.0"
        })
        @DisplayName("Регистронезависимость габарита")
        void sizeIsCaseInsensitive(String size, double expected) {
            double cost = calculator.calculate(31.0, size, false, "normal");
            assertEquals(expected, cost, 0.001);
        }
    }

    // -------------------------------------------------------------------------
    // Хрупкость
    // -------------------------------------------------------------------------

    @Nested
    @Tag("basic")
    @DisplayName("Слагаемое за хрупкость")
    class FragilityCost {

        @Test
        @DisplayName("Хрупкий груз добавляет 300 руб.")
        void fragileAdds300() {
            // 200 (dist<=30) + 100 (small) + 300 (fragile) = 600
            double cost = calculator.calculate(15.0, "small", true, "normal");
            assertEquals(600.0, cost, 0.001);
        }

        @Test
        @DisplayName("Нехрупкий груз не добавляет ничего")
        void notFragileAddsZero() {
            // 200 + 100 = 300 → min 400
            double cost = calculator.calculate(15.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }
    }

    // -------------------------------------------------------------------------
    // Загруженность
    // -------------------------------------------------------------------------

    @Nested
    @Tag("basic")
    @DisplayName("Коэффициент загруженности")
    class LoadMultiplier {

        @ParameterizedTest(name = "load={0} → cost={1}")
        @CsvSource({
            "normal,   500.0",   // 300+200=500 → *1.0=500
            "elevated, 600.0",   // 500*1.2=600
            "high,     700.0",   // 500*1.4=700
            "very_high,800.0"    // 500*1.6=800
        })
        @DisplayName("Все уровни загруженности (dist=31, large, not fragile)")
        void allLoadLevels(String load, double expected) {
            double cost = calculator.calculate(31.0, "large", false, load);
            assertEquals(expected, cost, 0.001);
        }

        @ParameterizedTest(name = "load=''{0}'' → expected={1}")
        @CsvSource({
            "NORMAL,   500.0",
            "High,     700.0",
            "ELEVATED, 600.0",
            "very_HIGH,800.0"
        })
        @DisplayName("Регистронезависимость загруженности (dist=31, large)")
        void loadIsCaseInsensitive(String load, double expected) {
            double cost = calculator.calculate(31.0, "large", false, load);
            assertEquals(expected, cost, 0.001);
        }
    }

    // -------------------------------------------------------------------------
    // Минимальная сумма
    // -------------------------------------------------------------------------

    @Nested
    @Tag("edge")
    @DisplayName("Минимальная стоимость доставки")
    class MinimumCost {

        @Test
        @DisplayName("Сумма ниже 400 → возвращается 400")
        void belowMinimumReturns400() {
            // 50 + 100 = 150 → должно вернуть 400
            double cost = calculator.calculate(1.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName("Сумма ровно 400 → возвращается 400")
        void exactlyMinimumReturns400() {
            // 300 + 100 = 400
            double cost = calculator.calculate(31.0, "small", false, "normal");
            assertEquals(400.0, cost, 0.001);
        }

        @Test
        @DisplayName("Стоимость доставки всегда >= 400")
        void costIsAlwaysAtLeast400() {
            double cost = calculator.calculate(2.0, "small", false, "normal");
            assertTrue(cost >= 400.0, "Стоимость должна быть не менее 400 руб.");
        }

        @ParameterizedTest(name = "dist={0}, size={1}, fragile={2}, load={3} → min 400")
        @CsvSource({
            "0.0,  small, false, normal",
            "1.0,  small, false, normal",
            "2.0,  small, false, normal",
            "5.0,  small, false, normal",
            "10.0, small, false, normal",
        })
        @DisplayName("Комбинации с результатом < 400 → всегда 400")
        void combinationsYieldingMinimum(double dist, String size, boolean fragile, String load) {
            double cost = calculator.calculate(dist, size, fragile, load);
            assertTrue(cost >= 400.0);
        }
    }

    // -------------------------------------------------------------------------
    // Граничные значения (параметризовано)
    // -------------------------------------------------------------------------

    @Nested
    @Tag("edge")
    @DisplayName("Граничные значения расстояний")
    class BoundaryDistances {

        @ParameterizedTest(name = "dist={0} → expected={1}")
        @CsvSource({
            "0.0,  400.0",   // 50+100=150 → min 400
            "2.0,  400.0",   // 50+100=150 → min 400
            "2.1,  400.0",   // 100+100=200 → min 400
            "10.0, 400.0",   // 100+100=200 → min 400
            "10.1, 400.0",   // 200+100=300 → min 400
            "30.0, 400.0",   // 200+100=300 → min 400
            "30.1, 400.0",   // 300+100=400 → ровно min
            "50.0, 400.0"    // 300+100=400 → ровно min
        })
        @DisplayName("small + normal: результат всегда ≥400")
        void distanceBoundariesSmallNormal(double dist, double expected) {
            double cost = calculator.calculate(dist, "small", false, "normal");
            assertEquals(expected, cost, 0.001);
        }

        @ParameterizedTest(name = "dist={0} → expected={1}")
        @CsvSource({
            "0.0,  400.0",   // 50+200=250 → min 400
            "2.0,  400.0",   // 50+200=250 → min 400
            "10.0, 400.0",   // 100+200=300 → min 400
            "30.0, 400.0",   // 200+200=400 → ровно min
            "30.1, 500.0",   // 300+200=500
            "50.0, 500.0"    // 300+200=500
        })
        @DisplayName("large + normal: граничные значения")
        void distanceBoundariesLargeNormal(double dist, double expected) {
            double cost = calculator.calculate(dist, "large", false, "normal");
            assertEquals(expected, cost, 0.001);
        }
    }

    // -------------------------------------------------------------------------
    // Комплексные сценарии
    // -------------------------------------------------------------------------

    @Nested
    @Tag("basic")
    @DisplayName("Комплексные сценарии")
    class ComplexScenarios {

        @ParameterizedTest(name = "dist={0}, size={1}, fragile={2}, load={3} → {4}")
        @CsvSource({
            // 300+200+300=800, *1.6=1280
            "31.0, large, false, very_high, 800.0",
            // 200+200+300=700, *1.4=980
            "15.0, large, true,  high,      980.0",
            // 100+100+300=500, *1.2=600
            "5.0,  small, true,  elevated,  600.0",
            // 50+200+300=550, *1.0=550
            "1.0,  large, true,  normal,    550.0",
            // 300+200=500, *1.4=700
            "31.0, large, false, high,      700.0",
            // 200+100+300=600, *1.6=960
            "15.0, small, true,  very_high, 960.0"
        })
        @DisplayName("Полная комбинация параметров")
        void fullCombinations(double dist, String size, boolean fragile, String load, double expected) {
            double cost = calculator.calculate(dist, size, fragile, load);
            assertEquals(expected, cost, 0.001);
        }
    }

    // -------------------------------------------------------------------------
    // Обработка ошибок
    // -------------------------------------------------------------------------

    @Nested
    @Tag("error")
    @DisplayName("Обработка некорректных входных данных")
    class ErrorHandling {

        @Test
        @DisplayName("Хрупкий груз + расстояние >30 км → исключение")
        void fragileOver30kmThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(31.0, "small", true, "normal"));
        }

        @Test
        @DisplayName("Хрупкий груз + расстояние ровно 30 км → допустимо")
        void fragileExactly30kmAllowed() {
            assertDoesNotThrow(
                    () -> calculator.calculate(30.0, "small", true, "normal"));
        }

        @Test
        @DisplayName("Неизвестный габарит → исключение")
        void invalidSizeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(10.0, "medium", false, "normal"));
        }

        @Test
        @DisplayName("Пустой габарит → исключение")
        void emptySizeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(10.0, "", false, "normal"));
        }

        @Test
        @DisplayName("Неизвестная загруженность → исключение")
        void invalidLoadThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(10.0, "small", false, "overload"));
        }

        @Test
        @DisplayName("Пустая загруженность → исключение")
        void emptyLoadThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(10.0, "small", false, ""));
        }

        @Test
        @DisplayName("null в качестве size → исключение")
        void nullSizeThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(10.0, null, false, "normal"));
        }

        @Test
        @DisplayName("null в качестве load → исключение")
        void nullLoadThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(10.0, "small", false, null));
        }

        @ParameterizedTest(name = "dist={0} → исключение для хрупкого груза")
        @CsvSource({"30.1", "31.0", "100.0", "1000.0"})
        @Tag("error")
        @DisplayName("Хрупкий груз при дистанции >30 км — все случаи")
        void fragileOverDistanceVariants(double dist) {
            assertThrows(IllegalArgumentException.class,
                    () -> calculator.calculate(dist, "small", true, "normal"));
        }
    }
}
