package xyz.ytora.toolkit.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusinessDatesTest {

    @Test
    void shouldIdentifyWeekendAndBusinessDay() {
        LocalDate friday = LocalDate.of(2026, 4, 10);
        LocalDate saturday = LocalDate.of(2026, 4, 11);
        LocalDate sunday = LocalDate.of(2026, 4, 12);
        Date monday = Dates.toDate(LocalDate.of(2026, 4, 13));

        assertFalse(BusinessDates.isWeekend(friday));
        assertTrue(BusinessDates.isWeekend(saturday));
        assertTrue(BusinessDates.isWeekend(sunday));
        assertFalse(BusinessDates.isWeekend(monday));

        assertTrue(BusinessDates.isBusinessDay(friday));
        assertFalse(BusinessDates.isBusinessDay(saturday));
        assertTrue(BusinessDates.isBusinessDay((Object) LocalDateTime.of(2026, 4, 13, 10, 30, 15)));

        assertFalse(BusinessDates.isWeekend((LocalDate) null));
        assertFalse(BusinessDates.isBusinessDay((LocalDate) null));
    }

    @Test
    void shouldGetNextBusinessDay() {
        LocalDate friday = LocalDate.of(2026, 4, 10);
        LocalDate saturday = LocalDate.of(2026, 4, 11);
        Date sunday = Dates.toDate(LocalDate.of(2026, 4, 12));

        assertEquals(LocalDate.of(2026, 4, 13), BusinessDates.nextBusinessDay(friday));
        assertEquals(LocalDate.of(2026, 4, 13), BusinessDates.nextBusinessDay(saturday));
        assertEquals(LocalDate.of(2026, 4, 13), Dates.toLocalDate(BusinessDates.nextBusinessDay(sunday)));
        assertEquals(LocalDate.of(2026, 4, 13),
                BusinessDates.nextBusinessDay((Object) LocalDateTime.of(2026, 4, 10, 10, 30, 15)));
        assertNull(BusinessDates.nextBusinessDay((LocalDate) null));
    }

    @Test
    void shouldGetPreviousBusinessDay() {
        LocalDate monday = LocalDate.of(2026, 4, 13);
        LocalDate sunday = LocalDate.of(2026, 4, 12);
        Date saturday = Dates.toDate(LocalDate.of(2026, 4, 11));

        assertEquals(LocalDate.of(2026, 4, 10), BusinessDates.previousBusinessDay(monday));
        assertEquals(LocalDate.of(2026, 4, 10), BusinessDates.previousBusinessDay(sunday));
        assertEquals(LocalDate.of(2026, 4, 10), Dates.toLocalDate(BusinessDates.previousBusinessDay(saturday)));
        assertEquals(LocalDate.of(2026, 4, 10),
                BusinessDates.previousBusinessDay((Object) LocalDateTime.of(2026, 4, 13, 10, 30, 15)));
        assertNull(BusinessDates.previousBusinessDay((LocalDate) null));
    }

    @Test
    void shouldPlusAndMinusBusinessDays() {
        LocalDate friday = LocalDate.of(2026, 4, 10);
        LocalDate monday = LocalDate.of(2026, 4, 13);
        Date fridayDate = Dates.toDate(friday);

        assertEquals(LocalDate.of(2026, 4, 13), BusinessDates.plusBusinessDays(friday, 1));
        assertEquals(LocalDate.of(2026, 4, 15), BusinessDates.plusBusinessDays(friday, 3));
        assertEquals(LocalDate.of(2026, 4, 10), BusinessDates.plusBusinessDays(monday, -1));
        assertEquals(friday, BusinessDates.plusBusinessDays(friday, 0));

        assertEquals(LocalDate.of(2026, 4, 8), BusinessDates.minusBusinessDays(friday, 2));
        assertEquals(LocalDate.of(2026, 4, 15), Dates.toLocalDate(BusinessDates.plusBusinessDays(fridayDate, 3)));
        assertEquals(LocalDate.of(2026, 4, 8), Dates.toLocalDate(BusinessDates.minusBusinessDays(fridayDate, 2)));

        assertNull(BusinessDates.plusBusinessDays((LocalDate) null, 1));
        assertNull(BusinessDates.minusBusinessDays((LocalDate) null, 1));
    }

    @Test
    void shouldRejectUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> BusinessDates.isWeekend((Object) "2026-04-12"));
        assertThrows(IllegalArgumentException.class, () -> BusinessDates.nextBusinessDay((Object) "2026-04-12"));
        assertThrows(IllegalArgumentException.class, () -> BusinessDates.previousBusinessDay((Object) "2026-04-12"));
    }
}
