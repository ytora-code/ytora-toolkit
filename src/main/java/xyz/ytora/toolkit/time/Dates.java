package xyz.ytora.toolkit.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 日期时间工具类。
 *
 * <p>提供日期/时间格式化、解析、间隔计算、起止时间处理，以及
 * {@link Date}、{@link LocalDate}、{@link LocalDateTime} 之间的相互转换。</p>
 * <p>本类仅包含静态方法，不支持实例化。</p>
 *
 * @author ytora
 * @since 1.0.0
 */
public final class Dates {

    /**
     * 默认日期格式：{@code yyyy-MM-dd}
     */
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 默认日期时间格式：{@code yyyy-MM-dd HH:mm:ss}
     */
    public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private Dates() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 获取当前本地日期。
     *
     * <p>示例：</p>
     * <p>{@code today() -> 2026-04-12}</p>
     *
     * @return 当前本地日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 获取当前本地日期时间。
     *
     * <p>示例：</p>
     * <p>{@code now() -> 2026-04-12T10:30:15}</p>
     *
     * @return 当前本地日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前时间戳毫秒值。
     *
     * @return 当前时间戳毫秒值
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 使用默认格式格式化本地日期。
     *
     * <p>示例：</p>
     * <p>{@code format(LocalDate.of(2026, 4, 12)) -> "2026-04-12"}</p>
     *
     * @param value 待格式化日期
     * @return 格式化后的字符串；若参数为 {@code null} 则返回 {@code null}
     */
    public static String format(LocalDate value) {
        return format(value, DEFAULT_DATE_PATTERN);
    }

    /**
     * 使用指定格式格式化本地日期。
     *
     * <p>若格式为空，将抛出 {@link IllegalArgumentException}。</p>
     *
     * @param value 待格式化日期
     * @param pattern 格式模板
     * @return 格式化后的字符串；若日期为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当格式模板为空时抛出
     */
    public static String format(LocalDate value, String pattern) {
        if (value == null) {
            return null;
        }
        return value.format(formatter(pattern));
    }

    /**
     * 使用默认格式格式化本地日期时间。
     *
     * <p>示例：</p>
     * <p>{@code format(LocalDateTime.of(2026, 4, 12, 10, 30, 15)) -> "2026-04-12 10:30:15"}</p>
     *
     * @param value 待格式化日期时间
     * @return 格式化后的字符串；若参数为 {@code null} 则返回 {@code null}
     */
    public static String format(LocalDateTime value) {
        return format(value, DEFAULT_DATE_TIME_PATTERN);
    }

    /**
     * 使用指定格式格式化本地日期时间。
     *
     * <p>若格式为空，将抛出 {@link IllegalArgumentException}。</p>
     *
     * @param value 待格式化日期时间
     * @param pattern 格式模板
     * @return 格式化后的字符串；若日期时间为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当格式模板为空时抛出
     */
    public static String format(LocalDateTime value, String pattern) {
        if (value == null) {
            return null;
        }
        return value.format(formatter(pattern));
    }

    /**
     * 使用默认格式格式化 {@link Date}。
     *
     * @param value 待格式化日期
     * @return 格式化后的字符串；若参数为 {@code null} 则返回 {@code null}
     */
    public static String format(Date value) {
        return format(value, DEFAULT_DATE_TIME_PATTERN);
    }

    /**
     * 使用指定格式格式化 {@link Date}。
     *
     * @param value 待格式化日期
     * @param pattern 格式模板
     * @return 格式化后的字符串；若日期为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当格式模板为空时抛出
     */
    public static String format(Date value, String pattern) {
        if (value == null) {
            return null;
        }
        return format(toLocalDateTime(value), pattern);
    }

    /**
     * 使用默认格式格式化日期对象。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     *
     * @param value 待格式化对象
     * @return 格式化后的字符串
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static String format(Object value) {
        return format(value, DEFAULT_DATE_TIME_PATTERN);
    }

    /**
     * 使用指定格式格式化日期对象。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 待格式化对象
     * @param pattern 格式模板
     * @return 格式化后的字符串；若参数为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当对象类型不受支持或格式模板为空时抛出
     */
    public static String format(Object value, String pattern) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return format((LocalDate) value, pattern);
        }
        if (value instanceof LocalDateTime) {
            return format((LocalDateTime) value, pattern);
        }
        if (value instanceof Date) {
            return format((Date) value, pattern);
        }
        throw unsupportedType(value);
    }

    /**
     * 使用默认格式解析本地日期。
     *
     * <p>示例：</p>
     * <p>{@code parseDate("2026-04-12") -> 2026-04-12}</p>
     *
     * @param value 待解析字符串
     * @return 解析后的本地日期；若参数为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当文本无法按格式解析时抛出
     */
    public static LocalDate parseDate(String value) {
        return parseDate(value, DEFAULT_DATE_PATTERN);
    }

    /**
     * 使用指定格式解析本地日期。
     *
     * @param value 待解析字符串
     * @param pattern 格式模板
     * @return 解析后的本地日期；若文本为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当格式模板为空或文本无法按格式解析时抛出
     */
    public static LocalDate parseDate(String value, String pattern) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value, formatter(pattern));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("日期文本解析失败: " + value, ex);
        }
    }

    /**
     * 使用默认格式解析本地日期时间。
     *
     * <p>示例：</p>
     * <p>{@code parseDateTime("2026-04-12 10:30:15") -> 2026-04-12T10:30:15}</p>
     *
     * @param value 待解析字符串
     * @return 解析后的本地日期时间；若参数为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当文本无法按格式解析时抛出
     */
    public static LocalDateTime parseDateTime(String value) {
        return parseDateTime(value, DEFAULT_DATE_TIME_PATTERN);
    }

    /**
     * 使用指定格式解析本地日期时间。
     *
     * @param value 待解析字符串
     * @param pattern 格式模板
     * @return 解析后的本地日期时间；若文本为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当格式模板为空或文本无法按格式解析时抛出
     */
    public static LocalDateTime parseDateTime(String value, String pattern) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, formatter(pattern));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("日期时间文本解析失败: " + value, ex);
        }
    }

    /**
     * 使用默认日期格式解析并转换为 {@link Date}。
     *
     * <p>默认取当天开始时刻。</p>
     *
     * @param value 待解析字符串
     * @return 解析后的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date parseDateAsDate(String value) {
        LocalDate date = parseDate(value);
        return date == null ? null : toDate(date);
    }

    /**
     * 使用指定日期格式解析并转换为 {@link Date}。
     *
     * <p>默认取当天开始时刻。</p>
     *
     * @param value 待解析字符串
     * @param pattern 格式模板
     * @return 解析后的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date parseDateAsDate(String value, String pattern) {
        LocalDate date = parseDate(value, pattern);
        return date == null ? null : toDate(date);
    }

    /**
     * 使用默认日期时间格式解析并转换为 {@link Date}。
     *
     * @param value 待解析字符串
     * @return 解析后的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date parseDateTimeAsDate(String value) {
        LocalDateTime dateTime = parseDateTime(value);
        return dateTime == null ? null : toDate(dateTime);
    }

    /**
     * 使用指定日期时间格式解析并转换为 {@link Date}。
     *
     * @param value 待解析字符串
     * @param pattern 格式模板
     * @return 解析后的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date parseDateTimeAsDate(String value, String pattern) {
        LocalDateTime dateTime = parseDateTime(value, pattern);
        return dateTime == null ? null : toDate(dateTime);
    }

    /**
     * 将 {@link LocalDate} 转换为一天开始时刻的 {@link LocalDateTime}。
     *
     * @param value 待转换日期
     * @return 当天开始时间；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDateTime startOfDay(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }

    /**
     * 将 {@link LocalDate} 转换为一天结束时刻的 {@link LocalDateTime}。
     *
     * <p>结束时刻定义为 {@code 23:59:59.999999999}。</p>
     *
     * @param value 待转换日期
     * @return 当天结束时间；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDateTime endOfDay(LocalDate value) {
        return value == null ? null : value.atTime(LocalTime.MAX);
    }

    /**
     * 将 {@link Date} 调整为当天开始时刻。
     *
     * @param value 待处理日期
     * @return 当天开始时间对应的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date startOfDay(Date value) {
        return value == null ? null : toDate(startOfDay(toLocalDate(value)));
    }

    /**
     * 将 {@link Date} 调整为当天结束时刻。
     *
     * @param value 待处理日期
     * @return 当天结束时间对应的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date endOfDay(Date value) {
        return value == null ? null : toDate(endOfDay(toLocalDate(value)));
    }

    /**
     * 对本地日期增加指定天数。
     *
     * @param value 原始日期
     * @param days 增加天数，可为负数
     * @return 计算后的日期；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDate plusDays(LocalDate value, long days) {
        return value == null ? null : value.plusDays(days);
    }

    /**
     * 对本地日期时间增加指定天数。
     *
     * @param value 原始日期时间
     * @param days 增加天数，可为负数
     * @return 计算后的日期时间；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDateTime plusDays(LocalDateTime value, long days) {
        return value == null ? null : value.plusDays(days);
    }

    /**
     * 对 {@link Date} 增加指定天数。
     *
     * @param value 原始日期
     * @param days 增加天数，可为负数
     * @return 计算后的日期；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date plusDays(Date value, long days) {
        return value == null ? null : toDate(toLocalDateTime(value).plusDays(days));
    }

    /**
     * 对本地日期增加指定月数。
     *
     * @param value 原始日期
     * @param months 增加月数，可为负数
     * @return 计算后的日期；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDate plusMonths(LocalDate value, long months) {
        return value == null ? null : value.plusMonths(months);
    }

    /**
     * 对本地日期时间增加指定月数。
     *
     * @param value 原始日期时间
     * @param months 增加月数，可为负数
     * @return 计算后的日期时间；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDateTime plusMonths(LocalDateTime value, long months) {
        return value == null ? null : value.plusMonths(months);
    }

    /**
     * 对 {@link Date} 增加指定月数。
     *
     * @param value 原始日期
     * @param months 增加月数，可为负数
     * @return 计算后的日期；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date plusMonths(Date value, long months) {
        return value == null ? null : toDate(toLocalDateTime(value).plusMonths(months));
    }

    /**
     * 对本地日期增加指定年数。
     *
     * @param value 原始日期
     * @param years 增加年数，可为负数
     * @return 计算后的日期；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDate plusYears(LocalDate value, long years) {
        return value == null ? null : value.plusYears(years);
    }

    /**
     * 对本地日期时间增加指定年数。
     *
     * @param value 原始日期时间
     * @param years 增加年数，可为负数
     * @return 计算后的日期时间；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDateTime plusYears(LocalDateTime value, long years) {
        return value == null ? null : value.plusYears(years);
    }

    /**
     * 对 {@link Date} 增加指定年数。
     *
     * @param value 原始日期
     * @param years 增加年数，可为负数
     * @return 计算后的日期；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date plusYears(Date value, long years) {
        return value == null ? null : toDate(toLocalDateTime(value).plusYears(years));
    }

    /**
     * 判断前一个日期是否早于后一个日期。
     *
     * @param left 左侧日期
     * @param right 右侧日期
     * @return 当两个参数均不为 {@code null} 且左侧早于右侧时返回 {@code true}
     */
    public static boolean isBefore(LocalDate left, LocalDate right) {
        return left != null && right != null && left.isBefore(right);
    }

    /**
     * 判断前一个日期时间是否早于后一个日期时间。
     *
     * @param left 左侧日期时间
     * @param right 右侧日期时间
     * @return 当两个参数均不为 {@code null} 且左侧早于右侧时返回 {@code true}
     */
    public static boolean isBefore(LocalDateTime left, LocalDateTime right) {
        return left != null && right != null && left.isBefore(right);
    }

    /**
     * 判断前一个日期是否晚于后一个日期。
     *
     * @param left 左侧日期
     * @param right 右侧日期
     * @return 当两个参数均不为 {@code null} 且左侧晚于右侧时返回 {@code true}
     */
    public static boolean isAfter(LocalDate left, LocalDate right) {
        return left != null && right != null && left.isAfter(right);
    }

    /**
     * 判断前一个日期时间是否晚于后一个日期时间。
     *
     * @param left 左侧日期时间
     * @param right 右侧日期时间
     * @return 当两个参数均不为 {@code null} 且左侧晚于右侧时返回 {@code true}
     */
    public static boolean isAfter(LocalDateTime left, LocalDateTime right) {
        return left != null && right != null && left.isAfter(right);
    }

    /**
     * 判断日期是否位于起止区间内，包含边界。
     *
     * @param value 待判断日期
     * @param start 起始日期
     * @param end 结束日期
     * @return 当日期位于区间内时返回 {@code true}
     */
    public static boolean isBetween(LocalDate value, LocalDate start, LocalDate end) {
        if (value == null || start == null || end == null) {
            return false;
        }
        return !value.isBefore(start) && !value.isAfter(end);
    }

    /**
     * 判断日期时间是否位于起止区间内，包含边界。
     *
     * @param value 待判断日期时间
     * @param start 起始日期时间
     * @param end 结束日期时间
     * @return 当日期时间位于区间内时返回 {@code true}
     */
    public static boolean isBetween(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        if (value == null || start == null || end == null) {
            return false;
        }
        return !value.isBefore(start) && !value.isAfter(end);
    }

    /**
     * 判断左侧日期对象是否早于右侧日期对象。
     *
     * <p>支持混合传入 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param left 左侧日期对象
     * @param right 右侧日期对象
     * @return 当两个参数均不为 {@code null} 且左侧早于右侧时返回 {@code true}
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static boolean isBefore(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        return toComparableDateTime(left).isBefore(toComparableDateTime(right));
    }

    /**
     * 判断左侧日期对象是否晚于右侧日期对象。
     *
     * <p>支持混合传入 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param left 左侧日期对象
     * @param right 右侧日期对象
     * @return 当两个参数均不为 {@code null} 且左侧晚于右侧时返回 {@code true}
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static boolean isAfter(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        return toComparableDateTime(left).isAfter(toComparableDateTime(right));
    }

    /**
     * 判断日期对象是否位于起止区间内，包含边界。
     *
     * <p>支持混合传入 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 待判断对象
     * @param start 起始对象
     * @param end 结束对象
     * @return 当对象位于区间内时返回 {@code true}
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static boolean isBetween(Object value, Object start, Object end) {
        if (value == null || start == null || end == null) {
            return false;
        }
        LocalDateTime actualValue = toComparableDateTime(value);
        LocalDateTime actualStart = toComparableDateTime(start);
        LocalDateTime actualEnd = toComparableDateTime(end);
        return !actualValue.isBefore(actualStart) && !actualValue.isAfter(actualEnd);
    }

    /**
     * 将 {@link Date} 转换为 {@link LocalDate}。
     *
     * @param value 待转换日期
     * @return 转换后的本地日期；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDate toLocalDate(Date value) {
        return value == null ? null : Instant.ofEpochMilli(value.getTime())
                .atZone(SYSTEM_ZONE)
                .toLocalDate();
    }

    /**
     * 将 {@link Date} 转换为 {@link LocalDateTime}。
     *
     * @param value 待转换日期
     * @return 转换后的本地日期时间；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDateTime toLocalDateTime(Date value) {
        return value == null ? null : Instant.ofEpochMilli(value.getTime())
                .atZone(SYSTEM_ZONE)
                .toLocalDateTime();
    }

    /**
     * 将日期对象转换为 {@link LocalDate}。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 待转换对象
     * @return 转换后的本地日期；若参数为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        if (value instanceof Date) {
            return toLocalDate((Date) value);
        }
        throw unsupportedType(value);
    }

    /**
     * 将日期对象转换为 {@link LocalDateTime}。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     * <p>{@link LocalDate} 默认转换为当天开始时刻。</p>
     *
     * @param value 待转换对象
     * @return 转换后的本地日期时间；若参数为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        }
        if (value instanceof Date) {
            return toLocalDateTime((Date) value);
        }
        throw unsupportedType(value);
    }

    /**
     * 将 {@link LocalDate} 转换为 {@link Date}。
     *
     * <p>默认取当天开始时刻。</p>
     *
     * @param value 待转换本地日期
     * @return 转换后的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date toDate(LocalDate value) {
        return value == null ? null : toDate(value.atStartOfDay());
    }

    /**
     * 将 {@link LocalDateTime} 转换为 {@link Date}。
     *
     * @param value 待转换本地日期时间
     * @return 转换后的日期对象；若参数为 {@code null} 则返回 {@code null}
     */
    public static Date toDate(LocalDateTime value) {
        return value == null ? null : Date.from(value.atZone(SYSTEM_ZONE).toInstant());
    }

    /**
     * 将日期对象转换为 {@link Date}。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 待转换对象
     * @return 转换后的日期对象；若参数为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static Date toDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof LocalDate) {
            return toDate((LocalDate) value);
        }
        if (value instanceof LocalDateTime) {
            return toDate((LocalDateTime) value);
        }
        throw unsupportedType(value);
    }

    /**
     * 将本地日期时间转换为时间戳毫秒值。
     *
     * @param value 待转换本地日期时间
     * @return 转换后的毫秒时间戳；若参数为 {@code null} 则返回 {@code null}
     */
    public static Long toEpochMilli(LocalDateTime value) {
        return value == null ? null : value.atZone(SYSTEM_ZONE).toInstant().toEpochMilli();
    }

    /**
     * 将日期对象转换为时间戳毫秒值。
     *
     * <p>支持 {@link Date}、{@link LocalDate}、{@link LocalDateTime}。</p>
     *
     * @param value 待转换对象
     * @return 转换后的毫秒时间戳；若参数为 {@code null} 则返回 {@code null}
     * @throws IllegalArgumentException 当对象类型不受支持时抛出
     */
    public static Long toEpochMilli(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        return toEpochMilli(toLocalDateTime(value));
    }

    /**
     * 将时间戳毫秒值转换为本地日期时间。
     *
     * @param value 毫秒时间戳
     * @return 转换后的本地日期时间；若参数为 {@code null} 则返回 {@code null}
     */
    public static LocalDateTime fromEpochMilli(Long value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(value), SYSTEM_ZONE);
    }

    /**
     * 创建日期格式化器。
     *
     * @param pattern 格式模板
     * @return 日期格式化器
     * @throws IllegalArgumentException 当格式模板为空时抛出
     */
    private static DateTimeFormatter formatter(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("日期格式不能为空");
        }
        return DateTimeFormatter.ofPattern(pattern);
    }

    /**
     * 将日期对象转换为可比较的本地日期时间。
     *
     * @param value 待转换对象
     * @return 可比较的本地日期时间
     */
    private static LocalDateTime toComparableDateTime(Object value) {
        return toLocalDateTime(value);
    }

    /**
     * 构造不支持的类型异常。
     *
     * @param value 实际参数
     * @return 非法参数异常
     */
    private static IllegalArgumentException unsupportedType(Object value) {
        return new IllegalArgumentException("不支持的日期类型: " + value.getClass().getName());
    }
}
