package xyz.ytora.toolkit.text.dsl.exception;

/**
 * DSL 求值阶段基础异常。
 */
public class DslEvaluationException extends DslException {

    private static final long serialVersionUID = 114514;

    public DslEvaluationException(String message) {
        super(message);
    }

    public DslEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
