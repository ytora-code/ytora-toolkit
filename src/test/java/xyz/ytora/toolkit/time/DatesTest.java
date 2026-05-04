package xyz.ytora.toolkit.time;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatesTest {

    @Test
    void shouldGetCurrentDateTime() {
        assertNotNull(Dates.today());
        assertNotNull(Dates.now());
        assertTrue(Dates.currentTimeMillis() > 0);
    }

    @Test
    void shouldFormatLocalDateAndLocalDateTime() {
        LocalDate date = LocalDate.of(2026, 4, 12);
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 12, 10, 30, 15);

        assertEquals("2026-04-12", Dates.format(date));
        assertEquals("2026/04/12", Dates.format(date, "yyyy/MM/dd"));
        assertEquals("2026-04-12 10:30:15", Dates.format(dateTime));
        assertEquals("2026/04/12 10:30", Dates.format(dateTime, "yyyy/MM/dd HH:mm"));
        assertNull(Dates.format((LocalDate) null));
        assertNull(Dates.format((LocalDateTime) null));
    }

    @Test
    void shouldFormatDate() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 12, 10, 30, 15);
        Date date = Dates.toDate(dateTime);

        assertEquals("2026-04-12 10:30:15", Dates.format(date));
        assertEquals("2026-04-12", Dates.format(date, "yyyy-MM-dd"));
        assertEquals("2026-04-12 10:30:15", Dates.format((Object) date));
        assertEquals("2026-04-12", Dates.format((Object) LocalDate.of(2026, 4, 12), "yyyy-MM-dd"));
        assertNull(Dates.format((Date) null));
        assertNull(Dates.format((Object) null));
    }

    @Test
    void shouldParseDateAndDateTime() {
        assertEquals(LocalDate.of(2026, 4, 12), Dates.parseDate("2026-04-12"));
        assertEquals(LocalDate.of(2026, 4, 12), Dates.parseDate("2026/04/12", "yyyy/MM/dd"));
        assertEquals(LocalDateTime.of(2026, 4, 12, 10, 30, 15),
                Dates.parseDateTime("2026-04-12 10:30:15"));
        assertEquals(LocalDateTime.of(2026, 4, 12, 10, 30),
                Dates.parseDateTime("2026/04/12 10:30", "yyyy/MM/dd HH:mm"));
        assertEquals(LocalDate.of(2026, 4, 12), Dates.toLocalDate(Dates.parseDateAsDate("2026-04-12")));
        assertEquals(LocalDateTime.of(2026, 4, 12, 10, 30, 15),
                Dates.toLocalDateTime(Dates.parseDateTimeAsDate("2026-04-12 10:30:15")));
        assertNull(Dates.parseDate(null));
        assertNull(Dates.parseDateTime(null));
        assertNull(Dates.parseDateAsDate(null));
        assertNull(Dates.parseDateTimeAsDate(null));
    }

    @Test
    void shouldRejectInvalidPatternOrText() {
        assertThrows(IllegalArgumentException.class, () -> Dates.format(LocalDate.now(), ""));
        assertThrows(IllegalArgumentException.class, () -> Dates.parseDate("2026/04/12"));
        assertThrows(IllegalArgumentException.class, () -> Dates.parseDateTime("2026-04-12"));
    }

    @Test
    void shouldHandleDayBoundaries() {
        LocalDate date = LocalDate.of(2026, 4, 12);
        Date utilDate = Dates.toDate(LocalDateTime.of(2026, 4, 12, 10, 30, 15));

        assertEquals(LocalDateTime.of(2026, 4, 12, 0, 0, 0), Dates.startOfDay(date));
        assertEquals(LocalTime.MAX, Dates.endOfDay(date).toLocalTime());
        assertEquals(LocalDateTime.of(2026, 4, 12, 0, 0, 0), Dates.toLocalDateTime(Dates.startOfDay(utilDate)));
        assertEquals(LocalTime.of(23, 59, 59, 999000000),
                Dates.toLocalDateTime(Dates.endOfDay(utilDate)).toLocalTime());
        assertNull(Dates.startOfDay((LocalDate) null));
        assertNull(Dates.endOfDay((LocalDate) null));
    }

    @Test
    void shouldPlusDateAndDateTime() {
        LocalDate date = LocalDate.of(2026, 4, 12);
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 12, 10, 30, 15);
        Date utilDate = Dates.toDate(dateTime);

        assertEquals(LocalDate.of(2026, 4, 15), Dates.plusDays(date, 3));
        assertEquals(LocalDate.of(2026, 6, 12), Dates.plusMonths(date, 2));
        assertEquals(LocalDate.of(2028, 4, 12), Dates.plusYears(date, 2));

        assertEquals(LocalDateTime.of(2026, 4, 15, 10, 30, 15), Dates.plusDays(dateTime, 3));
        assertEquals(LocalDateTime.of(2026, 6, 12, 10, 30, 15), Dates.plusMonths(dateTime, 2));
        assertEquals(LocalDateTime.of(2028, 4, 12, 10, 30, 15), Dates.plusYears(dateTime, 2));

        assertEquals(LocalDateTime.of(2026, 4, 15, 10, 30, 15), Dates.toLocalDateTime(Dates.plusDays(utilDate, 3)));
        assertEquals(LocalDateTime.of(2026, 6, 12, 10, 30, 15), Dates.toLocalDateTime(Dates.plusMonths(utilDate, 2)));
        assertEquals(LocalDateTime.of(2028, 4, 12, 10, 30, 15), Dates.toLocalDateTime(Dates.plusYears(utilDate, 2)));
    }

    @Test
    void shouldCompareDates() {
        LocalDate date1 = LocalDate.of(2026, 4, 12);
        LocalDate date2 = LocalDate.of(2026, 4, 13);
        LocalDateTime time1 = LocalDateTime.of(2026, 4, 12, 10, 30, 15);
        LocalDateTime time2 = LocalDateTime.of(2026, 4, 12, 10, 30, 16);
        Date utilDate = Dates.toDate(time1);

        assertTrue(Dates.isBefore(date1, date2));
        assertFalse(Dates.isBefore(date2, date1));
        assertTrue(Dates.isAfter(date2, date1));
        assertFalse(Dates.isAfter(date1, date2));

        assertTrue(Dates.isBefore(time1, time2));
        assertTrue(Dates.isAfter(time2, time1));

        assertTrue(Dates.isBetween(LocalDate.of(2026, 4, 12), date1, date2));
        assertTrue(Dates.isBetween(LocalDate.of(2026, 4, 13), date1, date2));
        assertFalse(Dates.isBetween(LocalDate.of(2026, 4, 14), date1, date2));

        assertTrue(Dates.isBetween(LocalDateTime.of(2026, 4, 12, 10, 30, 15), time1, time2));
        assertTrue(Dates.isBetween(LocalDateTime.of(2026, 4, 12, 10, 30, 16), time1, time2));
        assertFalse(Dates.isBetween(LocalDateTime.of(2026, 4, 12, 10, 30, 17), time1, time2));

        assertTrue(Dates.isBefore((Object) date1, time1));
        assertTrue(Dates.isBefore((Object) time1, date2));
        assertFalse(Dates.isAfter((Object) date1, utilDate));
        assertTrue(Dates.isBetween((Object) utilDate, date1, time2));
        assertFalse(Dates.isBetween((Object) Dates.plusDays(utilDate, 2), date1, time2));
    }

    @Test
    void shouldConvertBetweenDateTypes() {
        LocalDate date = LocalDate.of(2026, 4, 12);
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 12, 10, 30, 15);

        Date utilDateFromDate = Dates.toDate(date);
        Date utilDateFromDateTime = Dates.toDate(dateTime);

        assertEquals(date, Dates.toLocalDate(utilDateFromDate));
        assertEquals(dateTime, Dates.toLocalDateTime(utilDateFromDateTime));
        assertEquals(date, Dates.toLocalDate((Object) dateTime));
        assertEquals(date.atStartOfDay(), Dates.toLocalDateTime((Object) date));
        assertEquals(utilDateFromDateTime, Dates.toDate((Object) dateTime));
        assertNull(Dates.toDate((LocalDate) null));
        assertNull(Dates.toDate((LocalDateTime) null));
        assertNull(Dates.toLocalDate(null));
        assertNull(Dates.toLocalDateTime(null));
        assertNull(Dates.toDate((Object) null));
    }

    @Test
    void shouldConvertEpochMillis() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 4, 12, 10, 30, 15);
        Date utilDate = Dates.toDate(dateTime);

        Long epochMilli = Dates.toEpochMilli(dateTime);

        assertNotNull(epochMilli);
        assertEquals(dateTime, Dates.fromEpochMilli(epochMilli));
        assertEquals(epochMilli, Dates.toEpochMilli((Object) utilDate));
        assertNull(Dates.toEpochMilli(null));
        assertNull(Dates.fromEpochMilli(null));
    }

    @Test
    void shouldRejectUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> Dates.format((Object) "2026-04-12"));
        assertThrows(IllegalArgumentException.class, () -> Dates.toLocalDate((Object) "2026-04-12"));
        assertThrows(IllegalArgumentException.class, () -> Dates.isBefore((Object) "2026-04-12", LocalDate.now()));
    }
}
