package xyz.ytora.toolkit.time;

import org.junit.jupiter.api.Test;
import xyz.ytora.toolkit.time.cron.CronExpression;
import xyz.ytora.toolkit.time.cron.Crons;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CronsTest {

    @Test
    void shouldNormalizeExpression() {
        assertEquals("0 0/5 * * * ?", Crons.normalize("  0\t0/5   *  * *   ? "));
        assertEquals("", Crons.normalize("   "));
        assertEquals(null, Crons.normalize(null));
    }

    @Test
    void shouldSplitExpression() {
        assertEquals(Arrays.asList("0", "0/5", "*", "*", "*", "?"),
                Crons.split(" 0  0/5 * * * ? "));
        assertEquals(6, Crons.fieldCount(" 0  0/5 * * * ? "));
        assertThrows(IllegalArgumentException.class, () -> Crons.split(null));
        assertThrows(IllegalArgumentException.class, () -> Crons.split("   "));
    }

    @Test
    void shouldRecognizeStandardCron() {
        assertTrue(Crons.isStandard("*/5 8-18 * * 1-5"));
        assertTrue(Crons.isStandard("0 0 1 JAN MON"));
        assertFalse(Crons.isStandard("0 0 12 * * ?"));
        assertFalse(Crons.isStandard("60 0 * * *"));
        assertFalse(Crons.isStandard(null));
    }

    @Test
    void shouldRecognizeQuartzCron() {
        assertTrue(Crons.isQuartz("0 0/5 * * * ?"));
        assertTrue(Crons.isQuartz("0 15 10 ? * MON-FRI"));
        assertTrue(Crons.isQuartz("0 0 9 LW * ?"));
        assertTrue(Crons.isQuartz("0 0 10 ? * 2#3"));
        assertTrue(Crons.isQuartz("0 0 12 ? * MONL 2026"));

        assertFalse(Crons.isQuartz("*/5 8-18 * * 1-5"));
        assertFalse(Crons.isQuartz("0 0 12 * * *"));
        assertFalse(Crons.isQuartz("0 0 24 * * ?"));
        assertFalse(Crons.isQuartz("0 0 10 ? * 2#6"));
        assertFalse(Crons.isQuartz("0 0 12 ? * MONL 2100"));
    }

    @Test
    void shouldValidateCronExpression() {
        assertTrue(Crons.isValid("*/10 * * * *"));
        assertTrue(Crons.isValid("0 0 12 ? * WED"));
        assertFalse(Crons.isValid("bad cron"));
        assertFalse(Crons.isValid("0 0 12 ? * 9"));
        assertFalse(Crons.isValid(""));
    }

    @Test
    void shouldCalculateNextQuartzTime() {
        CronExpression cron = Crons.parse("0 0/15 9-10 ? * MON-FRI");
        LocalDateTime baseTime = LocalDateTime.of(2026, 4, 20, 9, 0, 0);

        assertEquals(LocalDateTime.of(2026, 4, 20, 9, 15, 0), cron.nextTimeAfter(baseTime));
        assertTrue(cron.matches(LocalDateTime.of(2026, 4, 20, 10, 45, 0)));
        assertFalse(cron.matches(LocalDateTime.of(2026, 4, 19, 10, 45, 0)));
    }

    @Test
    void shouldSupportQuartzDaySpecials() {
        assertEquals(LocalDateTime.of(2026, 2, 27, 9, 0, 0),
                Crons.parse("0 0 9 LW * ?").nextTimeAfter(LocalDateTime.of(2026, 2, 1, 0, 0, 0)));
        assertEquals(LocalDateTime.of(2026, 8, 3, 9, 0, 0),
                Crons.parse("0 0 9 1W * ?").nextTimeAfter(LocalDateTime.of(2026, 7, 15, 0, 0, 0)));
        assertEquals(LocalDateTime.of(2026, 4, 14, 10, 0, 0),
                Crons.parse("0 0 10 ? * 3#2").nextTimeAfter(LocalDateTime.of(2026, 4, 1, 0, 0, 0)));
        assertEquals(LocalDateTime.of(2026, 4, 24, 10, 0, 0),
                Crons.parse("0 0 10 ? * FRIL").nextTimeAfter(LocalDateTime.of(2026, 4, 1, 0, 0, 0)));
    }

    @Test
    void shouldCalculateNextTimes() {
        CronExpression cron = Crons.parse("0 0 12 ? * MON-FRI");
        ZonedDateTime baseTime = ZonedDateTime.of(
                LocalDateTime.of(2026, 4, 17, 12, 0, 0),
                ZoneId.systemDefault());

        List<ZonedDateTime> nextTimes = cron.nextTimesAfter(baseTime, 3);

        assertEquals(3, nextTimes.size());
        assertEquals(LocalDateTime.of(2026, 4, 20, 12, 0, 0), nextTimes.get(0).toLocalDateTime());
        assertEquals(LocalDateTime.of(2026, 4, 21, 12, 0, 0), nextTimes.get(1).toLocalDateTime());
        assertEquals(LocalDateTime.of(2026, 4, 22, 12, 0, 0), nextTimes.get(2).toLocalDateTime());
    }

    @Test
    void shouldReturnNullWhenNoNextTime() {
        CronExpression cron = Crons.parse("0 0 0 1 1 ? 2099");

        assertNull(cron.nextTimeAfter(LocalDateTime.of(2099, 1, 1, 0, 0, 0)));
    }
}
