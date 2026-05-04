package xyz.ytora.toolkit.time.cron;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Cron 表达式工具类。
 *
 * <p>默认按 Quartz Cron 解析，支持 6 段和 7 段表达式；同时提供 5 段
 * Unix Cron 的格式识别能力。该实现不依赖第三方库。</p>
 *
 * @author ytora
 * @since 1.0.0
 */
public final class Crons {

    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private Crons() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 规范化 Cron 表达式中的空白字符。
     *
     * @param cronExpr Cron 表达式
     * @return 规范化后的表达式；若参数为 {@code null} 则返回 {@code null}
     */
    public static String normalize(String cronExpr) {
        if (cronExpr == null) {
            return null;
        }
        return cronExpr.trim().replaceAll("\\s+", " ");
    }

    /**
     * 拆分 Cron 表达式字段。
     *
     * @param cronExpr Cron 表达式
     * @return 字段列表
     * @throws IllegalArgumentException 当表达式为空时抛出
     */
    public static List<String> split(String cronExpr) {
        String normalized = normalize(cronExpr);
        if (normalized == null || normalized.isEmpty()) {
            throw new IllegalArgumentException("Cron 表达式不能为空");
        }
        return Collections.unmodifiableList(Arrays.asList(normalized.split(" ")));
    }

    /**
     * 获取 Cron 表达式字段数量。
     *
     * @param cronExpr Cron 表达式
     * @return 字段数量
     */
    public static int fieldCount(String cronExpr) {
        return split(cronExpr).size();
    }

    /**
     * 判断是否为合法的 5 段 Unix Cron 表达式。
     *
     * @param cronExpr Cron 表达式
     * @return 是否合法
     */
    public static boolean isStandard(String cronExpr) {
        try {
            CronExpression.parseStandard(cronExpr);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * 判断是否为合法的 Quartz Cron 表达式。
     *
     * @param cronExpr Cron 表达式
     * @return 是否合法
     */
    public static boolean isQuartz(String cronExpr) {
        try {
            CronExpression.parseQuartz(cronExpr);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * 判断是否为合法 Cron 表达式。
     *
     * <p>先按 Quartz 解析，失败后再按 5 段 Unix Cron 解析。</p>
     *
     * @param cronExpr Cron 表达式
     * @return 是否合法
     */
    public static boolean isValid(String cronExpr) {
        return isQuartz(cronExpr) || isStandard(cronExpr);
    }

    /**
     * 解析 Cron 表达式。
     *
     * @param cronExpr Cron 表达式
     * @return Cron 表达式对象
     */
    public static CronExpression parse(String cronExpr) {
        return CronExpression.parse(cronExpr);
    }

    /**
     * 根据 Cron 表达式计算相对于基准时间的下一次执行时间。
     *
     * @param cronExpr Cron 表达式
     * @param baseTimeMillis 基准时间戳，毫秒
     * @return 下一次执行时间戳，毫秒；若不存在则返回 {@code -1}
     */
    public static Long nextTimeByCron(String cronExpr, long baseTimeMillis) {
        return nextTime(cronExpr, baseTimeMillis);
    }

    /**
     * 根据 Cron 表达式计算相对于当前时间的下一次执行时间。
     *
     * @param cronExpr Cron 表达式
     * @return 下一次执行时间戳，毫秒；若不存在则返回 {@code -1}
     */
    public static Long nextTimeByCron(String cronExpr) {
        return nextTime(cronExpr, System.currentTimeMillis());
    }

    /**
     * 根据 Cron 表达式计算相对于基准时间的下一次执行时间。
     *
     * @param cronExpr Cron 表达式
     * @param baseTimeMillis 基准时间戳，毫秒
     * @return 下一次执行时间戳，毫秒；若不存在则返回 {@code -1}
     */
    public static Long nextTime(String cronExpr, long baseTimeMillis) {
        ZonedDateTime baseTime = Instant.ofEpochMilli(baseTimeMillis).atZone(SYSTEM_ZONE);
        ZonedDateTime nextTime = parse(cronExpr).nextTimeAfter(baseTime);
        return nextTime == null ? -1L : nextTime.toInstant().toEpochMilli();
    }
}
