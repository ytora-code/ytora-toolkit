package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 控制流关键字使用位置非法。
 */
public class ControlFlowUsageException extends DslException {

    private static final long serialVersionUID = 114514;

    public ControlFlowUsageException(String message) {
        super(message);
    }
}
