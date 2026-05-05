package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 值类型不匹配。
 */
public class TypeMismatchException extends DslEvaluationException {

    private static final long serialVersionUID = 114514;

    public TypeMismatchException(String message) {
        super(message);
    }
}
