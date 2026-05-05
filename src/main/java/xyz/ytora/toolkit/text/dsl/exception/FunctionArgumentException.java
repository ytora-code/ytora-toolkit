package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 函数参数不合法。
 */
public class FunctionArgumentException extends DslEvaluationException {

    private static final long serialVersionUID = 114514;

    public FunctionArgumentException(String message) {
        super(message);
    }
}
