package xyz.ytora.toolkit.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 打印工具类
 *
 * <p>提供了一系列打印相关的 API，将输入的字符串以美观、整体的方式进行打印。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class Printers {

    private static final int DEFAULT_PADDING = 1;

    private Printers() {
        throw new AssertionError("不允许实例化工具类");
    }


    /**
     * 使用默认风格打印文本。
     *
     * @param text 要打印的文本
     */
    public static void print(String text) {
        print(text, PrintStyle.BOX_ASCII);
    }

    /**
     * 按指定风格打印文本。
     *
     * @param text 要打印的文本
     * @param style 打印风格
     */
    public static void print(String text, PrintStyle style) {
        if (Strs.isEmpty(text)) {
            System.out.println("(empty)");
            return;
        }
        Objects.requireNonNull(style, "style 不能为空");

        List<String> lines = normalizeLines(text);
        int maxWidth = calcMaxWidth(lines);
        int lineNoWidth = style.showLineNumber() ? String.valueOf(lines.size()).length() + 2 : 0;
        int contentWidth = maxWidth + DEFAULT_PADDING * 2 + lineNoWidth;

        render(lines, style, contentWidth, DEFAULT_PADDING);
    }

    /**
     * 返回格式化后的文本，而不是直接打印。
     *
     * @param text 要格式化的文本
     * @param style 打印风格
     * @return 格式化后的完整字符串
     */
    public static String format(String text, PrintStyle style) {
        if (Strs.isEmpty(text)) {
            return "(empty)";
        }
        Objects.requireNonNull(style, "style 不能为空");

        List<String> lines = normalizeLines(text);
        int maxWidth = calcMaxWidth(lines);
        int lineNoWidth = style.showLineNumber() ? String.valueOf(lines.size()).length() + 2 : 0;
        int contentWidth = maxWidth + DEFAULT_PADDING * 2 + lineNoWidth;

        StringBuilder sb = new StringBuilder(256);
        appendRendered(sb, lines, style, contentWidth, DEFAULT_PADDING);
        return sb.toString();
    }

    private static void render(List<String> lines, PrintStyle style, int contentWidth, int padding) {
        StringBuilder sb = new StringBuilder(256);
        appendRendered(sb, lines, style, contentWidth, padding);
        System.out.print(sb);
    }

    private static void appendRendered(StringBuilder sb,
                                       List<String> lines,
                                       PrintStyle style,
                                       int contentWidth,
                                       int padding) {
        Border border = style.border();

        if (style.hasTopBorder()) {
            appendBorderLine(sb, border.topLeft(), border.horizontal(), border.topRight(), contentWidth);
        }

        for (int i = 0; i < lines.size(); i++) {
            appendContentLine(sb, lines.get(i), i + 1, lines.size(), style, border, contentWidth, padding);
        }

        if (style.hasBottomBorder()) {
            appendBorderLine(sb, border.bottomLeft(), border.horizontal(), border.bottomRight(), contentWidth);
        }
    }

    private static void appendBorderLine(StringBuilder sb, char left, char horizontal, char right, int width) {
        sb.append(left);
        for (int i = 0; i < width; i++) {
            sb.append(horizontal);
        }
        sb.append(right).append(System.lineSeparator());
    }

    private static void appendContentLine(StringBuilder sb,
                                          String line,
                                          int lineNo,
                                          int totalLines,
                                          PrintStyle style,
                                          Border border,
                                          int width,
                                          int padding) {
        sb.append(border.vertical());

        for (int i = 0; i < padding; i++) {
            sb.append(' ');
        }

        int occupied = padding;

        if (style.showLineNumber()) {
            String no = String.format("%" + String.valueOf(totalLines).length() + "d| ", lineNo);
            sb.append(no);
            occupied += no.length();
        }

        sb.append(line);
        occupied += visualLength(line);

        while (occupied < width) {
            sb.append(' ');
            occupied++;
        }

        sb.append(border.vertical()).append(System.lineSeparator());
    }

    private static List<String> normalizeLines(String text) {
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        String[] rawLines = normalized.split("\n", -1);

        int minIndent = Integer.MAX_VALUE;
        for (String line : rawLines) {
            if (isBlank(line)) {
                continue;
            }
            int indent = countLeadingSpaces(line);
            if (indent < minIndent) {
                minIndent = indent;
            }
        }

        if (minIndent == Integer.MAX_VALUE) {
            minIndent = 0;
        }

        List<String> result = new ArrayList<>(rawLines.length);
        for (String line : rawLines) {
            if (line.length() >= minIndent) {
                result.add(line.substring(minIndent));
            } else {
                result.add(line);
            }
        }
        return result;
    }

    private static int calcMaxWidth(List<String> lines) {
        int maxWidth = 0;
        for (String line : lines) {
            int len = visualLength(line);
            if (len > maxWidth) {
                maxWidth = len;
            }
        }
        return maxWidth;
    }

    private static int countLeadingSpaces(String str) {
        int count = 0;
        while (count < str.length() && str.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 计算字符串显示宽度。
     *
     * <p>简单规则：ASCII 按 1 宽度，非 ASCII 字符按 2 宽度估算。
     * 对中文场景会比直接 length() 更接近实际显示效果。</p>
     *
     * @param str 字符串
     * @return 显示宽度
     */
    private static int visualLength(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        int len = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            len += ch <= 0x00FF ? 1 : 2;
        }
        return len;
    }

    /**
     * 打印风格枚举。
     */
    public enum PrintStyle {
        /**
         * ASCII 单线方框。
         */
        BOX_ASCII(new Border('+', '+', '+', '+', '-', '|'), true, true, false),

        /**
         * 粗线方框。
         */
        BOX_HEAVY(new Border('┏', '┓', '┗', '┛', '━', '┃'), true, true, false),

        /**
         * 双线方框。
         */
        DOUBLE_LINE(new Border('╔', '╗', '╚', '╝', '═', '║'), true, true, false),

        /**
         * 轻量边框。
         */
        MINIMAL(new Border('-', '-', '-', '-', '-', '|'), true, true, false),

        /**
         * 带行号的方框。
         */
        NUMBERED(new Border('┌', '┐', '└', '┘', '─', '│'), true, true, true),

        /**
         * 注释块风格，适合日志中嵌入。
         */
        COMMENT_BLOCK(new Border('/', '\\', '\\', '/', '*', '*'), true, true, false);

        private final Border border;
        private final boolean hasTopBorder;
        private final boolean hasBottomBorder;
        private final boolean showLineNumber;

        PrintStyle(Border border, boolean hasTopBorder, boolean hasBottomBorder, boolean showLineNumber) {
            this.border = border;
            this.hasTopBorder = hasTopBorder;
            this.hasBottomBorder = hasBottomBorder;
            this.showLineNumber = showLineNumber;
        }

        Border border() {
            return border;
        }

        boolean hasTopBorder() {
            return hasTopBorder;
        }

        boolean hasBottomBorder() {
            return hasBottomBorder;
        }

        boolean showLineNumber() {
            return showLineNumber;
        }
    }

    /**
     * 边框字符定义。
     */
    private static class Border {
        char topLeft;
        char topRight;
        char bottomLeft;
        char bottomRight;
        char horizontal;
        char vertical;

        public Border(char topLeft, char topRight, char bottomLeft, char bottomRight, char horizontal, char vertical) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.bottomLeft = bottomLeft;
            this.bottomRight = bottomRight;
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public char topLeft() {
            return topLeft;
        }

        public char topRight() {
            return topRight;
        }

        public char bottomLeft() {
            return bottomLeft;
        }

        public char bottomRight() {
            return bottomRight;
        }

        public char horizontal() {
            return horizontal;
        }

        public char vertical() {
            return vertical;
        }
    }


}