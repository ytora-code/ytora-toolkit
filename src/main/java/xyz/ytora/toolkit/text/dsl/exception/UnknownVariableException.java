package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 变量不存在。
 */
public class UnknownVariableException extends DslEvaluationException {

    private static final long serialVersionUID = 114514;

    public UnknownVariableException(String message) {
        super(message);
    }
}
