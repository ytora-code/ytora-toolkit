package xyz.ytora.toolkit.convert;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConvertsTest {

    @Test
    void shouldConvertRegisteredTypes() {
        assertEquals(Integer.valueOf(18), Converts.convert("18", Integer.class));
        assertEquals(Long.valueOf(18L), Converts.convert("18", Long.class));
        assertEquals(new BigInteger("12345678901234567890"), Converts.convert("12345678901234567890", BigInteger.class));
        assertEquals("18", Converts.convert(Integer.valueOf(18), String.class));
    }

    @Test
    void shouldConvertPrimitiveTargetTypes() {
        assertEquals(Integer.valueOf(18), Converts.convert("18", int.class));
        assertEquals(Long.valueOf(18L), Converts.convert(Integer.valueOf(18), long.class));
    }

    @Test
    void shouldConvertDates() {
        Date date = Converts.convert("2026-04-21", Date.class);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(2026, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.APRIL, calendar.get(Calendar.MONTH));
        assertEquals(21, calendar.get(Calendar.DAY_OF_MONTH));

        assertEquals(LocalDate.of(2026, 4, 21), Converts.convert("2026-04-21", LocalDate.class));
        assertEquals(
                LocalDateTime.of(2026, 4, 21, 10, 30, 15),
                Converts.convert("2026-04-21T10:30:15", LocalDateTime.class)
        );
    }

    @Test
    void shouldReturnDefaultValueWhenConversionFailed() {
        assertEquals(Integer.valueOf(9), Converts.convert("abc", Integer.class, 9));
        assertEquals(Integer.valueOf(9), Converts.convert(null, Integer.class, 9));
    }

    @Test
    void shouldRejectUnsupportedConversion() {
        assertThrows(ConverterException.class, () -> Converts.convert(new Object(), Integer.class));
        assertThrows(IllegalArgumentException.class, () -> Converts.convert("1", null));
    }

    @Test
    void shouldReturnNullForBlankValuesWhenConverterSupportsIt() {
        assertNull(Converts.convert("", Integer.class));
        assertNull(Converts.convert("", Long.class));
    }
}
