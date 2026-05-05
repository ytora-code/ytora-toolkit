package xyz.ytora.toolkit.text.dsl.exception;

/**
 * 模板语法错误。
 */
public class TemplateSyntaxException extends DslException {

    private static final long serialVersionUID = 114514;

    public TemplateSyntaxException(String message) {
        super(message);
    }
}
