package xyz.ytora.toolkit.text;

/**
 * 字符工具类。
 *
 * <p>提供常见的字符判断能力，以及全角/半角字符与字符串的相互转换。</p>
 * <p>本类仅包含静态方法，不支持实例化。</p>
 *
 * @author ytora
 * @since 1.0-SNAPSHOT
 */
public final class Chars {

    private Chars() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 判断字符是否属于 ASCII 范围。
     *
     * <p>示例：</p>
     * <p>{@code isAscii('A') -> true}</p>
     * <p>{@code isAscii('中') -> false}</p>
     *
     * @param value 待判断字符
     * @return 当字符编码不大于 {@code 0x7F} 时返回 {@code true}
     */
    public static boolean isAscii(char value) {
        return value <= 0x7F;
    }

    /**
     * 判断字符是否为 ASCII 英文字母。
     *
     * <p>示例：</p>
     * <p>{@code isAsciiLetter('A') -> true}</p>
     * <p>{@code isAsciiLetter('z') -> true}</p>
     * <p>{@code isAsciiLetter('1') -> false}</p>
     *
     * @param value 待判断字符
     * @return 当字符属于 ASCII 大写或小写字母时返回 {@code true}
     */
    public static boolean isAsciiLetter(char value) {
        return isAsciiLowerCase(value) || isAsciiUpperCase(value);
    }

    /**
     * 判断字符是否为 ASCII 小写字母。
     *
     * <p>示例：</p>
     * <p>{@code isAsciiLowerCase('a') -> true}</p>
     * <p>{@code isAsciiLowerCase('A') -> false}</p>
     *
     * @param value 待判断字符
     * @return 当字符位于 {@code a-z} 范围内时返回 {@code true}
     */
    public static boolean isAsciiLowerCase(char value) {
        return value >= 'a' && value <= 'z';
    }

    /**
     * 判断字符是否为 ASCII 大写字母。
     *
     * <p>示例：</p>
     * <p>{@code isAsciiUpperCase('A') -> true}</p>
     * <p>{@code isAsciiUpperCase('a') -> false}</p>
     *
     * @param value 待判断字符
     * @return 当字符位于 {@code A-Z} 范围内时返回 {@code true}
     */
    public static boolean isAsciiUpperCase(char value) {
        return value >= 'A' && value <= 'Z';
    }

    /**
     * 判断字符是否为 ASCII 数字。
     *
     * <p>示例：</p>
     * <p>{@code isAsciiDigit('7') -> true}</p>
     * <p>{@code isAsciiDigit('x') -> false}</p>
     *
     * @param value 待判断字符
     * @return 当字符位于 {@code 0-9} 范围内时返回 {@code true}
     */
    public static boolean isAsciiDigit(char value) {
        return value >= '0' && value <= '9';
    }

    /**
     * 判断字符是否为中文汉字脚本。
     *
     * <p>基于 {@code Character.UnicodeScript.HAN} 判断，适合常见汉字字符识别。</p>
     * <p>示例：</p>
     * <p>{@code isChinese('中') -> true}</p>
     * <p>{@code isChinese('A') -> false}</p>
     *
     * @param value 待判断字符
     * @return 当字符属于汉字脚本时返回 {@code true}
     */
    public static boolean isChinese(char value) {
        return Character.UnicodeScript.of(value) == Character.UnicodeScript.HAN;
    }

    /**
     * 判断字符是否为空白字符。
     *
     * <p>内部调用 {@link Character#isWhitespace(char)}。</p>
     * <p>示例：</p>
     * <p>{@code isWhitespace(' ') -> true}</p>
     * <p>{@code isWhitespace('\n') -> true}</p>
     * <p>{@code isWhitespace('x') -> false}</p>
     *
     * @param value 待判断字符
     * @return 当字符为空白字符时返回 {@code true}
     */
    public static boolean isWhitespace(char value) {
        return Character.isWhitespace(value);
    }

    /**
     * 将单个全角字符转换为半角字符。
     *
     * <p>支持将全角空格 {@code \u3000} 转为空格 {@code ' '}，以及
     * 全角可见字符 {@code \uFF01-\uFF5E} 转为对应的半角字符。</p>
     * <p>若字符本身不在可转换范围内，则原样返回。</p>
     * <p>示例：</p>
     * <p>{@code toHalfWidth('Ａ') -> 'A'}</p>
     * <p>{@code toHalfWidth('１') -> '1'}</p>
     * <p>{@code toHalfWidth('中') -> '中'}</p>
     *
     * @param value 待转换字符
     * @return 转换后的半角字符
     */
    public static char toHalfWidth(char value) {
        if (value == '\u3000') {
            return ' ';
        }
        if (value >= '\uFF01' && value <= '\uFF5E') {
            return (char) (value - 65248);
        }
        return value;
    }

    /**
     * 将单个半角字符转换为全角字符。
     *
     * <p>支持将普通空格 {@code ' '} 转为全角空格 {@code \u3000}，以及
     * 半角可见字符 {@code 33-126} 转为对应的全角字符。</p>
     * <p>若字符本身不在可转换范围内，则原样返回。</p>
     * <p>示例：</p>
     * <p>{@code toFullWidth('A') -> 'Ａ'}</p>
     * <p>{@code toFullWidth('1') -> '１'}</p>
     * <p>{@code toFullWidth('中') -> '中'}</p>
     *
     * @param value 待转换字符
     * @return 转换后的全角字符
     */
    public static char toFullWidth(char value) {
        if (value == ' ') {
            return '\u3000';
        }
        if (value >= 33 && value <= 126) {
            return (char) (value + 65248);
        }
        return value;
    }

    /**
     * 将字符串中的全角字符批量转换为半角字符。
     *
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code toHalfWidth("ＡＢＣ　１２３") -> "ABC 123"}</p>
     * <p>{@code toHalfWidth(null) -> null}</p>
     *
     * @param value 待转换字符串
     * @return 转换后的半角字符串
     */
    public static String toHalfWidth(String value) {
        if (value == null) {
            return null;
        }
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = toHalfWidth(chars[i]);
        }
        return new String(chars);
    }

    /**
     * 将字符串中的半角字符批量转换为全角字符。
     *
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code toFullWidth("ABC 123") -> "ＡＢＣ　１２３"}</p>
     * <p>{@code toFullWidth(null) -> null}</p>
     *
     * @param value 待转换字符串
     * @return 转换后的全角字符串
     */
    public static String toFullWidth(String value) {
        if (value == null) {
            return null;
        }
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = toFullWidth(chars[i]);
        }
        return new String(chars);
    }
}
