package xyz.ytora.toolkit.text.dsl.exception;

/**
 * DSL 基础异常。
 */
public class DslException extends RuntimeException {

    private static final long serialVersionUID = 114514;

    public DslException(String message) {
        super(message);
    }

    public DslException(String message, Throwable cause) {
        super(message, cause);
    }
}
