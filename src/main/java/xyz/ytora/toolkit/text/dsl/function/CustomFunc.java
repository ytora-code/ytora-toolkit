package xyz.ytora.toolkit.text.dsl.function;

/**
 * 自定义函数接口。
 */
public interface CustomFunc extends DslFunction {

    String funcName();

    @Override
    default String name() {
        return funcName();
    }
}
