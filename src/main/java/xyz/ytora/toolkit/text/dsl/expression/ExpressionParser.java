package xyz.ytora.toolkit.text.dsl.expression;

import xyz.ytora.toolkit.text.dsl.exception.DslEvaluationException;
import xyz.ytora.toolkit.text.dsl.exception.ExpressionSyntaxException;
import xyz.ytora.toolkit.text.dsl.exception.TypeMismatchException;
import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;
import xyz.ytora.toolkit.text.dsl.runtime.ValueSupport;
import xyz.ytora.toolkit.text.dsl.support.ErrorMessageSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 表达式解析器。
 *
 * <p>模板语法和表达式语法解耦，模板节点只依赖这个解析器产出的表达式对象。</p>
 */
public final class ExpressionParser {

    private final String source;
    private final List<Token> tokens;
    private final int originLine;
    private final int originColumn;
    private final String originSnippet;
    private int index;

    private ExpressionParser(String source, int originLine, int originColumn, String originSnippet) {
        this.source = source == null ? "" : source;
        this.originLine = originLine;
        this.originColumn = originColumn;
        this.originSnippet = originSnippet == null ? this.source : originSnippet;
        this.tokens = new Tokenizer().tokenize();
    }

    public static Expression parse(String source) {
        return parse(source, 1, 1, source);
    }

    public static Expression parse(String source, int originLine, int originColumn, String originSnippet) {
        ExpressionParser parser = new ExpressionParser(source, originLine, originColumn, originSnippet);
        Expression expression = parser.parseTernary();
        parser.expect(TokenType.EOF, "表达式后存在多余内容");
        return expression;
    }

    private Expression parseTernary() {
        Expression condition = parseOr();
        if (match(TokenType.QUESTION)) {
            Expression whenTrue = parseTernary();
            expect(TokenType.COLON, "三元表达式缺少 ':'");
            Expression whenFalse = parseTernary();
            return new TernaryExpression(condition, whenTrue, whenFalse);
        }
        return condition;
    }

    private Expression parseOr() {
        Expression expression = parseAnd();
        while (match(TokenType.OR_OR)) {
            expression = new BinaryExpression("||", expression, parseAnd());
        }
        return expression;
    }

    private Expression parseAnd() {
        Expression expression = parseEquality();
        while (match(TokenType.AND_AND)) {
            expression = new BinaryExpression("&&", expression, parseEquality());
        }
        return expression;
    }

    private Expression parseEquality() {
        Expression expression = parseComparison();
        while (true) {
            if (match(TokenType.EQ_EQ)) {
                expression = new BinaryExpression("==", expression, parseComparison());
            } else if (match(TokenType.NOT_EQ)) {
                expression = new BinaryExpression("!=", expression, parseComparison());
            } else {
                return expression;
            }
        }
    }

    private Expression parseComparison() {
        Expression expression = parseBitOr();
        while (true) {
            if (match(TokenType.GT)) {
                expression = new BinaryExpression(">", expression, parseBitOr());
            } else if (match(TokenType.GTE)) {
                expression = new BinaryExpression(">=", expression, parseBitOr());
            } else if (match(TokenType.LT)) {
                expression = new BinaryExpression("<", expression, parseBitOr());
            } else if (match(TokenType.LTE)) {
                expression = new BinaryExpression("<=", expression, parseBitOr());
            } else {
                return expression;
            }
        }
    }

    private Expression parseBitOr() {
        Expression expression = parseBitAnd();
        while (match(TokenType.BIT_OR)) {
            expression = new BinaryExpression("|", expression, parseBitAnd());
        }
        return expression;
    }

    private Expression parseBitAnd() {
        Expression expression = parseShift();
        while (match(TokenType.BIT_AND)) {
            expression = new BinaryExpression("&", expression, parseShift());
        }
        return expression;
    }

    private Expression parseShift() {
        Expression expression = parseJoinRepeat();
        while (true) {
            if (match(TokenType.SHIFT_LEFT)) {
                expression = new BinaryExpression("<<", expression, parseJoinRepeat());
            } else if (match(TokenType.SHIFT_RIGHT)) {
                expression = new BinaryExpression(">>", expression, parseJoinRepeat());
            } else {
                return expression;
            }
        }
    }

    private Expression parseJoinRepeat() {
        Expression expression = parseAdditive();
        while (true) {
            if (matchIdentifierKeyword("join")) {
                expression = new BinaryExpression("join", expression, parseAdditive());
            } else if (matchIdentifierKeyword("repeat")) {
                expression = new BinaryExpression("repeat", expression, parseAdditive());
            } else {
                return expression;
            }
        }
    }

    private Expression parseAdditive() {
        Expression expression = parseMultiplicative();
        while (true) {
            if (match(TokenType.PLUS)) {
                expression = new BinaryExpression("+", expression, parseMultiplicative());
            } else if (match(TokenType.MINUS)) {
                expression = new BinaryExpression("-", expression, parseMultiplicative());
            } else {
                return expression;
            }
        }
    }

    private Expression parseMultiplicative() {
        Expression expression = parseUnary();
        while (true) {
            if (match(TokenType.STAR)) {
                expression = new BinaryExpression("*", expression, parseUnary());
            } else if (match(TokenType.SLASH)) {
                expression = new BinaryExpression("/", expression, parseUnary());
            } else if (match(TokenType.PERCENT)) {
                expression = new BinaryExpression("%", expression, parseUnary());
            } else {
                return expression;
            }
        }
    }

    private Expression parseUnary() {
        if (match(TokenType.BANG)) {
            return new UnaryExpression("!", parseUnary());
        }
        if (match(TokenType.MINUS)) {
            return new UnaryExpression("-", parseUnary());
        }
        return parsePostfix();
    }

    private Expression parsePostfix() {
        Expression expression = parsePrimary();
        while (true) {
            if (match(TokenType.DOT)) {
                Token member = advance();
                if (member.type != TokenType.IDENTIFIER && member.type != TokenType.NUMBER) {
                    throw error("'.' 后必须是属性名或数字下标", member.start);
                }
                expression = new AccessExpression(expression, new LiteralExpression(member.literalValue()));
            } else if (match(TokenType.LEFT_BRACKET)) {
                Expression key = parseTernary();
                expect(TokenType.RIGHT_BRACKET, "缺少 ']'");
                expression = new AccessExpression(expression, key);
            } else if (match(TokenType.LEFT_PAREN)) {
                if (!(expression instanceof VariableExpression)) {
                    throw error("只有函数名才能被调用", currentStart());
                }
                List<Expression> args = new ArrayList<Expression>();
                if (!check(TokenType.RIGHT_PAREN)) {
                    do {
                        args.add(parseTernary());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RIGHT_PAREN, "缺少 ')'");
                expression = new FunctionCallExpression(((VariableExpression) expression).name, args);
            } else {
                return expression;
            }
        }
    }

    private Expression parsePrimary() {
        Token token = advance();
        switch (token.type) {
            case STRING:
            case NUMBER:
                return new LiteralExpression(token.literalValue());
            case TRUE:
                return new LiteralExpression(Boolean.TRUE);
            case FALSE:
                return new LiteralExpression(Boolean.FALSE);
            case NULL:
                return new LiteralExpression(null);
            case IDENTIFIER:
                return new VariableExpression(token.text);
            case LEFT_PAREN:
                Expression expression = parseTernary();
                expect(TokenType.RIGHT_PAREN, "缺少 ')'");
                return expression;
            case LEFT_BRACKET:
                List<Expression> items = new ArrayList<Expression>();
                if (!check(TokenType.RIGHT_BRACKET)) {
                    do {
                        items.add(parseTernary());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RIGHT_BRACKET, "缺少 ']'");
                return new ArrayExpression(items);
            case LEFT_BRACE:
                Map<String, Expression> entries = new LinkedHashMap<String, Expression>();
                if (!check(TokenType.RIGHT_BRACE)) {
                    do {
                        Token keyToken = advance();
                        if (keyToken.type != TokenType.STRING) {
                            throw error("对象字面量的键必须是字符串", keyToken.start);
                        }
                        expect(TokenType.COLON, "对象字面量的键后缺少 ':'");
                        entries.put((String) keyToken.literalValue(), parseTernary());
                    } while (match(TokenType.COMMA));
                }
                expect(TokenType.RIGHT_BRACE, "缺少 '}'");
                return new ObjectExpression(entries);
            default:
                throw error("无法识别的标记: " + token.text, token.start);
        }
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            index++;
            return true;
        }
        return false;
    }

    private boolean matchIdentifierKeyword(String keyword) {
        if (check(TokenType.IDENTIFIER) && keyword.equals(peek().text)) {
            index++;
            return true;
        }
        return false;
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private Token advance() {
        return tokens.get(index++);
    }

    private Token peek() {
        return tokens.get(index);
    }

    private void expect(TokenType type, String message) {
        if (!match(type)) {
            throw error(message, currentStart());
        }
    }

    private ExpressionSyntaxException error(String message, int offset) {
        return new ExpressionSyntaxException(formatMessage(message, offset));
    }

    private String formatMessage(String message, int offset) {
        return ErrorMessageSupport.format(message, originLine, originColumn + Math.max(offset, 0), originSnippet);
    }

    private int currentStart() {
        return peek().start;
    }

    private enum TokenType {
        IDENTIFIER, STRING, NUMBER, TRUE, FALSE, NULL,
        LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET, LEFT_BRACE, RIGHT_BRACE,
        COMMA, DOT, COLON, QUESTION,
        PLUS, MINUS, STAR, SLASH, PERCENT, BANG,
        GT, GTE, LT, LTE, EQ_EQ, NOT_EQ, AND_AND, OR_OR, BIT_AND, BIT_OR, SHIFT_LEFT, SHIFT_RIGHT,
        EOF
    }

    private static final class Token {
        private final TokenType type;
        private final String text;
        private final Object literal;
        private final int start;

        private Token(TokenType type, String text, Object literal, int start) {
            this.type = type;
            this.text = text;
            this.literal = literal;
            this.start = start;
        }

        private Object literalValue() {
            return literal;
        }
    }

    private final class Tokenizer {

        private final List<Token> parsedTokens = new ArrayList<Token>();
        private int cursor;

        private List<Token> tokenize() {
            while (!isAtEnd()) {
                char c = source.charAt(cursor);
                if (Character.isWhitespace(c)) {
                    cursor++;
                } else if (isIdentifierStart(c)) {
                    readIdentifier();
                } else if (Character.isDigit(c)) {
                    readNumber();
                } else {
                    readSymbol(c);
                }
            }
            parsedTokens.add(new Token(TokenType.EOF, "<eof>", null, source.length()));
            return parsedTokens;
        }

        private void readSymbol(char c) {
            int start = cursor;
            switch (c) {
                case '\'':
                case '"':
                    readString(c);
                    break;
                case '(':
                    parsedTokens.add(new Token(TokenType.LEFT_PAREN, "(", null, start));
                    cursor++;
                    break;
                case ')':
                    parsedTokens.add(new Token(TokenType.RIGHT_PAREN, ")", null, start));
                    cursor++;
                    break;
                case '[':
                    parsedTokens.add(new Token(TokenType.LEFT_BRACKET, "[", null, start));
                    cursor++;
                    break;
                case ']':
                    parsedTokens.add(new Token(TokenType.RIGHT_BRACKET, "]", null, start));
                    cursor++;
                    break;
                case '{':
                    parsedTokens.add(new Token(TokenType.LEFT_BRACE, "{", null, start));
                    cursor++;
                    break;
                case '}':
                    parsedTokens.add(new Token(TokenType.RIGHT_BRACE, "}", null, start));
                    cursor++;
                    break;
                case ',':
                    parsedTokens.add(new Token(TokenType.COMMA, ",", null, start));
                    cursor++;
                    break;
                case '.':
                    parsedTokens.add(new Token(TokenType.DOT, ".", null, start));
                    cursor++;
                    break;
                case ':':
                    parsedTokens.add(new Token(TokenType.COLON, ":", null, start));
                    cursor++;
                    break;
                case '?':
                    parsedTokens.add(new Token(TokenType.QUESTION, "?", null, start));
                    cursor++;
                    break;
                case '+':
                    parsedTokens.add(new Token(TokenType.PLUS, "+", null, start));
                    cursor++;
                    break;
                case '-':
                    parsedTokens.add(new Token(TokenType.MINUS, "-", null, start));
                    cursor++;
                    break;
                case '*':
                    parsedTokens.add(new Token(TokenType.STAR, "*", null, start));
                    cursor++;
                    break;
                case '/':
                    parsedTokens.add(new Token(TokenType.SLASH, "/", null, start));
                    cursor++;
                    break;
                case '%':
                    parsedTokens.add(new Token(TokenType.PERCENT, "%", null, start));
                    cursor++;
                    break;
                case '!':
                    if (match('=')) {
                        parsedTokens.add(new Token(TokenType.NOT_EQ, "!=", null, start));
                    } else {
                        parsedTokens.add(new Token(TokenType.BANG, "!", null, start));
                        cursor++;
                    }
                    break;
                case '=':
                    if (match('=')) {
                        parsedTokens.add(new Token(TokenType.EQ_EQ, "==", null, start));
                        break;
                    }
                    throw syntaxError("出现了非法的 '='", start);
                case '>':
                    if (match('>')) {
                        parsedTokens.add(new Token(TokenType.SHIFT_RIGHT, ">>", null, start));
                    } else if (match('=')) {
                        parsedTokens.add(new Token(TokenType.GTE, ">=", null, start));
                    } else {
                        parsedTokens.add(new Token(TokenType.GT, ">", null, start));
                        cursor++;
                    }
                    break;
                case '<':
                    if (match('<')) {
                        parsedTokens.add(new Token(TokenType.SHIFT_LEFT, "<<", null, start));
                    } else if (match('=')) {
                        parsedTokens.add(new Token(TokenType.LTE, "<=", null, start));
                    } else {
                        parsedTokens.add(new Token(TokenType.LT, "<", null, start));
                        cursor++;
                    }
                    break;
                case '&':
                    if (match('&')) {
                        parsedTokens.add(new Token(TokenType.AND_AND, "&&", null, start));
                    } else {
                        parsedTokens.add(new Token(TokenType.BIT_AND, "&", null, start));
                        cursor++;
                    }
                    break;
                case '|':
                    if (match('|')) {
                        parsedTokens.add(new Token(TokenType.OR_OR, "||", null, start));
                    } else {
                        parsedTokens.add(new Token(TokenType.BIT_OR, "|", null, start));
                        cursor++;
                    }
                    break;
                default:
                    throw syntaxError("出现了非法字符 '" + c + "'", start);
            }
        }

        private boolean isAtEnd() {
            return cursor >= source.length();
        }

        private boolean match(char expected) {
            if (cursor + 1 < source.length() && source.charAt(cursor + 1) == expected) {
                cursor += 2;
                return true;
            }
            return false;
        }

        private void readIdentifier() {
            int start = cursor;
            cursor++;
            while (!isAtEnd() && isIdentifierPart(source.charAt(cursor))) {
                cursor++;
            }
            String text = source.substring(start, cursor);
            if ("true".equals(text)) {
                parsedTokens.add(new Token(TokenType.TRUE, text, Boolean.TRUE, start));
            } else if ("false".equals(text)) {
                parsedTokens.add(new Token(TokenType.FALSE, text, Boolean.FALSE, start));
            } else if ("null".equals(text)) {
                parsedTokens.add(new Token(TokenType.NULL, text, null, start));
            } else {
                parsedTokens.add(new Token(TokenType.IDENTIFIER, text, null, start));
            }
        }

        private void readNumber() {
            int start = cursor;
            while (!isAtEnd() && Character.isDigit(source.charAt(cursor))) {
                cursor++;
            }
            if (!isAtEnd() && source.charAt(cursor) == '.') {
                cursor++;
                if (isAtEnd() || !Character.isDigit(source.charAt(cursor))) {
                    throw syntaxError("存在非法的小数字面量", start);
                }
                while (!isAtEnd() && Character.isDigit(source.charAt(cursor))) {
                    cursor++;
                }
            }
            String text = source.substring(start, cursor);
            parsedTokens.add(new Token(TokenType.NUMBER, text, new BigDecimal(text), start));
        }

        private void readString(char quote) {
            int start = cursor++;
            StringBuilder builder = new StringBuilder();
            while (!isAtEnd()) {
                char c = source.charAt(cursor++);
                if (c == quote) {
                    parsedTokens.add(new Token(TokenType.STRING, source.substring(start, cursor), builder.toString(), start));
                    return;
                }
                if (c == '\\') {
                    if (isAtEnd()) {
                        throw syntaxError("字符串字面量没有正确结束", start);
                    }
                    char next = source.charAt(cursor++);
                    switch (next) {
                        case '\\':
                            builder.append('\\');
                            break;
                        case '\'':
                            builder.append('\'');
                            break;
                        case '"':
                            builder.append('"');
                            break;
                        case 'n':
                            builder.append('\n');
                            break;
                        case 'r':
                            builder.append('\r');
                            break;
                        case 't':
                            builder.append('\t');
                            break;
                        default:
                            builder.append(next);
                    }
                } else {
                    builder.append(c);
                }
            }
            throw syntaxError("字符串字面量没有正确结束", start);
        }

        private ExpressionSyntaxException syntaxError(String message, int offset) {
            return new ExpressionSyntaxException(formatMessage(message, offset));
        }

        private boolean isIdentifierStart(char c) {
            return Character.isLetter(c) || c == '_';
        }

        private boolean isIdentifierPart(char c) {
            return Character.isLetterOrDigit(c) || c == '_';
        }
    }

    private static final class LiteralExpression implements Expression {
        private final Object value;

        private LiteralExpression(Object value) {
            this.value = value;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            return value;
        }
    }

    private static final class VariableExpression implements Expression {
        private final String name;

        private VariableExpression(String name) {
            this.name = name;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            return context.resolveVariable(name);
        }
    }

    private static final class UnaryExpression implements Expression {
        private final String operator;
        private final Expression target;

        private UnaryExpression(String operator, Expression target) {
            this.operator = operator;
            this.target = target;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            Object value = target.evaluate(context);
            if ("!".equals(operator)) {
                return !ValueSupport.isTruthy(value);
            }
            if ("-".equals(operator)) {
                return ValueSupport.requireNumber(value, "一元 '-' 只能作用于数值").negate();
            }
            throw new DslEvaluationException("不支持的一元运算符: " + operator);
        }
    }

    private static final class BinaryExpression implements Expression {
        private final String operator;
        private final Expression left;
        private final Expression right;

        private BinaryExpression(String operator, Expression left, Expression right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            if ("&&".equals(operator)) {
                return ValueSupport.isTruthy(left.evaluate(context)) && ValueSupport.isTruthy(right.evaluate(context));
            }
            if ("||".equals(operator)) {
                return ValueSupport.isTruthy(left.evaluate(context)) || ValueSupport.isTruthy(right.evaluate(context));
            }
            Object leftValue = left.evaluate(context);
            Object rightValue = right.evaluate(context);
            if ("==".equals(operator)) {
                return ValueSupport.equalsValue(leftValue, rightValue);
            }
            if ("!=".equals(operator)) {
                return !ValueSupport.equalsValue(leftValue, rightValue);
            }
            if (">".equals(operator) || ">=".equals(operator) || "<".equals(operator) || "<=".equals(operator)) {
                int compare = ValueSupport.requireNumber(leftValue, "比较运算两侧必须都是数值")
                        .compareTo(ValueSupport.requireNumber(rightValue, "比较运算两侧必须都是数值"));
                if (">".equals(operator)) {
                    return compare > 0;
                }
                if (">=".equals(operator)) {
                    return compare >= 0;
                }
                if ("<".equals(operator)) {
                    return compare < 0;
                }
                return compare <= 0;
            }
            if ("+".equals(operator)) {
                if (leftValue instanceof Number && rightValue instanceof Number) {
                    return ValueSupport.requireNumber(leftValue, "加法运算两侧必须都是数值")
                            .add(ValueSupport.requireNumber(rightValue, "加法运算两侧必须都是数值"));
                }
                return String.valueOf(leftValue) + String.valueOf(rightValue);
            }
            if ("-".equals(operator)) {
                return ValueSupport.requireNumber(leftValue, "减法运算两侧必须都是数值")
                        .subtract(ValueSupport.requireNumber(rightValue, "减法运算两侧必须都是数值"));
            }
            if ("*".equals(operator)) {
                return ValueSupport.requireNumber(leftValue, "乘法运算两侧必须都是数值")
                        .multiply(ValueSupport.requireNumber(rightValue, "乘法运算两侧必须都是数值"));
            }
            if ("/".equals(operator)) {
                return ValueSupport.divide(
                        ValueSupport.requireNumber(leftValue, "除法运算两侧必须都是数值"),
                        ValueSupport.requireNumber(rightValue, "除法运算两侧必须都是数值")
                );
            }
            if ("%".equals(operator)) {
                return ValueSupport.remainder(
                        ValueSupport.requireNumber(leftValue, "取余运算两侧必须都是数值"),
                        ValueSupport.requireNumber(rightValue, "取余运算两侧必须都是数值")
                );
            }
            if ("join".equals(operator)) {
                return ValueSupport.join(ValueSupport.requireSequence(leftValue, "join 左侧必须是数组"),
                        ValueSupport.requireString(rightValue, "join 的分隔符必须是字符串"));
            }
            if ("repeat".equals(operator)) {
                String text = ValueSupport.requireString(leftValue, "repeat 左侧必须是字符串");
                int times = ValueSupport.requireInteger(rightValue, "repeat 右侧必须是整数");
                if (times < 0) {
                    throw new TypeMismatchException("repeat 的次数不能为负数");
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < times; i++) {
                    builder.append(text);
                }
                return builder.toString();
            }
            if ("&".equals(operator)) {
                return ValueSupport.requireInteger(leftValue, "按位与 '&' 两侧必须都是整数")
                        & ValueSupport.requireInteger(rightValue, "按位与 '&' 两侧必须都是整数");
            }
            if ("|".equals(operator)) {
                return ValueSupport.requireInteger(leftValue, "按位或 '|' 两侧必须都是整数")
                        | ValueSupport.requireInteger(rightValue, "按位或 '|' 两侧必须都是整数");
            }
            if ("<<".equals(operator)) {
                return ValueSupport.requireInteger(leftValue, "左移 '<<' 两侧必须都是整数")
                        << ValueSupport.requireInteger(rightValue, "左移 '<<' 两侧必须都是整数");
            }
            if (">>".equals(operator)) {
                return ValueSupport.requireInteger(leftValue, "右移 '>>' 两侧必须都是整数")
                        >> ValueSupport.requireInteger(rightValue, "右移 '>>' 两侧必须都是整数");
            }
            throw new DslEvaluationException("不支持的二元运算符: " + operator);
        }
    }

    private static final class TernaryExpression implements Expression {
        private final Expression condition;
        private final Expression whenTrue;
        private final Expression whenFalse;

        private TernaryExpression(Expression condition, Expression whenTrue, Expression whenFalse) {
            this.condition = condition;
            this.whenTrue = whenTrue;
            this.whenFalse = whenFalse;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            return ValueSupport.isTruthy(condition.evaluate(context))
                    ? whenTrue.evaluate(context)
                    : whenFalse.evaluate(context);
        }
    }

    private static final class AccessExpression implements Expression {
        private final Expression target;
        private final Expression member;

        private AccessExpression(Expression target, Expression member) {
            this.target = target;
            this.member = member;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            return context.access(target.evaluate(context), member.evaluate(context));
        }
    }

    private static final class FunctionCallExpression implements Expression {
        private final String name;
        private final List<Expression> args;

        private FunctionCallExpression(String name, List<Expression> args) {
            this.name = name;
            this.args = args;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            Object[] values = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) {
                values[i] = args.get(i).evaluate(context);
            }
            return context.invokeFunction(name, values);
        }
    }

    private static final class ArrayExpression implements Expression {
        private final List<Expression> items;

        private ArrayExpression(List<Expression> items) {
            this.items = items;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            List<Object> values = new ArrayList<Object>(items.size());
            for (Expression item : items) {
                values.add(item.evaluate(context));
            }
            return values;
        }
    }

    private static final class ObjectExpression implements Expression {
        private final Map<String, Expression> entries;

        private ObjectExpression(Map<String, Expression> entries) {
            this.entries = entries;
        }

        @Override
        public Object evaluate(EvaluationContext context) {
            Map<String, Object> values = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Expression> entry : entries.entrySet()) {
                values.put(entry.getKey(), entry.getValue().evaluate(context));
            }
            return values;
        }
    }
}
