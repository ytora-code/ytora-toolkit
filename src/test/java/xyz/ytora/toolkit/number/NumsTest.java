package xyz.ytora.toolkit.number;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumsTest {

    @Test
    void shouldAddSubtractAndMultiply() {
        assertEquals(new BigDecimal("3.3"), Nums.add(1.1D, 2.2D));
        assertEquals(new BigDecimal("3.30"), Nums.add(1.1D, 2.2D, 2));

        assertEquals(new BigDecimal("7.7"), Nums.subtract(10, 2.3D));
        assertEquals(new BigDecimal("7.70"), Nums.subtract(10, 2.3D, 2));

        assertEquals(new BigDecimal("6.0"), Nums.multiply(2, 3.0D));
        assertEquals(new BigDecimal("6.000"), Nums.multiply(2, 3.0D, 3));
    }

    @Test
    void shouldDivideWithAndWithoutScale() {
        assertEquals(new BigDecimal("2.5"), Nums.divide(5, 2));
        assertEquals(new BigDecimal("3.33"), Nums.divide(10, 3, 2));
        assertEquals(new BigDecimal("3.333"), Nums.divide(10, 3, 3, RoundingMode.DOWN));

        assertThrows(IllegalArgumentException.class, () -> Nums.divide(10, 3));
        assertThrows(IllegalArgumentException.class, () -> Nums.divide(10, 0));
    }

    @Test
    void shouldRemainderAndMod() {
        assertEquals(new BigDecimal("1"), Nums.remainder(10, 3));
        assertEquals(new BigDecimal("-1"), Nums.remainder(-10, 3));
        assertEquals(new BigDecimal("1.00"), Nums.remainder(10, 3, 2));

        assertEquals(new BigDecimal("2"), Nums.mod(-10, 3));
        assertEquals(new BigDecimal("1"), Nums.mod(10, 3));
        assertEquals(new BigDecimal("2.000"), Nums.mod(-10, 3, 3));

        assertThrows(IllegalArgumentException.class, () -> Nums.remainder(10, 0));
        assertThrows(IllegalArgumentException.class, () -> Nums.mod(10, 0));
    }

    @Test
    void shouldCalculatePercent() {
        assertEquals(new BigDecimal("25.00"), Nums.percent(1, 4));
        assertEquals(new BigDecimal("33.33"), Nums.percent(1, 3, 2));
        assertEquals(new BigDecimal("33.333"), Nums.percent(1, 3, 3, RoundingMode.DOWN));
    }

    @Test
    void shouldRejectInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> Nums.add(null, 1));
        assertThrows(IllegalArgumentException.class, () -> Nums.subtract(1, null));
        assertThrows(IllegalArgumentException.class, () -> Nums.multiply(1, null));
        assertThrows(IllegalArgumentException.class, () -> Nums.add(1, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> Nums.divide(1, 2, -1));
        assertThrows(IllegalArgumentException.class, () -> Nums.divide(1, 2, 2, null));
        assertThrows(IllegalArgumentException.class, () -> Nums.percent(1, 2, 2, null));
    }
}
