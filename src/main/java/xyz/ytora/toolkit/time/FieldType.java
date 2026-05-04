package xyz.ytora.toolkit.time;

/**
 * CRON 每一段的枚举
 *
 * <p>说明</p>
 *
 * @author ytora 
 * @since 1.0
 */
public enum FieldType {
    SECOND(0, 59),
    MINUTE(0, 59),
    HOUR(0, 23),
    DAY_OF_MONTH(1, 31),
    MONTH(1, 12),
    DAY_OF_WEEK(1, 7),
    YEAR(1970, 2099);

    private final int min;
    private final int max;

    FieldType(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

}
