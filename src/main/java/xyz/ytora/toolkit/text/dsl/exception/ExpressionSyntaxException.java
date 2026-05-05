package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 表达式语法错误。
 */
public class ExpressionSyntaxException extends DslException {

    private static final long serialVersionUID = 114514;

    public ExpressionSyntaxException(String message) {
        super(message);
    }
}
