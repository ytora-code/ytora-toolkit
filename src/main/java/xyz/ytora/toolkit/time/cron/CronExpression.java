package xyz.ytora.toolkit.time.cron;

import java.time.*;
import java.util.*;

/**
 * 不可变 Cron 表达式。
 *
 * <p>Quartz Cron 字段顺序为：秒、分、时、日、月、周、年（可选）。
 * 支持 {@code * ? , - /}，月份和星期英文别名，以及 Quartz 常用的
 * {@code L W LW #} 语义。</p>
 *
 * @author ytora
 * @since 1.0.0
 */
public final class CronExpression {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2099;
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
    private static final Map<String, Integer> MONTH_NAMES = monthNames();
    private static final Map<String, Integer> WEEK_NAMES = weekNames();

    private final String expression;
    private final boolean quartz;
    private final NumberField seconds;
    private final NumberField minutes;
    private final NumberField hours;
    private final DayOfMonthField dayOfMonth;
    private final NumberField months;
    private final DayOfWeekField dayOfWeek;
    private final NumberField years;

    private CronExpression(String expression, boolean quartz, NumberField seconds, NumberField minutes,
                           NumberField hours, DayOfMonthField dayOfMonth, NumberField months,
                           DayOfWeekField dayOfWeek, NumberField years) {
        this.expression = expression;
        this.quartz = quartz;
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        this.dayOfMonth = dayOfMonth;
        this.months = months;
        this.dayOfWeek = dayOfWeek;
        this.years = years;
    }

    /**
     * 自动解析 Cron 表达式。
     *
     * <p>优先按 Quartz Cron 解析，失败后按 5 段 Unix Cron 解析。</p>
     *
     * @param cronExpr Cron 表达式
     * @return 表达式对象
     */
    public static CronExpression parse(String cronExpr) {
        try {
            return parseQuartz(cronExpr);
        } catch (CronTypeException ex) {
            // 转为五段标准CRON
            return parseStandard(cronExpr);
        }
    }

    /**
     * 解析 Quartz Cron 表达式。
     *
     * @param cronExpr Cron 表达式
     * @return 表达式对象
     */
    public static CronExpression parseQuartz(String cronExpr) {
        List<String> fields = Crons.split(cronExpr);
        if (fields.size() != 6 && fields.size() != 7) {
//            throw invalid("Quartz Cron 必须是 6 段或 7 段");
            throw new CronTypeException();
        }
        DayOfMonthField dom = DayOfMonthField.parse(fields.get(3), true);
        DayOfWeekField dow = DayOfWeekField.parse(fields.get(5), true);
        if (dom.unspecified == dow.unspecified) {
            throw invalid("Quartz Cron 的日字段和周字段必须且只能有一个使用 ?");
        }
        return new CronExpression(
                Crons.normalize(cronExpr),
                true,
                NumberField.parse(fields.get(0), 0, 59, null, false),
                NumberField.parse(fields.get(1), 0, 59, null, false),
                NumberField.parse(fields.get(2), 0, 23, null, false),
                dom,
                NumberField.parse(fields.get(4), 1, 12, MONTH_NAMES, false),
                dow,
                fields.size() == 7 ? NumberField.parse(fields.get(6), MIN_YEAR, MAX_YEAR, null, false)
                        : NumberField.all(MIN_YEAR, MAX_YEAR)
        );
    }

    /**
     * 解析 5 段 Unix Cron 表达式。
     *
     * @param cronExpr Cron 表达式
     * @return 表达式对象
     */
    public static CronExpression parseStandard(String cronExpr) {
        List<String> fields = Crons.split(cronExpr);
        if (fields.size() != 5) {
            throw invalid("标准 Cron 必须是 5 段");
        }
        return new CronExpression(
                Crons.normalize(cronExpr),
                false,
                NumberField.single(0, 0, 59),
                NumberField.parse(fields.get(0), 0, 59, null, false),
                NumberField.parse(fields.get(1), 0, 23, null, false),
                DayOfMonthField.parse(fields.get(2), false),
                NumberField.parse(fields.get(3), 1, 12, MONTH_NAMES, false),
                DayOfWeekField.parse(fields.get(4), false),
                NumberField.all(MIN_YEAR, MAX_YEAR)
        );
    }

    /**
     * 返回规范化后的表达式。
     *
     * @return 表达式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * 判断是否为 Quartz Cron。
     *
     * @return 是否为 Quartz Cron
     */
    public boolean isQuartz() {
        return quartz;
    }

    /**
     * 判断指定时间是否匹配当前表达式。
     *
     * @param time 待判断时间
     * @return 是否匹配
     */
    public boolean matches(ZonedDateTime time) {
        if (time == null) {
            return false;
        }
        return years.contains(time.getYear())
                && months.contains(time.getMonthValue())
                && dayMatches(time.toLocalDate())
                && hours.contains(time.getHour())
                && minutes.contains(time.getMinute())
                && seconds.contains(time.getSecond());
    }

    /**
     * 判断指定本地时间是否匹配当前表达式。
     *
     * @param time 待判断时间
     * @return 是否匹配
     */
    public boolean matches(LocalDateTime time) {
        return time != null && matches(time.atZone(SYSTEM_ZONE));
    }

    /**
     * 计算指定时间之后的下一次执行时间。
     *
     * @param baseTime 基准时间，不包含该时间本身
     * @return 下一次执行时间；若不存在则返回 {@code null}
     */
    public ZonedDateTime nextTimeAfter(ZonedDateTime baseTime) {
        if (baseTime == null) {
            throw new NullPointerException("不能为空baseTime");
        }
        ZonedDateTime cursor = baseTime.plusSeconds(1).withNano(0);
        LocalDate startDate = cursor.toLocalDate();
        LocalDate endDate = LocalDate.of(MAX_YEAR, 12, 31);
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (!years.contains(date.getYear()) || !months.contains(date.getMonthValue()) || !dayMatches(date)) {
                continue;
            }
            ZonedDateTime candidate = firstTimeOnDate(date, cursor, baseTime.getZone());
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 计算指定本地时间之后的下一次执行时间。
     *
     * @param baseTime 基准时间，不包含该时间本身
     * @return 下一次执行时间；若不存在则返回 {@code null}
     */
    public LocalDateTime nextTimeAfter(LocalDateTime baseTime) {
        if (baseTime == null) {
            throw new NullPointerException("不能为空baseTime");
        }
        ZonedDateTime next = nextTimeAfter(baseTime.atZone(SYSTEM_ZONE));
        return next == null ? null : next.toLocalDateTime();
    }

    /**
     * 连续计算多次执行时间。
     *
     * @param baseTime 基准时间
     * @param count 数量
     * @return 执行时间列表
     */
    public List<ZonedDateTime> nextTimesAfter(ZonedDateTime baseTime, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("数量不能小于 0");
        }
        List<ZonedDateTime> result = new ArrayList<ZonedDateTime>(count);
        ZonedDateTime cursor = baseTime;
        for (int i = 0; i < count; i++) {
            ZonedDateTime next = nextTimeAfter(cursor);
            if (next == null) {
                break;
            }
            result.add(next);
            cursor = next;
        }
        return result;
    }

    private ZonedDateTime firstTimeOnDate(LocalDate date, ZonedDateTime cursor, ZoneId zone) {
        boolean sameDate = date.equals(cursor.toLocalDate());
        for (Integer hour : hours.values) {
            if (sameDate && hour < cursor.getHour()) {
                continue;
            }
            for (Integer minute : minutes.values) {
                if (sameDate && hour == cursor.getHour() && minute < cursor.getMinute()) {
                    continue;
                }
                for (Integer second : seconds.values) {
                    if (sameDate && hour == cursor.getHour() && minute == cursor.getMinute()
                            && second < cursor.getSecond()) {
                        continue;
                    }
                    return ZonedDateTime.of(date, LocalTime.of(hour, minute, second), zone);
                }
            }
        }
        return null;
    }

    private boolean dayMatches(LocalDate date) {
        boolean domMatch = dayOfMonth.matches(date);
        boolean dowMatch = dayOfWeek.matches(date);
        if (quartz) {
            return dayOfMonth.unspecified ? dowMatch : domMatch;
        }
        if (dayOfMonth.any && dayOfWeek.any) {
            return true;
        }
        if (dayOfMonth.any) {
            return dowMatch;
        }
        if (dayOfWeek.any) {
            return domMatch;
        }
        return domMatch || dowMatch;
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }

    private static Map<String, Integer> monthNames() {
        Map<String, Integer> names = new HashMap<String, Integer>();
        for (Month month : Month.values()) {
            names.put(month.name().substring(0, 3), month.getValue());
        }
        return names;
    }

    private static Map<String, Integer> weekNames() {
        Map<String, Integer> names = new HashMap<String, Integer>();
        names.put("SUN", 1);
        names.put("MON", 2);
        names.put("TUE", 3);
        names.put("WED", 4);
        names.put("THU", 5);
        names.put("FRI", 6);
        names.put("SAT", 7);
        return names;
    }

    private static int parseNumber(String value, int min, int max, Map<String, Integer> names) {
        String text = value.toUpperCase(Locale.ROOT);
        Integer named = names == null ? null : names.get(text);
        int number;
        if (named != null) {
            number = named;
        } else {
            try {
                number = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                throw invalid("Cron 字段值非法: " + value);
            }
        }
        if (number < min || number > max) {
            throw invalid("Cron 字段值超出范围: " + value);
        }
        return number;
    }

    private static final class NumberField {

        private final int min;
        private final int max;
        private final List<Integer> values;
        private final boolean any;

        private NumberField(int min, int max, List<Integer> values, boolean any) {
            this.min = min;
            this.max = max;
            this.values = values;
            this.any = any;
        }

        static NumberField all(int min, int max) {
            TreeSet<Integer> values = new TreeSet<Integer>();
            for (int i = min; i <= max; i++) {
                values.add(i);
            }
            return new NumberField(min, max, immutable(values), true);
        }

        static NumberField single(int value, int min, int max) {
            TreeSet<Integer> values = new TreeSet<Integer>();
            if (value < min || value > max) {
                throw invalid("Cron 字段值超出范围: " + value);
            }
            values.add(value);
            return new NumberField(min, max, immutable(values), false);
        }

        static NumberField parse(String field, int min, int max, Map<String, Integer> names, boolean allowQuestion) {
            if (field == null || field.trim().isEmpty()) {
                throw invalid("Cron 字段不能为空");
            }
            String text = field.trim().toUpperCase(Locale.ROOT);
            if ("?".equals(text)) {
                if (!allowQuestion) {
                    throw invalid("当前 Cron 字段不支持 ?");
                }
                return all(min, max);
            }
            if ("*".equals(text)) {
                return all(min, max);
            }
            TreeSet<Integer> values = new TreeSet<Integer>();
            String[] parts = text.split(",");
            for (String part : parts) {
                addPart(values, part, min, max, names);
            }
            if (values.isEmpty()) {
                throw invalid("Cron 字段不能为空");
            }
            return new NumberField(min, max, immutable(values), false);
        }

        boolean contains(int value) {
            return value >= min && value <= max && values.contains(value);
        }

        private static void addPart(TreeSet<Integer> values, String part, int min, int max, Map<String, Integer> names) {
            if (part == null || part.isEmpty()) {
                throw invalid("Cron 字段包含空片段");
            }
            String[] stepParts = part.split("/");
            if (stepParts.length > 2) {
                throw invalid("Cron 步长格式非法: " + part);
            }
            int step = stepParts.length == 2 ? parseNumber(stepParts[1], 1, max - min + 1, null) : 1;
            String range = stepParts[0];
            int start;
            int end;
            if ("*".equals(range) || "?".equals(range)) {
                start = min;
                end = max;
            } else if (range.indexOf('-') >= 0) {
                String[] bounds = range.split("-");
                if (bounds.length != 2) {
                    throw invalid("Cron 范围格式非法: " + part);
                }
                start = parseNumber(bounds[0], min, max, names);
                end = parseNumber(bounds[1], min, max, names);
                if (start > end) {
                    throw invalid("Cron 范围起始值不能大于结束值: " + part);
                }
            } else {
                start = parseNumber(range, min, max, names);
                end = stepParts.length == 2 ? max : start;
            }
            for (int i = start; i <= end; i += step) {
                values.add(i);
            }
        }
    }

    private static final class DayOfMonthField {

        private final NumberField numbers;
        private final boolean any;
        private final boolean unspecified;
        private final boolean lastDay;
        private final boolean lastWeekday;
        private final Integer nearestWeekday;

        private DayOfMonthField(NumberField numbers, boolean any, boolean unspecified,
                                boolean lastDay, boolean lastWeekday, Integer nearestWeekday) {
            this.numbers = numbers;
            this.any = any;
            this.unspecified = unspecified;
            this.lastDay = lastDay;
            this.lastWeekday = lastWeekday;
            this.nearestWeekday = nearestWeekday;
        }

        static DayOfMonthField parse(String field, boolean quartz) {
            String text = field == null ? "" : field.trim().toUpperCase(Locale.ROOT);
            if (text.isEmpty()) {
                throw invalid("日字段不能为空");
            }
            if ("?".equals(text)) {
                if (!quartz) {
                    throw invalid("标准 Cron 日字段不支持 ?");
                }
                return new DayOfMonthField(NumberField.all(1, 31), false, true, false, false, null);
            }
            if ("*".equals(text)) {
                return new DayOfMonthField(NumberField.all(1, 31), true, false, false, false, null);
            }
            if ("L".equals(text)) {
                return new DayOfMonthField(null, false, false, true, false, null);
            }
            if ("LW".equals(text)) {
                return new DayOfMonthField(null, false, false, false, true, null);
            }
            if (text.endsWith("W")) {
                int day = parseNumber(text.substring(0, text.length() - 1), 1, 31, null);
                return new DayOfMonthField(null, false, false, false, false, day);
            }
            return new DayOfMonthField(NumberField.parse(text, 1, 31, null, false), false, false, false, false, null);
        }

        boolean matches(LocalDate date) {
            if (unspecified || any) {
                return true;
            }
            int day = date.getDayOfMonth();
            if (lastDay) {
                return day == date.lengthOfMonth();
            }
            if (lastWeekday) {
                return date.equals(lastWeekday(date));
            }
            if (nearestWeekday != null) {
                return date.equals(nearestWeekday(date.getYear(), date.getMonthValue(), nearestWeekday));
            }
            return numbers.contains(day);
        }

        private static LocalDate lastWeekday(LocalDate date) {
            LocalDate last = YearMonth.from(date).atEndOfMonth();
            DayOfWeek week = last.getDayOfWeek();
            if (week == DayOfWeek.SATURDAY) {
                return last.minusDays(1);
            }
            if (week == DayOfWeek.SUNDAY) {
                return last.minusDays(2);
            }
            return last;
        }

        private static LocalDate nearestWeekday(int year, int month, int requestedDay) {
            YearMonth yearMonth = YearMonth.of(year, month);
            int day = Math.min(requestedDay, yearMonth.lengthOfMonth());
            LocalDate date = yearMonth.atDay(day);
            DayOfWeek week = date.getDayOfWeek();
            if (week == DayOfWeek.SATURDAY) {
                return day == 1 ? date.plusDays(2) : date.minusDays(1);
            }
            if (week == DayOfWeek.SUNDAY) {
                return day == yearMonth.lengthOfMonth() ? date.minusDays(2) : date.plusDays(1);
            }
            return date;
        }
    }

    private static final class DayOfWeekField {

        private final NumberField numbers;
        private final boolean any;
        private final boolean unspecified;
        private final Integer lastWeekday;
        private final Integer nthWeekday;
        private final Integer nthIndex;

        private DayOfWeekField(NumberField numbers, boolean any, boolean unspecified,
                               Integer lastWeekday, Integer nthWeekday, Integer nthIndex) {
            this.numbers = numbers;
            this.any = any;
            this.unspecified = unspecified;
            this.lastWeekday = lastWeekday;
            this.nthWeekday = nthWeekday;
            this.nthIndex = nthIndex;
        }

        static DayOfWeekField parse(String field, boolean quartz) {
            String text = field == null ? "" : field.trim().toUpperCase(Locale.ROOT);
            if (text.isEmpty()) {
                throw invalid("周字段不能为空");
            }
            if ("?".equals(text)) {
                if (!quartz) {
                    throw invalid("标准 Cron 周字段不支持 ?");
                }
                return new DayOfWeekField(NumberField.all(1, 7), false, true, null, null, null);
            }
            if ("*".equals(text)) {
                return new DayOfWeekField(NumberField.all(1, 7), true, false, null, null, null);
            }
            if (text.endsWith("L") && text.length() > 1) {
                int weekday = parseWeekday(text.substring(0, text.length() - 1));
                return new DayOfWeekField(null, false, false, weekday, null, null);
            }
            int sharp = text.indexOf('#');
            if (sharp > 0) {
                int weekday = parseWeekday(text.substring(0, sharp));
                int index = parseNumber(text.substring(sharp + 1), 1, 5, null);
                return new DayOfWeekField(null, false, false, null, weekday, index);
            }
            return new DayOfWeekField(NumberField.parse(text, 1, 7, WEEK_NAMES, false), false, false, null, null, null);
        }

        boolean matches(LocalDate date) {
            if (unspecified || any) {
                return true;
            }
            int weekday = quartzWeekday(date);
            if (lastWeekday != null) {
                return weekday == lastWeekday && date.plusWeeks(1).getMonthValue() != date.getMonthValue();
            }
            if (nthWeekday != null) {
                int index = (date.getDayOfMonth() - 1) / 7 + 1;
                return weekday == nthWeekday && index == nthIndex;
            }
            return numbers.contains(weekday);
        }

        private static int parseWeekday(String value) {
            return parseNumber(value, 1, 7, WEEK_NAMES);
        }

        private static int quartzWeekday(LocalDate date) {
            int javaValue = date.getDayOfWeek().getValue();
            return javaValue == 7 ? 1 : javaValue + 1;
        }
    }

    private static List<Integer> immutable(TreeSet<Integer> values) {
        return Collections.unmodifiableList(new ArrayList<Integer>(values));
    }
}
