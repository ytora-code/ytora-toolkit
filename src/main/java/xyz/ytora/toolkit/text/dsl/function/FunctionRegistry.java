package xyz.ytora.toolkit.text.dsl.function;

import xyz.ytora.toolkit.text.dsl.exception.DslEvaluationException;
import xyz.ytora.toolkit.text.dsl.exception.FunctionNotFoundException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 函数注册表。
 */
public final class FunctionRegistry {

    private final Map<String, DslFunction> functions = new LinkedHashMap<>();

    public void register(DslFunction function) {
        if (function == null) {
            throw new IllegalArgumentException("函数不能为空");
        }
        functions.put(normalize(function.name()), function);
    }

    public Object invoke(String name, Object... args) {
        DslFunction function = functions.get(normalize(name));
        if (function == null) {
            throw new FunctionNotFoundException("函数不存在: " + name);
        }
        try {
            return function.invoke(args);
        } catch (DslEvaluationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DslEvaluationException("函数调用失败: " + name, ex);
        }
    }

    public FunctionRegistry copy() {
        FunctionRegistry registry = new FunctionRegistry();
        registry.functions.putAll(this.functions);
        return registry;
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
