package xyz.ytora.toolkit.text;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 正则校验工具类。
 *
 * <p>用于判断输入字符串是否符合常见文本规则，例如中文、数字、邮箱、手机号、身份证号等。</p>
 *
 * @author ytora
 * @since 1.0-SNAPSHOT
 */
public final class Regs {

    private static final int ID_CARD_LENGTH = 18;
    private static final int ID_CARD_15_LENGTH = 15;
    private static final int MOBILE_LENGTH = 11;
    private static final int EMAIL_MAX_LENGTH = 254;
    private static final int EMAIL_LOCAL_PART_MAX_LENGTH = 64;
    private static final int LABEL_MAX_LENGTH = 63;
    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6,
            3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7',
            '6', '5', '4', '3', '2'};

    private static final Pattern CHINESE_PATTERN = Pattern.compile("^[\\u4E00-\\u9FFF]+$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^[+-]?\\d+$");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^[+-]?(\\d+\\.\\d+|\\d+|\\.\\d+)$");
    private static final Pattern LETTER_PATTERN = Pattern.compile("^[A-Za-z]+$");
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ID_CARD_18_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])"
                    + "(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");
    private static final Pattern ID_CARD_15_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}$");
    private static final Pattern LANDLINE_PATTERN = Pattern.compile(
            "^(?:(?:0\\d{2,3}-?)?\\d{7,8}|400-?\\d{3}-?\\d{4}|800-?\\d{3}-?\\d{4})(?:-\\d{1,6})?$");
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
                    + "(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}"
                    + "-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$");
    private static final Pattern EMAIL_LOCAL_PART_PATTERN = Pattern.compile(
            "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$");
    private static final Pattern EMAIL_DOMAIN_LABEL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile(
            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss")
            .withResolverStyle(ResolverStyle.STRICT);

    private Regs() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 判断输入字符串是否匹配指定正则表达式。
     *
     * <p>示例：</p>
     * <p>{@code 输入("abc123", "^[a-z0-9]+$") -> 输出: true}</p>
     * <p>{@code 输入("abc-123", "^[a-z0-9]+$") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @param regex 正则表达式
     * @return 匹配时返回 {@code true}
     */
    public static boolean matches(String value, String regex) {
        if (value == null || regex == null) {
            return false;
        }
        return value.matches(regex);
    }

    /**
     * 判断输入字符串是否匹配指定正则模式。
     *
     * <p>示例：</p>
     * <p>{@code 输入("abc123", 预编译模式) -> 输出: true}</p>
     *
     * @param value 待判断字符串
     * @param pattern 预编译模式
     * @return 匹配时返回 {@code true}
     */
    public static boolean matches(String value, Pattern pattern) {
        if (value == null || pattern == null) {
            return false;
        }
        return pattern.matcher(value).matches();
    }

    /**
     * 判断输入字符串是否全部由中文字符组成。
     *
     * <p>示例：</p>
     * <p>{@code 输入("你好世界") -> 输出: true}</p>
     * <p>{@code 输入("你好123") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 全为中文时返回 {@code true}
     */
    public static boolean isChinese(String value) {
        return matches(value, CHINESE_PATTERN);
    }

    /**
     * 判断输入字符串是否全部由数字组成。
     *
     * <p>示例：</p>
     * <p>{@code 输入("2024") -> 输出: true}</p>
     * <p>{@code 输入("20A4") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 全为数字时返回 {@code true}
     */
    public static boolean isNumber(String value) {
        return matches(value, NUMBER_PATTERN);
    }

    /**
     * 判断输入字符串是否为整数。
     *
     * <p>示例：</p>
     * <p>{@code 输入("-12") -> 输出: true}</p>
     * <p>{@code 输入("12.5") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为整数时返回 {@code true}
     */
    public static boolean isInteger(String value) {
        return matches(value, INTEGER_PATTERN);
    }

    /**
     * 判断输入字符串是否为十进制数值。
     *
     * <p>示例：</p>
     * <p>{@code 输入("12.5") -> 输出: true}</p>
     * <p>{@code 输入(".75") -> 输出: true}</p>
     * <p>{@code 输入("12a") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为十进制数值时返回 {@code true}
     */
    public static boolean isDecimal(String value) {
        return matches(value, DECIMAL_PATTERN);
    }

    /**
     * 判断输入字符串是否全部由英文字母组成。
     *
     * <p>示例：</p>
     * <p>{@code 输入("AbCd") -> 输出: true}</p>
     * <p>{@code 输入("Ab12") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 全为字母时返回 {@code true}
     */
    public static boolean isLetter(String value) {
        return matches(value, LETTER_PATTERN);
    }

    /**
     * 判断输入字符串是否全部由字母或数字组成。
     *
     * <p>示例：</p>
     * <p>{@code 输入("Ab12") -> 输出: true}</p>
     * <p>{@code 输入("Ab-12") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 全为字母或数字时返回 {@code true}
     */
    public static boolean isAlphanumeric(String value) {
        return matches(value, ALPHANUMERIC_PATTERN);
    }

    /**
     * 判断输入字符串是否为严格的日期文本，格式为 {@code yyyy-MM-dd}。
     *
     * <p>示例：</p>
     * <p>{@code 输入("2024-02-29") -> 输出: true}</p>
     * <p>{@code 输入("2023-02-29") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为严格日期文本时返回 {@code true}
     */
    public static boolean isDate(String value) {
        if (!matches(value, DATE_PATTERN)) {
            return false;
        }
        try {
            LocalDate.parse(value, DATE_FORMATTER);
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }

    /**
     * 判断输入字符串是否为严格的日期时间文本，格式为 {@code yyyy-MM-dd HH:mm:ss}。
     *
     * <p>示例：</p>
     * <p>{@code 输入("2024-02-29 23:59:59") -> 输出: true}</p>
     * <p>{@code 输入("2024-02-29 24:00:00") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为严格日期时间文本时返回 {@code true}
     */
    public static boolean isDateTime(String value) {
        if (!matches(value, DATE_TIME_PATTERN)) {
            return false;
        }
        try {
            LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }

    /**
     * 判断输入字符串是否为邮箱地址。
     *
     * <p>示例：</p>
     * <p>{@code 输入("user@example.com") -> 输出: true}</p>
     * <p>{@code 输入("user@@example.com") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为邮箱地址时返回 {@code true}
     */
    public static boolean isEmail(String value) {
        if (value == null || value.isEmpty() || value.length() > EMAIL_MAX_LENGTH) {
            return false;
        }
        int atIndex = value.indexOf('@');
        if (atIndex <= 0 || atIndex != value.lastIndexOf('@') || atIndex == value.length() - 1) {
            return false;
        }
        String localPart = value.substring(0, atIndex);
        String domainPart = value.substring(atIndex + 1);
        if (localPart.length() > EMAIL_LOCAL_PART_MAX_LENGTH
                || !matches(localPart, EMAIL_LOCAL_PART_PATTERN)) {
            return false;
        }
        return isEmailDomain(domainPart);
    }

    /**
     * 判断输入字符串是否为中国大陆手机号。
     *
     * <p>示例：</p>
     * <p>{@code 输入("13800138000") -> 输出: true}</p>
     * <p>{@code 输入("12800138000") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为中国大陆手机号时返回 {@code true}
     */
    public static boolean isMobile(String value) {
        return value != null
                && value.length() == MOBILE_LENGTH
                && matches(value, MOBILE_PATTERN);
    }

    /**
     * 判断输入字符串是否为固定电话或服务电话。
     *
     * <p>支持常见格式，如区号加号码、纯本地号码，以及 {@code 400} / {@code 800} 电话。</p>
     * <p>示例：</p>
     * <p>{@code 输入("010-88886666") -> 输出: true}</p>
     * <p>{@code 输入("400-123-4567") -> 输出: true}</p>
     * <p>{@code 输入("12345") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为固定电话或服务电话时返回 {@code true}
     */
    public static boolean isLandline(String value) {
        return matches(value, LANDLINE_PATTERN);
    }

    /**
     * 判断输入字符串是否为常见电话号码。
     *
     * <p>当前包含中国大陆手机号、固定电话，以及 {@code 400} / {@code 800} 电话。</p>
     * <p>示例：</p>
     * <p>{@code 输入("13800138000") -> 输出: true}</p>
     * <p>{@code 输入("010-88886666") -> 输出: true}</p>
     * <p>{@code 输入("12345") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为常见电话号码时返回 {@code true}
     */
    public static boolean isPhone(String value) {
        return isMobile(value) || isLandline(value);
    }

    /**
     * 判断输入字符串是否为中国大陆身份证号。
     *
     * <p>当前支持 15 位和 18 位身份证号。</p>
     * <p>示例：</p>
     * <p>{@code 输入("11010519491231002X") -> 输出: true}</p>
     * <p>{@code 输入("130503670401001") -> 输出: true}</p>
     * <p>{@code 输入("11010519491331002X") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为身份证号格式时返回 {@code true}
     */
    public static boolean isIdCard(String value) {
        return isIdCard18(value) || isIdCard15(value);
    }

    /**
     * 判断输入字符串是否为中国大陆 18 位身份证号。
     *
     * <p>该方法校验基本格式、出生日期是否合法，以及最后一位校验码是否正确。</p>
     * <p>示例：</p>
     * <p>{@code 输入("11010519491231002X") -> 输出: true}</p>
     * <p>{@code 输入("110105194912310021") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为 18 位身份证号时返回 {@code true}
     */
    public static boolean isIdCard18(String value) {
        if (!matches(value, ID_CARD_18_PATTERN)) {
            return false;
        }
        if (!isValidIdCardBirthDate18(value)) {
            return false;
        }
        return isValidIdCardCheckCode(value);
    }

    /**
     * 判断输入字符串是否为中国大陆 15 位身份证号。
     *
     * <p>该方法校验基本格式以及出生日期是否合法。</p>
     * <p>示例：</p>
     * <p>{@code 输入("130503670401001") -> 输出: true}</p>
     * <p>{@code 输入("130503670431001") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为 15 位身份证号时返回 {@code true}
     */
    public static boolean isIdCard15(String value) {
        if (!matches(value, ID_CARD_15_PATTERN)) {
            return false;
        }
        return isValidIdCardBirthDate15(value);
    }

    /**
     * 判断输入字符串是否为以 {@code http://} 或 {@code https://} 开头的 URL。
     *
     * <p>示例：</p>
     * <p>{@code 输入("https://example.com") -> 输出: true}</p>
     * <p>{@code 输入("ftp://example.com") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为 URL 时返回 {@code true}
     */
    public static boolean isUrl(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
                return false;
            }
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                return false;
            }
            if (host.contains("..")) {
                return false;
            }
            return !value.contains(" ");
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    /**
     * 判断输入字符串是否为 IPv4 地址。
     *
     * <p>示例：</p>
     * <p>{@code 输入("192.168.1.1") -> 输出: true}</p>
     * <p>{@code 输入("256.168.1.1") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为 IPv4 地址时返回 {@code true}
     */
    public static boolean isIpv4(String value) {
        return matches(value, IPV4_PATTERN);
    }

    /**
     * 判断输入字符串是否为 UUID。
     *
     * <p>示例：</p>
     * <p>{@code 输入("550e8400-e29b-41d4-a716-446655440000") -> 输出: true}</p>
     * <p>{@code 输入("not-a-uuid") -> 输出: false}</p>
     *
     * @param value 待判断字符串
     * @return 为 UUID 时返回 {@code true}
     */
    public static boolean isUuid(String value) {
        return matches(value, UUID_PATTERN);
    }

    private static boolean isEmailDomain(String domainPart) {
        if (domainPart.isEmpty() || domainPart.length() > 253 || domainPart.startsWith(".")
                || domainPart.endsWith(".")) {
            return false;
        }
        String[] labels = domainPart.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        for (String label : labels) {
            if (label.isEmpty() || label.length() > LABEL_MAX_LENGTH
                    || !matches(label, EMAIL_DOMAIN_LABEL_PATTERN)) {
                return false;
            }
        }
        String topLevelDomain = labels[labels.length - 1];
        return topLevelDomain.length() >= 2 && !isNumber(topLevelDomain);
    }

    private static boolean isValidIdCardBirthDate18(String value) {
        try {
            int year = Integer.parseInt(value.substring(6, 10));
            int month = Integer.parseInt(value.substring(10, 12));
            int day = Integer.parseInt(value.substring(12, 14));
            LocalDate birthDate = LocalDate.of(year, month, day);
            return !birthDate.isAfter(LocalDate.now());
        } catch (DateTimeException ex) {
            return false;
        }
    }

    private static boolean isValidIdCardBirthDate15(String value) {
        if (value == null || value.length() != ID_CARD_15_LENGTH) {
            return false;
        }
        try {
            int year = 1900 + Integer.parseInt(value.substring(6, 8));
            int month = Integer.parseInt(value.substring(8, 10));
            int day = Integer.parseInt(value.substring(10, 12));
            LocalDate birthDate = LocalDate.of(year, month, day);
            return !birthDate.isAfter(LocalDate.now());
        } catch (DateTimeException ex) {
            return false;
        }
    }

    private static boolean isValidIdCardCheckCode(String value) {
        if (value.length() != ID_CARD_LENGTH) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < ID_CARD_WEIGHTS.length; i++) {
            sum += (value.charAt(i) - '0') * ID_CARD_WEIGHTS[i];
        }
        char expectedCheckCode = ID_CARD_CHECK_CODES[sum % 11];
        char actualCheckCode = Character.toUpperCase(value.charAt(ID_CARD_LENGTH - 1));
        return actualCheckCode == expectedCheckCode;
    }
}
