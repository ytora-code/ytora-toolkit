package xyz.ytora.toolkit.text;

import java.security.SecureRandom;
import java.util.*;

/**
 * 字符串处理工具类。
 *
 * <p>包含常用的字符串判空、空白处理、切割、拼接、格式化、命名风格转换和随机内容生成方法。</p>
 * <p>该类仅包含静态方法，不应被实例化。</p>
 *
 * @author ytora
 * @since 1.0-SNAPSHOT
 */
public final class Strs {

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB"};

    /**
     * 数字字符范围。
     */
    private static final String NUMBER_RANGE = "0123456789";

    /**
     * 小写字母字符范围。
     */
    private static final String LOWER_LETTER_RANGE = "abcdefghijklmnopqrstuvwxyz";

    /**
     * 大写字母字符范围。
     */
    private static final String UPPER_LETTER_RANGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 数字和字母字符范围。
     */
    private static final String ALPHANUMERIC_RANGE =
            NUMBER_RANGE + LOWER_LETTER_RANGE + UPPER_LETTER_RANGE;

    /**
     * 安全随机数生成器。
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    private Strs() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 判断字符串是否为 {@code null} 或空串。
     *
     * <p>示例：</p>
     * <p>{@code 输入(null) -> 输出: true}</p>
     * <p>{@code 输入("") -> 输出: true}</p>
     * <p>{@code 输入("abc") -> 输出: false}</p>
     *
     * @param value 待检查字符串
     * @return 为 {@code null} 或空串时返回 {@code true}
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * 判断字符串是否非 {@code null} 且非空串。
     *
     * <p>示例：</p>
     * <p>{@code 输入("abc") -> 输出: true}</p>
     * <p>{@code 输入("") -> 输出: false}</p>
     *
     * @param value 待检查字符串
     * @return 非空时返回 {@code true}
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    /**
     * 判断字符串是否为 {@code null}、空串或仅包含空白字符。
     *
     * <p>示例：</p>
     * <p>{@code 输入("  \t") -> 输出: true}</p>
     * <p>{@code 输入(" abc ") -> 输出: false}</p>
     *
     * @param value 待检查字符串
     * @return 为空白时返回 {@code true}
     */
    public static boolean isBlank(String value) {
        if (value == null) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否包含非空白内容。
     *
     * <p>示例：</p>
     * <p>{@code 输入(" abc ") -> 输出: true}</p>
     * <p>{@code 输入("   ") -> 输出: false}</p>
     *
     * @param value 待检查字符串
     * @return 包含非空白内容时返回 {@code true}
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    /**
     * 将 {@code null} 转换为空串。
     *
     * <p>示例：</p>
     * <p>{@code 输入(null) -> 输出: ""}</p>
     * <p>{@code 输入("abc") -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @return 原始字符串为 {@code null} 时返回空串，否则返回原值
     */
    public static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 将空串转换为 {@code null}。
     *
     * <p>示例：</p>
     * <p>{@code 输入("") -> 输出: null}</p>
     * <p>{@code 输入("abc") -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @return 原始字符串为空串时返回 {@code null}，否则返回原值
     */
    public static String emptyToNull(String value) {
        return isEmpty(value) ? null : value;
    }

    /**
     * 去除字符串首尾空白。
     *
     * <p>示例：</p>
     * <p>{@code 输入("  abc  ") -> 输出: "abc"}</p>
     * <p>{@code 输入(null) -> 输出: null}</p>
     *
     * @param value 原始字符串
     * @return 去除首尾空白后的结果；输入为 {@code null} 时返回 {@code null}
     */
    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 去除字符串首尾空白；若结果为空串则返回 {@code null}。
     *
     * <p>示例：</p>
     * <p>{@code 输入("  abc  ") -> 输出: "abc"}</p>
     * <p>{@code 输入("   ") -> 输出: null}</p>
     *
     * @param value 原始字符串
     * @return 去除首尾空白后的结果；结果为空串时返回 {@code null}
     */
    public static String trimToNull(String value) {
        String trimmed = trim(value);
        return isEmpty(trimmed) ? null : trimmed;
    }

    /**
     * 去除字符串首尾空白；输入为 {@code null} 时返回空串。
     *
     * <p>示例：</p>
     * <p>{@code 输入("  abc  ") -> 输出: "abc"}</p>
     * <p>{@code 输入(null) -> 输出: ""}</p>
     *
     * @param value 原始字符串
     * @return 去除首尾空白后的结果；输入为 {@code null} 时返回空串
     */
    public static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 当字符串为 {@code null} 或空串时返回默认值。
     *
     * <p>示例：</p>
     * <p>{@code 输入("", "N/A") -> 输出: "N/A"}</p>
     * <p>{@code 输入("abc", "N/A") -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @param defaultValue 默认值
     * @return 原始字符串为空时返回默认值，否则返回原值
     */
    public static String defaultIfEmpty(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }

    /**
     * 当字符串为空白时返回默认值。
     *
     * <p>示例：</p>
     * <p>{@code 输入("   ", "N/A") -> 输出: "N/A"}</p>
     * <p>{@code 输入("abc", "N/A") -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @param defaultValue 默认值
     * @return 原始字符串为空白时返回默认值，否则返回原值
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    /**
     * 安全比较两个字符串是否相等。
     *
     * <p>示例：</p>
     * <p>{@code 输入("abc", "abc") -> 输出: true}</p>
     * <p>{@code 输入(null, "abc") -> 输出: false}</p>
     * <p>{@code 输入(null, null) -> 输出: true}</p>
     *
     * @param left 左字符串
     * @param right 右字符串
     * @return 相等时返回 {@code true}
     */
    public static boolean equals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    /**
     * 忽略大小写比较两个字符串是否相等。
     *
     * <p>示例：</p>
     * <p>{@code 输入("Abc", "aBC") -> 输出: true}</p>
     * <p>{@code 输入("abc", "abd") -> 输出: false}</p>
     *
     * @param left 左字符串
     * @param right 右字符串
     * @return 忽略大小写后相等时返回 {@code true}
     */
    public static boolean equalsIgnoreCase(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equalsIgnoreCase(right);
    }

    /**
     * 判断字符串是否包含指定子串。
     *
     * <p>示例：</p>
     * <p>{@code 输入("hello world", "world") -> 输出: true}</p>
     * <p>{@code 输入("hello", "WORLD") -> 输出: false}</p>
     *
     * @param value 原始字符串
     * @param searchStr 待查找子串
     * @return 包含时返回 {@code true}
     */
    public static boolean contains(String value, String searchStr) {
        if (value == null || searchStr == null) {
            return false;
        }
        return value.contains(searchStr);
    }

    /**
     * 忽略大小写判断字符串是否包含指定子串。
     *
     * <p>示例：</p>
     * <p>{@code 输入("hello world", "WORLD") -> 输出: true}</p>
     *
     * @param value 原始字符串
     * @param searchStr 待查找子串
     * @return 包含时返回 {@code true}
     */
    public static boolean containsIgnoreCase(String value, String searchStr) {
        if (value == null || searchStr == null) {
            return false;
        }
        return value.toLowerCase(Locale.ROOT).contains(searchStr.toLowerCase(Locale.ROOT));
    }

    /**
     * 判断字符串是否以指定前缀开头。
     *
     * <p>示例：</p>
     * <p>{@code 输入("prefix-value", "pre") -> 输出: true}</p>
     *
     * @param value 原始字符串
     * @param prefix 前缀
     * @return 以指定前缀开头时返回 {@code true}
     */
    public static boolean startsWith(String value, String prefix) {
        if (value == null || prefix == null) {
            return false;
        }
        return value.startsWith(prefix);
    }

    /**
     * 忽略大小写判断字符串是否以指定前缀开头。
     *
     * <p>示例：</p>
     * <p>{@code 输入("PrefixValue", "pre") -> 输出: true}</p>
     *
     * @param value 原始字符串
     * @param prefix 前缀
     * @return 忽略大小写后以指定前缀开头时返回 {@code true}
     */
    public static boolean startsWithIgnoreCase(String value, String prefix) {
        if (value == null || prefix == null) {
            return false;
        }
        if (prefix.length() > value.length()) {
            return false;
        }
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * 判断字符串是否以指定后缀结尾。
     *
     * <p>示例：</p>
     * <p>{@code 输入("report.pdf", ".pdf") -> 输出: true}</p>
     *
     * @param value 原始字符串
     * @param suffix 后缀
     * @return 以指定后缀结尾时返回 {@code true}
     */
    public static boolean endsWith(String value, String suffix) {
        if (value == null || suffix == null) {
            return false;
        }
        return value.endsWith(suffix);
    }

    /**
     * 忽略大小写判断字符串是否以指定后缀结尾。
     *
     * <p>示例：</p>
     * <p>{@code 输入("report.PDF", ".pdf") -> 输出: true}</p>
     *
     * @param value 原始字符串
     * @param suffix 后缀
     * @return 忽略大小写后以指定后缀结尾时返回 {@code true}
     */
    public static boolean endsWithIgnoreCase(String value, String suffix) {
        if (value == null || suffix == null) {
            return false;
        }
        if (suffix.length() > value.length()) {
            return false;
        }
        int start = value.length() - suffix.length();
        return value.regionMatches(true, start, suffix, 0, suffix.length());
    }

    /**
     * 获取指定分隔符第一次出现前的内容。
     *
     * <p>示例：</p>
     * <p>{@code 输入("a:b:c", ":") -> 输出: "a"}</p>
     *
     * @param value 原始字符串
     * @param separator 分隔符
     * @return 分隔符前的内容；未找到分隔符时返回原字符串
     */
    public static String substringBefore(String value, String separator) {
        if (value == null || separator == null) {
            return value;
        }
        if (separator.isEmpty()) {
            return "";
        }
        int index = value.indexOf(separator);
        return index < 0 ? value : value.substring(0, index);
    }

    /**
     * 获取指定分隔符第一次出现后的内容。
     *
     * <p>示例：</p>
     * <p>{@code 输入("a:b:c", ":") -> 输出: "b:c"}</p>
     *
     * @param value 原始字符串
     * @param separator 分隔符
     * @return 分隔符后的内容；未找到分隔符时返回空串
     */
    public static String substringAfter(String value, String separator) {
        if (value == null) {
            return null;
        }
        if (separator == null) {
            return "";
        }
        if (separator.isEmpty()) {
            return value;
        }
        int index = value.indexOf(separator);
        return index < 0 ? "" : value.substring(index + separator.length());
    }

    /**
     * 获取指定分隔符最后一次出现前的内容。
     *
     * <p>示例：</p>
     * <p>{@code 输入("a:b:c", ":") -> 输出: "a:b"}</p>
     *
     * @param value 原始字符串
     * @param separator 分隔符
     * @return 分隔符前的内容；未找到分隔符时返回原字符串
     */
    public static String substringBeforeLast(String value, String separator) {
        if (value == null || separator == null || separator.isEmpty()) {
            return value;
        }
        int index = value.lastIndexOf(separator);
        return index < 0 ? value : value.substring(0, index);
    }

    /**
     * 获取指定分隔符最后一次出现后的内容。
     *
     * <p>示例：</p>
     * <p>{@code 输入("a:b:c", ":") -> 输出: "c"}</p>
     *
     * @param value 原始字符串
     * @param separator 分隔符
     * @return 分隔符后的内容；未找到分隔符时返回空串
     */
    public static String substringAfterLast(String value, String separator) {
        if (value == null) {
            return null;
        }
        if (separator == null || separator.isEmpty()) {
            return "";
        }
        int index = value.lastIndexOf(separator);
        return index < 0 ? "" : value.substring(index + separator.length());
    }

    /**
     * 获取两个标记之间的内容。
     *
     * <p>示例：</p>
     * <p>{@code 输入("[abc]", "[", "]") -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @param open 起始标记
     * @param close 结束标记
     * @return 两个标记之间的内容；未匹配成功时返回 {@code null}
     */
    public static String substringBetween(String value, String open, String close) {
        if (value == null || open == null || close == null) {
            return null;
        }
        int start = value.indexOf(open);
        if (start < 0) {
            return null;
        }
        start += open.length();
        int end = value.indexOf(close, start);
        if (end < 0) {
            return null;
        }
        return value.substring(start, end);
    }

    /**
     * 若字符串以指定前缀开头，则移除该前缀。
     *
     * <p>示例：</p>
     * <p>{@code 输入("pre-value", "pre-") -> 输出: "value"}</p>
     * <p>{@code 输入("value", "pre-") -> 输出: "value"}</p>
     *
     * @param value 原始字符串
     * @param prefix 待移除前缀
     * @return 移除前缀后的结果；未命中时返回原字符串
     */
    public static String removePrefix(String value, String prefix) {
        if (value == null || prefix == null || prefix.isEmpty()) {
            return value;
        }
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    /**
     * 若字符串以指定后缀结尾，则移除该后缀。
     *
     * <p>示例：</p>
     * <p>{@code 输入("report.txt", ".txt") -> 输出: "report"}</p>
     * <p>{@code 输入("report", ".txt") -> 输出: "report"}</p>
     *
     * @param value 原始字符串
     * @param suffix 待移除后缀
     * @return 移除后缀后的结果；未命中时返回原字符串
     */
    public static String removeSuffix(String value, String suffix) {
        if (value == null || suffix == null || suffix.isEmpty()) {
            return value;
        }
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }

    /**
     * 将字符串重复指定次数。
     *
     * <p>示例：</p>
     * <p>{@code 输入("ab", 3) -> 输出: "ababab"}</p>
     * <p>{@code 输入("ab", 0) -> 输出: ""}</p>
     *
     * @param value 原始字符串
     * @param times 重复次数
     * @return 重复拼接后的字符串
     */
    public static String repeat(String value, int times) {
        if (times < 0) {
            throw new IllegalArgumentException("重复次数不能小于 0");
        }
        if (times == 0 || isEmpty(value)) {
            return "";
        }
        StringBuilder builder = new StringBuilder(value.length() * times);
        for (int i = 0; i < times; i++) {
            builder.append(value);
        }
        return builder.toString();
    }

    /**
     * 将原始字符串的首字母变成大写
     *
     * <p>示例：</p>
     * <p>{@code firstCapitalize("name") -> 输出: "Name"}</p>
     *
     * @param value 原始字符串
     * @return 首字母大写后的字符串
     */
    public static String firstUppercase(String value) {
        if (isEmpty(value)) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * 将原始字符串的首字母变成小写
     *
     * <p>示例：</p>
     * <p>{@code firstCapitalize("Name") -> 输出: "name"}</p>
     *
     * @param value 原始字符串
     * @return 首字母大写后的字符串
     */
    public static String firstLowercase(String value) {
        if (isEmpty(value)) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    /**
     * 使用指定分隔符拼接可迭代对象中的元素。
     *
     * <p>若元素为 {@code null}，则按空串处理。</p>
     * <p>若 {@code values} 为 {@code null}，则返回空串。</p>
     * <p>示例：</p>
     * <p>{@code 输入(Arrays.asList("a", "b", "c"), "-") -> 输出: "a-b-c"}</p>
     * <p>{@code 输入(Arrays.asList("a", null, "c"), "-") -> 输出: "a--c"}</p>
     *
     * @param values 待拼接元素
     * @param delimiter 分隔符，为 {@code null} 时按空串处理
     * @return 拼接后的字符串
     */
    public static String join(Iterable<?> values, String delimiter) {
        if (values == null) {
            return "";
        }
        String actualDelimiter = delimiter == null ? "" : delimiter;
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                builder.append(actualDelimiter);
            }
            if (value != null) {
                builder.append(value);
            }
            first = false;
        }
        return builder.toString();
    }

    /**
     * 使用指定分隔符拼接数组中的元素。
     *
     * <p>若元素为 {@code null}，则按空串处理。</p>
     * <p>若 {@code values} 为 {@code null}，则返回空串。</p>
     * <p>示例：</p>
     * <p>{@code 输入(new Object[]{"a", "b", "c"}, "-") -> 输出: "a-b-c"}</p>
     * <p>{@code 输入(new Object[]{"a", null, "c"}, "-") -> 输出: "a--c"}</p>
     *
     * @param values 待拼接数组
     * @param delimiter 分隔符，为 {@code null} 时按空串处理
     * @return 拼接后的字符串
     */
    public static String join(Object[] values, String delimiter) {
        if (values == null || values.length == 0) {
            return "";
        }
        String actualDelimiter = delimiter == null ? "" : delimiter;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(actualDelimiter);
            }
            if (values[i] != null) {
                builder.append(values[i]);
            }
        }
        return builder.toString();
    }

    /**
     * 按指定分隔符切割字符串并返回列表。
     *
     * <p>该方法按完整分隔符进行切割，不使用正则表达式。</p>
     * <p>示例：</p>
     * <p>{@code 输入("a,b,c", ",") -> 输出: ["a", "b", "c"]}</p>
     *
     * @param value 原始字符串
     * @param delimiter 分隔符，不能为空串
     * @return 切割结果列表；原始字符串为 {@code null} 时返回空列表
     */
    public static List<String> splitToList(String value, String delimiter) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("分隔符不能为空");
        }
        List<String> result = new ArrayList<>();
        int start = 0;
        int index;
        while ((index = value.indexOf(delimiter, start)) >= 0) {
            result.add(value.substring(start, index));
            start = index + delimiter.length();
        }
        result.add(value.substring(start));
        return result;
    }

    /**
     * 按指定分隔符切割字符串，并去除每段首尾空白。
     *
     * <p>示例：</p>
     * <p>{@code 输入(" a , b ,  c ", ",") -> 输出: ["a", "b", "c"]}</p>
     * <p>{@code 输入(" a ,  , c ", ",") -> 输出: ["a", "", "c"]}</p>
     *
     * @param value 原始字符串
     * @param delimiter 分隔符，不能为空串
     * @return 去除首尾空白后的切割结果；原始字符串为 {@code null} 时返回空列表
     */
    public static List<String> splitAndTrim(String value, String delimiter) {
        List<String> parts = splitToList(value, delimiter);
        if (parts.isEmpty()) {
            return parts;
        }
        List<String> result = new ArrayList<>(parts.size());
        for (String part : parts) {
            result.add(trimToEmpty(part));
        }
        return result;
    }

    /**
     * 按指定分隔符切割字符串，去除每段首尾空白，并忽略空串结果。
     *
     * <p>示例：</p>
     * <p>{@code 输入(" a ,  , c ", ",") -> 输出: ["a", "c"]}</p>
     * <p>{@code 输入("  ,  ", ",") -> 输出: []}</p>
     *
     * @param value 原始字符串
     * @param delimiter 分隔符，不能为空串
     * @return 去除空串后的切割结果；原始字符串为 {@code null} 时返回空列表
     */
    public static List<String> splitAndTrimIgnoreEmpty(String value, String delimiter) {
        List<String> parts = splitAndTrim(value, delimiter);
        if (parts.isEmpty()) {
            return parts;
        }
        List<String> result = new ArrayList<>(parts.size());
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.add(part);
            }
        }
        return result;
    }

    /**
     * 使用占位符格式化字符串。
     *
     * <p>占位符使用 {@code {}}，会按参数顺序依次替换。</p>
     * <p>示例：</p>
     * <p>{@code 输入("Hello, {}!", "Tom") -> 输出: "Hello, Tom!"}</p>
     * <p>{@code 输入("{} + {} = {}", 1, 2, 3) -> 输出: "1 + 2 = 3"}</p>
     *
     * @param templateStr 模板字符串
     * @param args 参数列表
     * @return 格式化后的结果
     */
    public static String format(String templateStr, Object... args) {
        if (templateStr == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return templateStr;
        }
        StringBuilder builder = new StringBuilder(templateStr.length() + args.length * 16);
        int start = 0;
        int argIndex = 0;
        int placeholderIndex;
        while ((placeholderIndex = templateStr.indexOf("{}", start)) >= 0) {
            builder.append(templateStr, start, placeholderIndex);
            if (argIndex < args.length) {
                builder.append(args[argIndex++]);
            } else {
                builder.append("{}");
            }
            start = placeholderIndex + 2;
        }
        builder.append(templateStr.substring(start));
        return builder.toString();
    }

    /**
     * 使用命名占位符格式化字符串。
     *
     * <p>占位符使用 {@code {name}} 形式，会按键名替换为映射中的对应值。</p>
     * <p>若占位符未找到对应键，则保留原占位符不变。</p>
     * <p>示例：</p>
     * <p>{@code 输入("Hello, {name}", {"name": "Tom"}) -> 输出: "Hello, Tom"}</p>
     * <p>{@code 输入("{greet}, {name}", {"greet": "Hi"}) -> 输出: "Hi, {name}"}</p>
     *
     * @param templateStr 模板字符串
     * @param args 命名参数映射
     * @return 格式化后的结果
     */
    public static String formatNamed(String templateStr, Map<String, ?> args) {
        if (templateStr == null) {
            return null;
        }
        if (args == null || args.isEmpty()) {
            return templateStr;
        }
        StringBuilder builder = new StringBuilder(templateStr.length() + args.size() * 16);
        int start = 0;
        while (start < templateStr.length()) {
            int openIndex = templateStr.indexOf('{', start);
            if (openIndex < 0) {
                builder.append(templateStr.substring(start));
                break;
            }
            int closeIndex = templateStr.indexOf('}', openIndex + 1);
            if (closeIndex < 0) {
                builder.append(templateStr.substring(start));
                break;
            }
            builder.append(templateStr, start, openIndex);
            String key = templateStr.substring(openIndex + 1, closeIndex);
            if (!key.isEmpty() && args.containsKey(key)) {
                builder.append(args.get(key));
            } else {
                builder.append(templateStr, openIndex, closeIndex + 1);
            }
            start = closeIndex + 1;
        }
        return builder.toString();
    }

    /**
     * 将字符串截断到指定最大长度。
     *
     * <p>示例：</p>
     * <p>{@code 输入("abcdef", 4) -> 输出: "abcd"}</p>
     * <p>{@code 输入("abc", 4) -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @param maxLength 最大长度，必须大于等于 0
     * @return 截断后的结果；输入为 {@code null} 时返回 {@code null}
     */
    public static String truncate(String value, int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("最大长度不能小于 0");
        }
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * 将字符串截断到指定最大长度，并追加后缀。
     *
     * <p>示例：</p>
     * <p>{@code 输入("abcdef", 5, "..") -> 输出: "abc.."}</p>
     * <p>{@code 输入("abc", 5, "..") -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @param maxLength 最大长度，必须大于等于 0
     * @param suffix 截断后缀，为 {@code null} 时按空串处理
     * @return 截断后的结果；输入为 {@code null} 时返回 {@code null}
     */
    public static String truncate(String value, int maxLength, String suffix) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("最大长度不能小于 0");
        }
        String actualSuffix = suffix == null ? "" : suffix;
        if (actualSuffix.length() > maxLength) {
            throw new IllegalArgumentException("后缀长度不能大于最大长度");
        }
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength - actualSuffix.length()) + actualSuffix;
    }

    /**
     * 将字符串首字母转为大写。
     *
     * <p>示例：</p>
     * <p>{@code 输入("hello") -> 输出: "Hello"}</p>
     *
     * @param value 原始字符串
     * @return 首字母大写后的结果
     */
    public static String capitalize(String value) {
        if (isEmpty(value)) {
            return value;
        }
        char first = value.charAt(0);
        char capitalized = Character.toUpperCase(first);
        if (first == capitalized) {
            return value;
        }
        return capitalized + value.substring(1);
    }

    /**
     * 将字符串首字母转为小写。
     *
     * <p>示例：</p>
     * <p>{@code 输入("Hello") -> 输出: "hello"}</p>
     *
     * @param value 原始字符串
     * @return 首字母小写后的结果
     */
    public static String uncapitalize(String value) {
        if (isEmpty(value)) {
            return value;
        }
        char first = value.charAt(0);
        char uncapitalized = Character.toLowerCase(first);
        if (first == uncapitalized) {
            return value;
        }
        return uncapitalized + value.substring(1);
    }

    /**
     * 使用指定字符在左侧补齐到目标长度。
     *
     * <p>示例：</p>
     * <p>{@code 输入("7", 3, '0') -> 输出: "007"}</p>
     * <p>{@code 输入("abc", 2, '0') -> 输出: "abc"}</p>
     *
     * @param value 原始字符串
     * @param length 目标长度，必须大于等于 0
     * @param padChar 补齐字符
     * @return 补齐后的结果；输入为 {@code null} 时返回 {@code null}
     */
    public static String leftPad(String value, int length, char padChar) {
        if (length < 0) {
            throw new IllegalArgumentException("目标长度不能小于 0");
        }
        if (value == null || value.length() >= length) {
            return value;
        }
        StringBuilder builder = new StringBuilder(length);
        for (int i = value.length(); i < length; i++) {
            builder.append(padChar);
        }
        builder.append(value);
        return builder.toString();
    }

    /**
     * 判断字符串是否全部由数字组成。
     *
     * <p>示例：</p>
     * <p>{@code 输入("2024") -> 输出: true}</p>
     * <p>{@code 输入("20A4") -> 输出: false}</p>
     *
     * @param value 原始字符串
     * @return 全为数字时返回 {@code true}
     */
    public static boolean isNumeric(String value) {
        if (isEmpty(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否全部由字母或数字组成。
     *
     * <p>示例：</p>
     * <p>{@code 输入("A1b2") -> 输出: true}</p>
     * <p>{@code 输入("A1-b2") -> 输出: false}</p>
     *
     * @param value 原始字符串
     * @return 全为字母或数字时返回 {@code true}
     */
    public static boolean isAlphanumeric(String value) {
        if (isEmpty(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isLetterOrDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 统计指定子串在字符串中出现的次数。
     *
     * <p>示例：</p>
     * <p>{@code 输入("ababa", "aba") -> 输出: 1}</p>
     * <p>{@code 输入("a,b,c", ",") -> 输出: 2}</p>
     *
     * @param value 原始字符串
     * @param searchStr 待查找子串
     * @return 出现次数；输入无效时返回 0
     */
    public static int countMatches(String value, String searchStr) {
        if (isEmpty(value) || isEmpty(searchStr)) {
            return 0;
        }
        int count = 0;
        int fromIndex = 0;
        int index;
        while ((index = value.indexOf(searchStr, fromIndex)) >= 0) {
            count++;
            fromIndex = index + searchStr.length();
        }
        return count;
    }

    /**
     * 将连续空白字符规范为单个空格，并去除首尾空白。
     *
     * <p>示例：</p>
     * <p>{@code 输入("  a\t b \n c  ") -> 输出: "a b c"}</p>
     * <p>{@code 输入("   ") -> 输出: ""}</p>
     *
     * @param value 原始字符串
     * @return 规范化后的结果；输入为 {@code null} 时返回 {@code null}
     */
    public static String normalizeWhitespace(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(value.length());
        boolean previousWhitespace = true;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isWhitespace(current)) {
                if (!previousWhitespace) {
                    builder.append(' ');
                    previousWhitespace = true;
                }
            } else {
                builder.append(current);
                previousWhitespace = false;
            }
        }
        int length = builder.length();
        if (length > 0 && builder.charAt(length - 1) == ' ') {
            builder.deleteCharAt(length - 1);
        }
        return builder.toString();
    }

    /**
     * 生成指定长度的随机数字字符串。
     *
     * <p>示例：</p>
     * <p>{@code 输入(6) -> 输出: "381205"}</p>
     * <p>输出结果为随机值，上述内容仅为格式示例。</p>
     *
     * @param length 目标长度，必须大于等于 0
     * @return 随机数字字符串
     */
    public static String randomNumber(int length) {
        return random(length, NUMBER_RANGE);
    }

    /**
     * 生成指定长度的随机字母数字字符串。
     *
     * <p>示例：</p>
     * <p>{@code 输入(8) -> 输出: "aB3k9Pq1"}</p>
     * <p>输出结果为随机值，上述内容仅为格式示例。</p>
     *
     * @param length 目标长度，必须大于等于 0
     * @return 随机字母数字字符串
     */
    public static String randomString(int length) {
        return random(length, ALPHANUMERIC_RANGE);
    }

    /**
     * 将下划线命名、短横线命名或空白分隔命名转换为小驼峰命名。
     *
     * <p>示例：</p>
     * <p>{@code 输入("user_name") -> 输出: "userName"}</p>
     * <p>{@code 输入("user-name info") -> 输出: "userNameInfo"}</p>
     *
     * @param value 原始字符串
     * @return 转换后的小驼峰字符串
     */
    public static String toCamelCase(String value) {
        if (isBlank(value)) {
            return value;
        }
        String normalized = normalizeWordSeparators(value);
        String[] parts = normalized.split("_+");
        StringBuilder builder = new StringBuilder(value.length());
        boolean firstAppended = false;
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            String lower = part.toLowerCase(Locale.ROOT);
            if (!firstAppended) {
                builder.append(lower);
                firstAppended = true;
            } else {
                builder.append(Character.toUpperCase(lower.charAt(0)));
                builder.append(lower.substring(1));
            }
        }
        return builder.toString();
    }

    /**
     * 将字符串转换为下划线命名。
     *
     * <p>示例：</p>
     * <p>{@code 输入("userName") -> 输出: "user_name"}</p>
     * <p>{@code 输入("HTTPServerPort") -> 输出: "http_server_port"}</p>
     *
     * @param value 原始字符串
     * @return 转换后的下划线命名字符串
     */
    public static String toSnakeCase(String value) {
        return convertCamelOrMixedToDelimited(value, '_');
    }

    /**
     * 将字符串转换为短横线命名。
     *
     * <p>示例：</p>
     * <p>{@code 输入("userName") -> 输出: "user-name"}</p>
     * <p>{@code 输入("HTTPServerPort") -> 输出: "http-server-port"}</p>
     *
     * @param value 原始字符串
     * @return 转换后的短横线命名字符串
     */
    public static String toKebabCase(String value) {
        return convertCamelOrMixedToDelimited(value, '-');
    }

    /**
     * 将目标数字变成指定位数的字符串，不足前面填充0<br/>
     *  fillZero(123, 5) -> "00123"
     * @param targetNum 目标数字
     * @param length    字符串长度
     * @return 指定长度的字符串
     */
    public static String fillZero(int targetNum, int length) {
        String targetNumStr = String.valueOf(targetNum);
        if (targetNumStr.length() >= length) return targetNumStr;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - targetNumStr.length(); i++) {
            sb.append(0);
        }
        sb.append(targetNumStr);
        return sb.toString();
    }

    /**
     * 格式化字节数为带单位的字符串（保留两位小数）
     * @param bytes 字节数
     * @return 格式化字符串，如 1.23 MB
     */
    public static String formatSize(long bytes) {
        if (bytes <= 0) return "0 B";

        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < UNITS.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, UNITS[unitIndex]);
    }

    /**
     * 将带单位的文件大小字符串（如 1.23 MB）转换为字节数
     * 支持单位：B、KB、MB、GB、TB、PB（不区分大小写）
     * @param sizeStr 带单位的字符串
     * @return 字节数
     */
    public static long parseSize(String sizeStr) {
        if (Strs.isEmpty(sizeStr)) {
            throw new IllegalArgumentException("Size string cannot be null or empty");
        }

        String normalized = sizeStr.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        String numberPart = normalized.replaceAll("[A-Z]+", "");
        String unitPart = normalized.replaceAll("[^A-Z]+", "");

        if (numberPart.isEmpty() || unitPart.isEmpty()) {
            throw new IllegalArgumentException("Invalid size format: " + sizeStr);
        }

        double number = Double.parseDouble(numberPart);
        int unitIndex = -1;

        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equals(unitPart)) {
                unitIndex = i;
                break;
            }
        }

        if (unitIndex == -1) {
            throw new IllegalArgumentException("Unknown size unit: " + unitPart);
        }

        return (long) (number * Math.pow(1024, unitIndex));
    }

    /**
     * 将毫秒转换为可读的时间格式
     * @param uptimeMillis 毫秒数
     * @return 格式化后的字符串，如："2天3小时45分钟12秒"
     */
    public static String formatMillis(long uptimeMillis) {
        if (uptimeMillis <= 0) {
            return "0秒";
        }

        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        // 计算余数
        long remainingHours = hours % 24;
        long remainingMinutes = minutes % 60;
        long remainingSeconds = seconds % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append("天");
        }
        if (remainingHours > 0) {
            sb.append(remainingHours).append("小时");
        }
        if (remainingMinutes > 0) {
            sb.append(remainingMinutes).append("分钟");
        }
        if (remainingSeconds > 0) {
            sb.append(remainingSeconds).append("秒");
        }

        // 如果所有值都是0，返回小于1秒
        if (sb.length() == 0) {
            sb.append("小于1秒");
        }

        return sb.toString();
    }

    /**
     * 按指定字符范围生成固定长度的随机字符串。
     *
     * @param length 目标长度
     * @param range 可选字符范围
     * @return 随机结果
     */
    private static String random(int length, String range) {
        if (length < 0) {
            throw new IllegalArgumentException("长度不能小于 0");
        }
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(range.length());
            chars[i] = range.charAt(index);
        }
        return new String(chars);
    }

    /**
     * 将短横线和空白字符统一规范为下划线，便于后续进行命名风格转换。
     *
     * @param value 原始字符串
     * @return 规范化后的字符串
     */
    private static String normalizeWordSeparators(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '-' || Character.isWhitespace(current)) {
                builder.append('_');
            } else {
                builder.append(current);
            }
        }
        return builder.toString();
    }

    /**
     * 将驼峰或混合风格字符串转换为指定分隔符风格，例如下划线命名或短横线命名。
     *
     * @param value 原始字符串
     * @param delimiter 目标分隔符
     * @return 转换后的结果
     */
    private static String convertCamelOrMixedToDelimited(String value, char delimiter) {
        if (isBlank(value)) {
            return value;
        }
        StringBuilder builder = new StringBuilder(value.length() + 8);
        char previous = 0;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '_' || current == '-' || Character.isWhitespace(current)) {
                if (builder.length() > 0 && builder.charAt(builder.length() - 1) != delimiter) {
                    builder.append(delimiter);
                }
                previous = current;
                continue;
            }
            if (Character.isUpperCase(current)) {
                boolean hasPrevious = builder.length() > 0;
                boolean previousIsLetterOrDigit = Character.isLetterOrDigit(previous);
                boolean nextIsLowerCase = i + 1 < value.length()
                        && Character.isLowerCase(value.charAt(i + 1));
                boolean previousIsLowerCaseOrDigit =
                        Character.isLowerCase(previous) || Character.isDigit(previous);
                if (hasPrevious
                        && previousIsLetterOrDigit
                        && (previousIsLowerCaseOrDigit || nextIsLowerCase)
                        && builder.charAt(builder.length() - 1) != delimiter) {
                    builder.append(delimiter);
                }
                builder.append(Character.toLowerCase(current));
            } else {
                builder.append(Character.toLowerCase(current));
            }
            previous = current;
        }
        int length = builder.length();
        if (length > 0 && builder.charAt(length - 1) == delimiter) {
            builder.deleteCharAt(length - 1);
        }
        return builder.toString();
    }
}
