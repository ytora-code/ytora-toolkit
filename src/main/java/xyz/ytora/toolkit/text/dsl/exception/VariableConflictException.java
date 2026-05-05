package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 变量名称冲突。
 */
public class VariableConflictException extends DslException {

    private static final long serialVersionUID = 114514;

    public VariableConflictException(String message) {
        super(message);
    }
}
