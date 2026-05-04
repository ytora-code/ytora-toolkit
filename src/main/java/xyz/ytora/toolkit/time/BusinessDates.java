package xyz.ytora.toolkit.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 工作日日期工具类。
 *
 * <p>提供基于自然周的基础工作日计算能力。
 * 默认将周六、周日视为非工作日。</p>
 *
 * @author ytora
 * @since 1.0.0
 */
public final class BusinessDates {

    private BusinessDates() {
        throw new AssertionError("工具类不允许实例化");
    }

    /**
     * 判断是否为周末。
     *
     * @param value 日期
     * @return 是周六或周日返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isWeekend(LocalDate value) {
        if (value == null) {
            return false;
        }
        DayOfWeek dayOfWeek = value.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 判断 {@link Date} 是否为周末。
     *
     * @param value 日期
     * @return 是周六或周日返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isWeekend(Date value) {
        return value != null && isWeekend(Dates.toLocalDate(value));
    }

    /**
     * 判断给定对象是否为周末。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 日期对象
     * @return 是周六或周日返回 {@code true}，否则返回 {@code false}
     * @throws IllegalArgumentException 参数类型不受支持时抛出
     */
    public static boolean isWeekend(Object value) {
        return value != null && isWeekend(toLocalDate(value));
    }

    /**
     * 判断是否为工作日。
     *
     * @param value 日期
     * @return 非周末返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isBusinessDay(LocalDate value) {
        return value != null && !isWeekend(value);
    }

    /**
     * 判断 {@link Date} 是否为工作日。
     *
     * @param value 日期
     * @return 非周末返回 {@code true}，否则返回 {@code false}
     */
    public static boolean isBusinessDay(Date value) {
        return value != null && isBusinessDay(Dates.toLocalDate(value));
    }

    /**
     * 判断给定对象是否为工作日。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 日期对象
     * @return 非周末返回 {@code true}，否则返回 {@code false}
     * @throws IllegalArgumentException 参数类型不受支持时抛出
     */
    public static boolean isBusinessDay(Object value) {
        return value != null && isBusinessDay(toLocalDate(value));
    }

    /**
     * 获取指定日期之后的下一个工作日。
     *
     * @param value 日期
     * @return 下一个工作日；输入为 {@code null} 时返回 {@code null}
     */
    public static LocalDate nextBusinessDay(LocalDate value) {
        if (value == null) {
            return null;
        }
        LocalDate actual = value.plusDays(1);
        while (isWeekend(actual)) {
            actual = actual.plusDays(1);
        }
        return actual;
    }

    /**
     * 获取指定 {@link Date} 之后的下一个工作日。
     *
     * @param value 日期
     * @return 下一个工作日；输入为 {@code null} 时返回 {@code null}
     */
    public static Date nextBusinessDay(Date value) {
        return value == null ? null : Dates.toDate(nextBusinessDay(Dates.toLocalDate(value)));
    }

    /**
     * 获取指定对象之后的下一个工作日。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 日期对象
     * @return 下一个工作日；输入为 {@code null} 时返回 {@code null}
     * @throws IllegalArgumentException 参数类型不受支持时抛出
     */
    public static LocalDate nextBusinessDay(Object value) {
        return value == null ? null : nextBusinessDay(toLocalDate(value));
    }

    /**
     * 获取指定日期之前的上一个工作日。
     *
     * @param value 日期
     * @return 上一个工作日；输入为 {@code null} 时返回 {@code null}
     */
    public static LocalDate previousBusinessDay(LocalDate value) {
        if (value == null) {
            return null;
        }
        LocalDate actual = value.minusDays(1);
        while (isWeekend(actual)) {
            actual = actual.minusDays(1);
        }
        return actual;
    }

    /**
     * 获取指定 {@link Date} 之前的上一个工作日。
     *
     * @param value 日期
     * @return 上一个工作日；输入为 {@code null} 时返回 {@code null}
     */
    public static Date previousBusinessDay(Date value) {
        return value == null ? null : Dates.toDate(previousBusinessDay(Dates.toLocalDate(value)));
    }

    /**
     * 获取指定对象之前的上一个工作日。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 日期对象
     * @return 上一个工作日；输入为 {@code null} 时返回 {@code null}
     * @throws IllegalArgumentException 参数类型不受支持时抛出
     */
    public static LocalDate previousBusinessDay(Object value) {
        return value == null ? null : previousBusinessDay(toLocalDate(value));
    }

    /**
     * 按工作日天数增减日期。
     *
     * <p>正数向后推移，负数向前推移，零则返回原日期。</p>
     *
     * @param value 日期
     * @param days 工作日天数
     * @return 计算后的日期；输入为 {@code null} 时返回 {@code null}
     */
    public static LocalDate plusBusinessDays(LocalDate value, long days) {
        if (value == null) {
            return null;
        }
        if (days == 0) {
            return value;
        }
        LocalDate actual = value;
        long remaining = Math.abs(days);
        while (remaining > 0) {
            actual = days > 0 ? actual.plusDays(1) : actual.minusDays(1);
            if (isBusinessDay(actual)) {
                remaining--;
            }
        }
        return actual;
    }

    /**
     * 按工作日天数增减 {@link Date}。
     *
     * @param value 日期
     * @param days 工作日天数
     * @return 计算后的日期；输入为 {@code null} 时返回 {@code null}
     */
    public static Date plusBusinessDays(Date value, long days) {
        return value == null ? null : Dates.toDate(plusBusinessDays(Dates.toLocalDate(value), days));
    }

    /**
     * 按工作日天数向前推移日期。
     *
     * @param value 日期
     * @param days 工作日天数
     * @return 计算后的日期；输入为 {@code null} 时返回 {@code null}
     */
    public static LocalDate minusBusinessDays(LocalDate value, long days) {
        return plusBusinessDays(value, -days);
    }

    /**
     * 按工作日天数向前推移 {@link Date}。
     *
     * @param value 日期
     * @param days 工作日天数
     * @return 计算后的日期；输入为 {@code null} 时返回 {@code null}
     */
    public static Date minusBusinessDays(Date value, long days) {
        return plusBusinessDays(value, -days);
    }

    /**
     * 将支持的日期对象转换为 {@link LocalDate}。
     *
     * @param value 日期对象
     * @return 转换后的日期
     * @throws IllegalArgumentException 参数类型不受支持时抛出
     */
    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        if (value instanceof Date) {
            return Dates.toLocalDate((Date) value);
        }
        throw unsupportedType(value);
    }

    /**
     * 构造不支持类型异常。
     *
     * @param value 参数值
     * @return 参数异常
     */
    private static IllegalArgumentException unsupportedType(Object value) {
        return new IllegalArgumentException("不支持的日期类型: " + value.getClass().getName());
    }
}
