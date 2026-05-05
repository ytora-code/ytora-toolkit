package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 占位符渲染值不合法。
 */
public class RenderValueException extends DslEvaluationException {

    private static final long serialVersionUID = 114514;

    public RenderValueException(String message) {
        super(message);
    }
}
