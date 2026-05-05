package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 数组索引访问错误。
 */
public class IndexAccessException extends DslEvaluationException {

    private static final long serialVersionUID = 114514;

    public IndexAccessException(String message) {
        super(message);
    }
}
