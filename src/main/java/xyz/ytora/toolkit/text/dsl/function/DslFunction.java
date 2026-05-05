package xyz.ytora.toolkit.text.dsl.function;

/**
 * DSL 自定义函数扩展点。
 */
public interface DslFunction {

    String name();

    Object invoke(Object... args);
}
