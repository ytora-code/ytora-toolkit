package xyz.ytora.toolkit.text.dsl.runtime;

/**
 * 访问模式。
 * <p>在变量/字段/下标访问失败时，DSL 应该怎么处理</p>
 */
public enum AccessMode {
    /**
     * 宽松模式，当变量不存在、对象字段不存在、数组下标越界、访问null时，不抛错，返回 null
     */
    LENIENT,
    /**
     * 严格模式，当变量不存在、对象字段不存在、数组下标越界、访问null时，直接抛异常
     */
    STRICT
}
