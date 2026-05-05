package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 函数不存在。
 */
public class FunctionNotFoundException extends DslEvaluationException {

    private static final long serialVersionUID = 114514;

    public FunctionNotFoundException(String message) {
        super(message);
    }
}
