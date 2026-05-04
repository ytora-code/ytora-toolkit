package xyz.ytora.toolkit.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 数字工具类。
 *
 * <p>提供常用计算方法。</p>
 *
 * @author ytora
 * @since 1.0-SNAPSHOT
 */
public final class Nums {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100L);

    private Nums() {
        throw new AssertionError("工具类不允许实例化");
    }

    /**
     * 判断目标类型是否属于数字类型
     * @param clazz 目标类型
     * @return 是否为数字
     */
    public static Boolean isNum(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (clazz.isPrimitive()) {
            return true;
        }
        return clazz.equals(Integer.class)
                || clazz.equals(Long.class)
                || clazz.equals(Short.class)
                || clazz.equals(Byte.class)
                || clazz.equals(Float.class)
                || clazz.equals(Double.class)
                || clazz.equals(BigDecimal.class)
                || clazz.equals(BigInteger.class);
    }

    /**
     * 加法。
     *
     * @param left 左值
     * @param right 右值
     * @return 计算结果
     */
    public static BigDecimal add(Number left, Number right) {
        return toBigDecimal(left).add(toBigDecimal(right));
    }

    /**
     * 加法，并按指定精度返回结果。
     *
     * @param left 左值
     * @param right 右值
     * @param scale 小数位数
     * @return 计算结果
     */
    public static BigDecimal add(Number left, Number right, int scale) {
        return scale(add(left, right), scale);
    }

    /**
     * 减法。
     *
     * @param left 左值
     * @param right 右值
     * @return 计算结果
     */
    public static BigDecimal subtract(Number left, Number right) {
        return toBigDecimal(left).subtract(toBigDecimal(right));
    }

    /**
     * 减法，并按指定精度返回结果。
     *
     * @param left 左值
     * @param right 右值
     * @param scale 小数位数
     * @return 计算结果
     */
    public static BigDecimal subtract(Number left, Number right, int scale) {
        return scale(subtract(left, right), scale);
    }

    /**
     * 乘法。
     *
     * @param left 左值
     * @param right 右值
     * @return 计算结果
     */
    public static BigDecimal multiply(Number left, Number right) {
        return toBigDecimal(left).multiply(toBigDecimal(right));
    }

    /**
     * 乘法，并按指定精度返回结果。
     *
     * @param left 左值
     * @param right 右值
     * @param scale 小数位数
     * @return 计算结果
     */
    public static BigDecimal multiply(Number left, Number right, int scale) {
        return scale(multiply(left, right), scale);
    }

    /**
     * 除法。
     *
     * <p>若结果为无限小数，请使用带精度参数的重载方法。</p>
     *
     * @param left 被除数
     * @param right 除数
     * @return 计算结果
     */
    public static BigDecimal divide(Number left, Number right) {
        BigDecimal divisor = toBigDecimal(right);
        requireNonZero(divisor, "除数不能为 0");
        try {
            return toBigDecimal(left).divide(divisor);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("除法结果无法精确表示，请使用带精度的重载方法", ex);
        }
    }

    /**
     * 除法，并按指定精度返回结果。
     *
     * @param left 被除数
     * @param right 除数
     * @param scale 小数位数
     * @return 计算结果
     */
    public static BigDecimal divide(Number left, Number right, int scale) {
        return divide(left, right, scale, RoundingMode.HALF_UP);
    }

    /**
     * 除法，并按指定精度和舍入模式返回结果。
     *
     * @param left 被除数
     * @param right 除数
     * @param scale 小数位数
     * @param roundingMode 舍入模式
     * @return 计算结果
     */
    public static BigDecimal divide(Number left, Number right, int scale, RoundingMode roundingMode) {
        checkScale(scale);
        if (roundingMode == null) {
            throw new IllegalArgumentException("舍入模式不能为空");
        }
        BigDecimal divisor = toBigDecimal(right);
        requireNonZero(divisor, "除数不能为 0");
        return toBigDecimal(left).divide(divisor, scale, roundingMode);
    }

    /**
     * 求余。
     *
     * <p>结果符号跟随被除数。</p>
     *
     * @param left 被除数
     * @param right 除数
     * @return 计算结果
     */
    public static BigDecimal remainder(Number left, Number right) {
        BigDecimal divisor = toBigDecimal(right);
        requireNonZero(divisor, "除数不能为 0");
        return toBigDecimal(left).remainder(divisor);
    }

    /**
     * 求余，并按指定精度返回结果。
     *
     * @param left 被除数
     * @param right 除数
     * @param scale 小数位数
     * @return 计算结果
     */
    public static BigDecimal remainder(Number left, Number right, int scale) {
        return scale(remainder(left, right), scale);
    }

    /**
     * 取模。
     *
     * <p>结果始终为非负数。</p>
     *
     * @param left 被除数
     * @param right 模数
     * @return 计算结果
     */
    public static BigDecimal mod(Number left, Number right) {
        BigDecimal divisor = toBigDecimal(right);
        requireNonZero(divisor, "模数不能为 0");
        BigDecimal modulus = divisor.abs();
        BigDecimal result = toBigDecimal(left).remainder(modulus);
        return result.signum() < 0 ? result.add(modulus) : result;
    }

    /**
     * 取模，并按指定精度返回结果。
     *
     * @param left 被除数
     * @param right 模数
     * @param scale 小数位数
     * @return 计算结果
     */
    public static BigDecimal mod(Number left, Number right, int scale) {
        return scale(mod(left, right), scale);
    }

    /**
     * 计算百分比。
     *
     * <p>等价于 {@code left / right * 100}。</p>
     *
     * @param left 分子
     * @param right 分母
     * @return 百分比结果
     */
    public static BigDecimal percent(Number left, Number right) {
        return divide(left, right).multiply(ONE_HUNDRED);
    }

    /**
     * 计算百分比，并按指定精度返回结果。
     *
     * @param left 分子
     * @param right 分母
     * @param scale 小数位数
     * @return 百分比结果
     */
    public static BigDecimal percent(Number left, Number right, int scale) {
        return percent(left, right, scale, RoundingMode.HALF_UP);
    }

    /**
     * 计算百分比，并按指定精度和舍入模式返回结果。
     *
     * @param left 分子
     * @param right 分母
     * @param scale 小数位数
     * @param roundingMode 舍入模式
     * @return 百分比结果
     */
    public static BigDecimal percent(Number left, Number right, int scale, RoundingMode roundingMode) {
        checkScale(scale);
        if (roundingMode == null) {
            throw new IllegalArgumentException("舍入模式不能为空");
        }
        return divide(left, right, scale + 2, roundingMode).multiply(ONE_HUNDRED).setScale(scale, roundingMode);
    }

    /**
     * 按指定精度处理结果。
     *
     * @param value 原始结果
     * @param scale 小数位数
     * @return 处理后的结果
     */
    private static BigDecimal scale(BigDecimal value, int scale) {
        checkScale(scale);
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 将数字转换为 {@link BigDecimal}。
     *
     * @param value 数字
     * @return 转换结果
     */
    private static BigDecimal toBigDecimal(Number value) {
        if (value == null) {
            throw new IllegalArgumentException("数字参数不能为空");
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 校验小数位数。
     *
     * @param scale 小数位数
     */
    private static void checkScale(int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("小数位数不能小于 0");
        }
    }

    /**
     * 校验除数或模数不能为零。
     *
     * @param value 数值
     * @param message 异常消息
     */
    private static void requireNonZero(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
