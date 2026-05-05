package xyz.ytora.toolkit.text.dsl.template;

import xyz.ytora.toolkit.text.dsl.exception.ControlFlowUsageException;
import xyz.ytora.toolkit.text.dsl.exception.PlaceholderSyntaxException;
import xyz.ytora.toolkit.text.dsl.exception.TemplateSyntaxException;
import xyz.ytora.toolkit.text.dsl.expression.Expression;
import xyz.ytora.toolkit.text.dsl.expression.ExpressionParser;
import xyz.ytora.toolkit.text.dsl.function.FunctionRegistry;
import xyz.ytora.toolkit.text.dsl.runtime.AccessMode;
import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;
import xyz.ytora.toolkit.text.dsl.runtime.ValueSupport;
import xyz.ytora.toolkit.text.dsl.support.DslReservedNames;
import xyz.ytora.toolkit.text.dsl.support.ErrorMessageSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板解析器。
 *
 * <p>该解析器是面向块结构的轻量实现，关键字语句按行识别，普通文本中的占位符按片段识别。</p>
 */
public final class TemplateParser {

    private static final Pattern IF_PATTERN = Pattern.compile("^if:\\s*(.+)\\s*\\{$");
    private static final Pattern FOR_PATTERN = Pattern.compile("^for:\\s*(.+?)(?:\\s+where\\s+(.+))?\\s*\\{$");
    private static final Pattern WHEN_PATTERN = Pattern.compile("^when:\\s*(.+)\\s*\\{$");
    private static final Pattern CASE_PATTERN = Pattern.compile("^case:\\s*(.+)\\s*\\{$");
    private static final Pattern SET_PATTERN = Pattern.compile("^set:\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+)$");
    private static final Pattern RAW_PATTERN = Pattern.compile("^raw:\\s*\\{$");
    private static final Pattern ELSE_PATTERN = Pattern.compile("^else\\s*\\{$");
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("^default:\\s*\\{$");

    private final FunctionRegistry functionRegistry;
    private final AccessMode accessMode;

    public TemplateParser(FunctionRegistry functionRegistry, AccessMode accessMode) {
        this.functionRegistry = functionRegistry;
        this.accessMode = accessMode;
    }

    public TemplateNode parse(String template) {
        String normalized = preprocess(template == null ? "" : template);
        Cursor cursor = new Cursor(splitKeepingLines(normalized));
        return parseBlock(cursor, false, false, "root", 1, 1, normalized).node;
    }

    private String preprocess(String template) {
        return template.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("(?m)^([ \\t]*)}\\s*else\\s*\\{$", "$1}\n$1else {");
    }

    private List<String> splitKeepingLines(String template) {
        List<String> lines = new ArrayList<String>();
        int start = 0;
        for (int i = 0; i < template.length(); i++) {
            if (template.charAt(i) == '\n') {
                lines.add(template.substring(start, i + 1));
                start = i + 1;
            }
        }
        if (start < template.length()) {
            lines.add(template.substring(start));
        }
        if (template.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private ParseBlockResult parseBlock(Cursor cursor, boolean stopAtRightBrace, boolean insideLoop,
                                        String blockName, int blockStartLine, int blockStartColumn, String blockLineText) {
        List<TemplateNode> nodes = new ArrayList<TemplateNode>();
        while (cursor.hasNext()) {
            String line = cursor.peek();
            String trimmed = line.trim();
            if (stopAtRightBrace && "}".equals(trimmed)) {
                cursor.next();
                return new ParseBlockResult(new SequenceNode(nodes));
            }
            if (trimmed.isEmpty()) {
                int lineNumber = cursor.currentLineNumber();
                nodes.add(parseInlineText(cursor.next(), lineNumber));
                continue;
            }

            Matcher matcher = IF_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                int lineNumber = cursor.currentLineNumber();
                int keywordColumn = findKeywordColumn(line, "if:");
                int expressionColumn = line.indexOf(matcher.group(1), keywordColumn - 1) + 1;
                cursor.next();
                Expression condition = ExpressionParser.parse(matcher.group(1), lineNumber, expressionColumn, line);
                TemplateNode ifBody = parseBlock(cursor, true, insideLoop, "if", lineNumber, keywordColumn, line).node;
                TemplateNode elseBody = EmptyNode.INSTANCE;
                if (cursor.hasNext() && ELSE_PATTERN.matcher(cursor.peek().trim()).matches()) {
                    String elseLineText = cursor.peek();
                    int elseLine = cursor.currentLineNumber();
                    int elseColumn = firstNonWhitespaceColumn(elseLineText);
                    cursor.next();
                    elseBody = parseBlock(cursor, true, insideLoop, "else", elseLine, elseColumn, elseLineText).node;
                }
                nodes.add(new IfNode(condition, ifBody, elseBody));
                continue;
            }

            matcher = FOR_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                int lineNumber = cursor.currentLineNumber();
                int keywordColumn = findKeywordColumn(line, "for:");
                int sequenceColumn = line.indexOf(matcher.group(1), keywordColumn - 1) + 1;
                cursor.next();
                Expression sequence = ExpressionParser.parse(matcher.group(1), lineNumber, sequenceColumn, line);
                Expression predicate = matcher.group(2) == null ? null
                        : ExpressionParser.parse(matcher.group(2), lineNumber,
                        line.indexOf(matcher.group(2), keywordColumn - 1) + 1, line);
                TemplateNode body = parseBlock(cursor, true, true, "for", lineNumber, keywordColumn, line).node;
                nodes.add(new ForNode(sequence, predicate, body));
                continue;
            }

            matcher = WHEN_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                int lineNumber = cursor.currentLineNumber();
                int keywordColumn = findKeywordColumn(line, "when:");
                int expressionColumn = line.indexOf(matcher.group(1), keywordColumn - 1) + 1;
                cursor.next();
                Expression target = ExpressionParser.parse(matcher.group(1), lineNumber, expressionColumn, line);
                nodes.add(parseWhen(cursor, target, insideLoop, lineNumber, keywordColumn, line));
                continue;
            }

            matcher = SET_PATTERN.matcher(trimmed);
            if (matcher.matches()) {
                int lineNumber = cursor.currentLineNumber();
                int nameColumn = line.indexOf(matcher.group(1)) + 1;
                int expressionColumn = line.indexOf(matcher.group(2), nameColumn - 1) + 1;
                cursor.next();
                validateVariableName(matcher.group(1), lineNumber, nameColumn, line);
                nodes.add(new SetNode(matcher.group(1),
                        ExpressionParser.parse(matcher.group(2), lineNumber, expressionColumn, line)));
                continue;
            }

            if (RAW_PATTERN.matcher(trimmed).matches()) {
                int lineNumber = cursor.currentLineNumber();
                int rawColumn = firstNonWhitespaceColumn(line);
                cursor.next();
                nodes.add(parseRawBlock(cursor, lineNumber, rawColumn, line));
                continue;
            }

            if ("continue".equals(trimmed)) {
                if (!insideLoop) {
                    throw new ControlFlowUsageException(format("continue 只能在 for 块内部使用",
                            cursor.currentLineNumber(), firstNonWhitespaceColumn(line), line));
                }
                cursor.next();
                nodes.add(ContinueNode.INSTANCE);
                continue;
            }

            if ("break".equals(trimmed)) {
                if (!insideLoop) {
                    throw new ControlFlowUsageException(format("break 只能在 for 块内部使用",
                            cursor.currentLineNumber(), firstNonWhitespaceColumn(line), line));
                }
                cursor.next();
                nodes.add(BreakNode.INSTANCE);
                continue;
            }

            if (ELSE_PATTERN.matcher(trimmed).matches() || CASE_PATTERN.matcher(trimmed).matches() || DEFAULT_PATTERN.matcher(trimmed).matches()) {
                throw new TemplateSyntaxException(format("出现了位置不合法的关键字行: " + trimmed,
                        cursor.currentLineNumber(), firstNonWhitespaceColumn(line), line));
            }

            int lineNumber = cursor.currentLineNumber();
            nodes.add(parseInlineText(cursor.next(), lineNumber));
        }

        if (stopAtRightBrace) {
            throw new TemplateSyntaxException(format(blockName + " 块缺少结束符 '}'", blockStartLine, blockStartColumn, blockLineText));
        }
        return new ParseBlockResult(new SequenceNode(nodes));
    }

    private TemplateNode parseWhen(Cursor cursor, Expression target, boolean insideLoop,
                                   int whenLine, int whenColumn, String whenLineText) {
        List<WhenBranch> cases = new ArrayList<WhenBranch>();
        TemplateNode defaultBody = EmptyNode.INSTANCE;
        while (cursor.hasNext()) {
            String line = cursor.peek();
            String trimmed = line.trim();
            if ("}".equals(trimmed)) {
                cursor.next();
                return new WhenNode(target, cases, defaultBody);
            }
            Matcher caseMatcher = CASE_PATTERN.matcher(trimmed);
            if (caseMatcher.matches()) {
                int lineNumber = cursor.currentLineNumber();
                int caseColumn = findKeywordColumn(line, "case:");
                int expressionColumn = line.indexOf(caseMatcher.group(1), caseColumn - 1) + 1;
                cursor.next();
                TemplateNode body = parseBlock(cursor, true, insideLoop, "case", lineNumber, caseColumn, line).node;
                cases.add(new WhenBranch(ExpressionParser.parse(caseMatcher.group(1), lineNumber, expressionColumn, line), body));
                continue;
            }
            if (DEFAULT_PATTERN.matcher(trimmed).matches()) {
                int lineNumber = cursor.currentLineNumber();
                int defaultColumn = firstNonWhitespaceColumn(line);
                cursor.next();
                defaultBody = parseBlock(cursor, true, insideLoop, "default", lineNumber, defaultColumn, line).node;
                continue;
            }
            throw new TemplateSyntaxException(format("when 块内部只允许 case/default，实际得到: " + trimmed,
                    cursor.currentLineNumber(), firstNonWhitespaceColumn(line), line));
        }
        throw new TemplateSyntaxException(format("when 块缺少结束符 '}'", whenLine, whenColumn, whenLineText));
    }

    private TemplateNode parseRawBlock(Cursor cursor, int rawLine, int rawColumn, String rawLineText) {
        StringBuilder builder = new StringBuilder();
        while (cursor.hasNext()) {
            String line = cursor.next();
            if ("}".equals(line.trim())) {
                return new TextNode(builder.toString());
            }
            builder.append(line);
        }
        throw new TemplateSyntaxException(format("raw 块缺少结束符 '}'", rawLine, rawColumn, rawLineText));
    }

    private TemplateNode parseInlineText(String text, int lineNumber) {
        List<TemplateNode> nodes = new ArrayList<TemplateNode>();
        int cursor = 0;
        while (cursor < text.length()) {
            int start = text.indexOf("#{", cursor);
            if (start < 0) {
                nodes.add(new TextNode(text.substring(cursor)));
                break;
            }
            if (start > cursor) {
                nodes.add(new TextNode(text.substring(cursor, start)));
            }
            int end = findPlaceholderEnd(text, start + 2);
            if (end < 0) {
                throw new PlaceholderSyntaxException(format("占位符没有正确结束", lineNumber, start + 1, text));
            }
            String expressionText = text.substring(start + 2, end).trim();
            if (expressionText.isEmpty()) {
                throw new PlaceholderSyntaxException(format("不允许出现空占位符", lineNumber, start + 1, text));
            }
            nodes.add(new PlaceholderNode(ExpressionParser.parse(expressionText, lineNumber, start + 3, text)));
            cursor = end + 1;
        }
        return nodes.size() == 1 ? nodes.get(0) : new SequenceNode(nodes);
    }

    private int findPlaceholderEnd(String text, int startIndex) {
        int nestedDepth = 0;
        boolean inString = false;
        char stringQuote = 0;
        for (int i = startIndex; i < text.length(); i++) {
            char current = text.charAt(i);
            if (inString) {
                if (current == '\\') {
                    i++;
                    continue;
                }
                if (current == stringQuote) {
                    inString = false;
                }
                continue;
            }
            if (current == '\'' || current == '"') {
                inString = true;
                stringQuote = current;
                continue;
            }
            if (current == '{' || current == '[' || current == '(') {
                nestedDepth++;
                continue;
            }
            if (current == '}' && nestedDepth == 0) {
                return i;
            }
            if (current == '}' || current == ']' || current == ')') {
                nestedDepth--;
            }
        }
        return -1;
    }

    private void validateVariableName(String name, int lineNumber, int column, String lineText) {
        if (DslReservedNames.isReserved(name)) {
            throw new TemplateSyntaxException(format("变量名属于保留字，禁止使用: " + name, lineNumber, column, lineText));
        }
    }

    private int findKeywordColumn(String line, String keyword) {
        int index = line.indexOf(keyword);
        return index < 0 ? firstNonWhitespaceColumn(line) : index + 1;
    }

    private int firstNonWhitespaceColumn(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return i + 1;
            }
        }
        return 1;
    }

    private String format(String message, int line, int column, String snippet) {
        return ErrorMessageSupport.format(message, line, column, snippet);
    }

    private static final class ParseBlockResult {
        private final TemplateNode node;

        private ParseBlockResult(TemplateNode node) {
            this.node = node;
        }
    }

    private static final class Cursor {
        private final List<String> lines;
        private int index;

        private Cursor(List<String> lines) {
            this.lines = lines;
        }

        private boolean hasNext() {
            return index < lines.size();
        }

        private String next() {
            return lines.get(index++);
        }

        private String peek() {
            return lines.get(index);
        }

        private int currentLineNumber() {
            return index + 1;
        }
    }

    private static final class SequenceNode implements TemplateNode {
        private final List<TemplateNode> nodes;

        private SequenceNode(List<TemplateNode> nodes) {
            this.nodes = nodes;
        }

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            for (TemplateNode node : nodes) {
                node.render(context, output);
            }
        }
    }

    private static final class EmptyNode implements TemplateNode {
        private static final EmptyNode INSTANCE = new EmptyNode();

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
        }
    }

    private static final class TextNode implements TemplateNode {
        private final String text;

        private TextNode(String text) {
            this.text = text;
        }

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            output.append(text);
        }
    }

    private static final class PlaceholderNode implements TemplateNode {
        private final Expression expression;

        private PlaceholderNode(Expression expression) {
            this.expression = expression;
        }

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            output.append(ValueSupport.stringifyRenderValue(expression.evaluate(context)));
        }
    }

    private static final class IfNode implements TemplateNode {
        private final Expression condition;
        private final TemplateNode ifBody;
        private final TemplateNode elseBody;

        private IfNode(Expression condition, TemplateNode ifBody, TemplateNode elseBody) {
            this.condition = condition;
            this.ifBody = ifBody;
            this.elseBody = elseBody;
        }

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            EvaluationContext child = context.childScope();
            if (ValueSupport.isTruthy(condition.evaluate(child))) {
                ifBody.render(child, output);
            } else {
                elseBody.render(child, output);
            }
        }
    }

    private static final class ForNode implements TemplateNode {
        private final Expression source;
        private final Expression predicate;
        private final TemplateNode body;

        private ForNode(Expression source, Expression predicate, TemplateNode body) {
            this.source = source;
            this.predicate = predicate;
            this.body = body;
        }

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            List<Object> values = ValueSupport.requireSequence(source.evaluate(context), "for 的数据源必须是数组");
            for (int i = 0; i < values.size(); i++) {
                EvaluationContext loopScope = context.loopScope(i, values.get(i));
                if (predicate != null && !ValueSupport.isTruthy(predicate.evaluate(loopScope))) {
                    continue;
                }
                try {
                    body.render(loopScope, output);
                } catch (ContinueSignal ignored) {
                    continue;
                } catch (BreakSignal ignored) {
                    break;
                }
            }
        }
    }

    private static final class WhenNode implements TemplateNode {
        private final Expression target;
        private final List<WhenBranch> branches;
        private final TemplateNode defaultBody;

        private WhenNode(Expression target, List<WhenBranch> branches, TemplateNode defaultBody) {
            this.target = target;
            this.branches = branches;
            this.defaultBody = defaultBody;
        }

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            Object targetValue = target.evaluate(context);
            EvaluationContext child = context.childScope();
            for (WhenBranch branch : branches) {
                if (ValueSupport.equalsValue(targetValue, branch.condition.evaluate(child))) {
                    branch.body.render(child, output);
                    return;
                }
            }
            defaultBody.render(child, output);
        }
    }

    private static final class WhenBranch {
        private final Expression condition;
        private final TemplateNode body;

        private WhenBranch(Expression condition, TemplateNode body) {
            this.condition = condition;
            this.body = body;
        }
    }

    private static final class SetNode implements TemplateNode {
        private final String name;
        private final Expression expression;

        private SetNode(String name, Expression expression) {
            this.name = name;
            this.expression = expression;
        }

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            context.setLocal(name, expression.evaluate(context));
        }
    }

    private static final class ContinueNode implements TemplateNode {
        private static final ContinueNode INSTANCE = new ContinueNode();

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            context.ensureInsideLoop("continue");
            throw ContinueSignal.INSTANCE;
        }
    }

    private static final class BreakNode implements TemplateNode {
        private static final BreakNode INSTANCE = new BreakNode();

        @Override
        public void render(EvaluationContext context, StringBuilder output) {
            context.ensureInsideLoop("break");
            throw BreakSignal.INSTANCE;
        }
    }

    private static final class BreakSignal extends RuntimeException {
        private static final long serialVersionUID = 114514;
        private static final BreakSignal INSTANCE = new BreakSignal();
    }

    private static final class ContinueSignal extends RuntimeException {
        private static final long serialVersionUID = 114514;
        private static final ContinueSignal INSTANCE = new ContinueSignal();
    }
}
