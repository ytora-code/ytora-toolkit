package xyz.ytora.toolkit.text.dsl.support;

/**
 * 错误文案格式化工具。
 */
public final class ErrorMessageSupport {

    private ErrorMessageSupport() {
    }

    public static String format(String message, int line, int column, String snippet) {
        StringBuilder builder = new StringBuilder(message);
        if (line > 0) {
            builder.append("，行 ").append(line);
        }
        if (column > 0) {
            builder.append("，列 ").append(column);
        }
        if (snippet != null && !snippet.isEmpty()) {
            builder.append("，附近内容: ").append(sanitize(snippet));
        }
        return builder.toString();
    }

    private static String sanitize(String snippet) {
        return snippet.replace("\r", "\\r").replace("\n", "\\n");
    }
}
