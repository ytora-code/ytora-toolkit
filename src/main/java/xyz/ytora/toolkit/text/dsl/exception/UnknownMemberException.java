package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 对象字段不存在。
 */
public class UnknownMemberException extends DslEvaluationException {

    private static final long serialVersionUID = 114514;

    public UnknownMemberException(String message) {
        super(message);
    }
}
