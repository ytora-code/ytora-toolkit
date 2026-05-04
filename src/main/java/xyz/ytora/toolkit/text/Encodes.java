package xyz.ytora.toolkit.text;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 编码与转义工具类。
 *
 * <p>提供常见的 URL 编解码、Base64 编解码以及 HTML 转义与反转义能力。</p>
 * <p>默认字符集为 UTF-8；当调用带 {@link Charset} 参数的方法且字符集为 {@code null} 时，
 * 也会自动回退到 UTF-8。</p>
 * <p>本类仅包含静态方法，不支持实例化。</p>
 *
 * @author ytora
 * @since 1.0-SNAPSHOT
 */
public final class Encodes {

    private Encodes() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 使用 UTF-8 对字符串进行 URL 编码。
     *
     * <p>空格会被编码为 {@code +}，其余非安全字符会按百分号编码输出。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code urlEncode("a b+c") -> "a+b%2Bc"}</p>
     * <p>{@code urlEncode("你好") -> "%E4%BD%A0%E5%A5%BD"}</p>
     *
     * @param value 待编码字符串
     * @return 编码后的 URL 字符串
     */
    public static String urlEncode(String value) {
        return urlEncode(value, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定字符集对字符串进行 URL 编码。
     *
     * <p>当字符集为 {@code null} 时，自动使用 UTF-8。</p>
     * <p>编码规则采用常见表单风格：空格转为 {@code +}，其余非安全字符转为
     * {@code %XX} 形式。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     *
     * @param value 待编码字符串
     * @param charset 编码使用的字符集
     * @return 编码后的 URL 字符串
     */
    public static String urlEncode(String value, Charset charset) {
        if (value == null) {
            return null;
        }
        Charset actualCharset = charset == null ? StandardCharsets.UTF_8 : charset;
        byte[] bytes = value.getBytes(actualCharset);
        StringBuilder builder = new StringBuilder(bytes.length * 3);
        for (byte currentByte : bytes) {
            int current = currentByte & 0xFF;
            if (isUrlSafe(current)) {
                builder.append((char) current);
            } else if (current == ' ') {
                builder.append('+');
            } else {
                builder.append('%');
                appendHex(builder, current);
            }
        }
        return builder.toString();
    }

    /**
     * 使用 UTF-8 对 URL 编码字符串进行解码。
     *
     * <p>会将 {@code +} 还原为空格，并解析百分号编码字节。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code urlDecode("a+b%2Bc") -> "a b+c"}</p>
     * <p>{@code urlDecode("%E4%BD%A0%E5%A5%BD") -> "你好"}</p>
     *
     * @param value 待解码字符串
     * @return 解码后的普通字符串
     */
    public static String urlDecode(String value) {
        return urlDecode(value, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定字符集对 URL 编码字符串进行解码。
     *
     * <p>当字符集为 {@code null} 时，自动使用 UTF-8。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>当遇到不完整的百分号编码、非法十六进制编码，或输入中直接包含非 ASCII 字符时，
     * 会抛出 {@link IllegalArgumentException}。</p>
     *
     * @param value 待解码字符串
     * @param charset 解码使用的字符集
     * @return 解码后的普通字符串
     * @throws IllegalArgumentException 当编码格式非法时抛出
     */
    public static String urlDecode(String value, Charset charset) {
        if (value == null) {
            return null;
        }
        Charset actualCharset = charset == null ? StandardCharsets.UTF_8 : charset;
        ByteArrayOutputStream output = new ByteArrayOutputStream(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '+') {
                output.write(' ');
                continue;
            }
            if (current == '%') {
                if (i + 2 >= value.length()) {
                    throw new IllegalArgumentException("URL 编码格式不完整");
                }
                int high = Character.digit(value.charAt(i + 1), 16);
                int low = Character.digit(value.charAt(i + 2), 16);
                if (high < 0 || low < 0) {
                    throw new IllegalArgumentException("URL 编码包含非法的十六进制字符");
                }
                output.write((high << 4) + low);
                i += 2;
                continue;
            }
            if (current > 0x7F) {
                throw new IllegalArgumentException("URL 编码字符串中不能直接包含非 ASCII 字符");
            }
            output.write((byte) current);
        }
        return new String(output.toByteArray(), actualCharset);
    }

    /**
     * 使用 UTF-8 对字符串进行 Base64 编码。
     *
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code base64Encode("ytora") -> "eXRvcmE="}</p>
     *
     * @param value 待编码字符串
     * @return Base64 编码结果
     */
    public static String base64Encode(String value) {
        if (value == null) {
            return null;
        }
        return base64Encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定字符集对字符串进行 Base64 编码。
     *
     * <p>当字符集为 {@code null} 时，自动使用 UTF-8。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     *
     * @param value 待编码字符串
     * @param charset 编码使用的字符集
     * @return Base64 编码结果
     */
    public static String base64Encode(String value, Charset charset) {
        if (value == null) {
            return null;
        }
        Charset actualCharset = charset == null ? StandardCharsets.UTF_8 : charset;
        return Base64.getEncoder().encodeToString(value.getBytes(actualCharset));
    }

    /**
     * 使用 UTF-8 将 Base64 字符串解码为普通字符串。
     *
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code base64DecodeToString("eXRvcmE=") -> "ytora"}</p>
     *
     * @param value 待解码 Base64 字符串
     * @return 解码后的普通字符串
     */
    public static String base64DecodeToString(String value) {
        return base64DecodeToString(value, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定字符集将 Base64 字符串解码为普通字符串。
     *
     * <p>当字符集为 {@code null} 时，自动使用 UTF-8。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     *
     * @param value 待解码 Base64 字符串
     * @param charset 解码使用的字符集
     * @return 解码后的普通字符串
     */
    public static String base64DecodeToString(String value, Charset charset) {
        if (value == null) {
            return null;
        }
        Charset actualCharset = charset == null ? StandardCharsets.UTF_8 : charset;
        return new String(Base64.getDecoder().decode(value), actualCharset);
    }

    /**
     * 对 HTML 特殊字符进行转义。
     *
     * <p>当前会处理以下字符：{@code & < > " '}。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code htmlEscape("<div>&\"'</div>") -> "&lt;div&gt;&amp;&quot;&#39;&lt;/div&gt;"}</p>
     *
     * @param value 待转义字符串
     * @return 转义后的 HTML 安全文本
     */
    public static String htmlEscape(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            switch (current) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&#39;");
                    break;
                default:
                    builder.append(current);
                    break;
            }
        }
        return builder.toString();
    }

    /**
     * 对 HTML 实体进行反转义。
     *
     * <p>当前支持常见命名实体 {@code amp}、{@code lt}、{@code gt}、
     * {@code quot}、{@code apos}，以及十进制和十六进制数字实体。</p>
     * <p>无法识别的实体会保留原内容。</p>
     * <p>若参数为 {@code null}，直接返回 {@code null}。</p>
     * <p>示例：</p>
     * <p>{@code htmlUnescape("&lt;div&gt;") -> "<div>"}</p>
     * <p>{@code htmlUnescape("&#65;") -> "A"}</p>
     *
     * @param value 待反转义字符串
     * @return 反转义后的普通字符串
     */
    public static String htmlUnescape(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current != '&') {
                builder.append(current);
                continue;
            }
            int end = value.indexOf(';', i + 1);
            if (end < 0) {
                builder.append(current);
                continue;
            }
            String entity = value.substring(i + 1, end);
            Character resolved = resolveHtmlEntity(entity);
            if (resolved == null) {
                builder.append(current);
                continue;
            }
            builder.append(resolved.charValue());
            i = end;
        }
        return builder.toString();
    }

    /**
     * 判断字节值在 URL 编码中是否可直接保留。
     *
     * @param value 待判断字节值
     * @return 当字节属于 URL 安全字符时返回 {@code true}
     */
    private static boolean isUrlSafe(int value) {
        return (value >= 'a' && value <= 'z')
                || (value >= 'A' && value <= 'Z')
                || (value >= '0' && value <= '9')
                || value == '-'
                || value == '_'
                || value == '.'
                || value == '*';
    }

    /**
     * 以两位十六进制形式向构建器追加编码结果。
     *
     * @param builder 目标构建器
     * @param value 待编码字节值
     */
    private static void appendHex(StringBuilder builder, int value) {
        char high = Character.toUpperCase(Character.forDigit((value >> 4) & 0xF, 16));
        char low = Character.toUpperCase(Character.forDigit(value & 0xF, 16));
        builder.append(high).append(low);
    }

    /**
     * 解析单个 HTML 实体。
     *
     * @param entity 不含 {@code &} 与 {@code ;} 的实体内容
     * @return 解析成功时返回对应字符，否则返回 {@code null}
     */
    private static Character resolveHtmlEntity(String entity) {
        if ("amp".equals(entity)) {
            return '&';
        }
        if ("lt".equals(entity)) {
            return '<';
        }
        if ("gt".equals(entity)) {
            return '>';
        }
        if ("quot".equals(entity)) {
            return '"';
        }
        if ("apos".equals(entity) || "#39".equals(entity)) {
            return '\'';
        }
        if (entity.startsWith("#x") || entity.startsWith("#X")) {
            return decodeNumericEntity(entity.substring(2), 16);
        }
        if (entity.startsWith("#")) {
            return decodeNumericEntity(entity.substring(1), 10);
        }
        return null;
    }

    /**
     * 解析数字型 HTML 实体。
     *
     * <p>仅当码点可表示为单个 {@code char} 时才返回结果；超出 BMP 的码点返回
     * {@code null}，由调用方保留原始实体文本。</p>
     *
     * @param value 数字部分文本
     * @param radix 进制
     * @return 解析成功时返回对应字符，否则返回 {@code null}
     */
    private static Character decodeNumericEntity(String value, int radix) {
        try {
            int codePoint = Integer.parseInt(value, radix);
            if (!Character.isValidCodePoint(codePoint) || codePoint > Character.MAX_VALUE) {
                return null;
            }
            return (char) codePoint;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
