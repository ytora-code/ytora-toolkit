package xyz.ytora.toolkit.text.dsl.runtime;

import xyz.ytora.toolkit.text.dsl.exception.ControlFlowUsageException;
import xyz.ytora.toolkit.text.dsl.exception.VariableConflictException;
import xyz.ytora.toolkit.text.dsl.function.FunctionRegistry;
import xyz.ytora.toolkit.text.dsl.support.DslReservedNames;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表达式和模板渲染共享的运行时上下文。
 *
 * <p>该对象只处理变量可见性、函数调用和控制流边界，不负责语法解析。</p>
 */
public final class EvaluationContext {

    private final Object rootObject;
    private final FunctionRegistry functionRegistry;
    private final AccessMode accessMode;
    private final EvaluationContext parent;
    private final Map<String, Object> locals;
    private final boolean insideLoop;

    private EvaluationContext(Object rootObject,
                              FunctionRegistry functionRegistry,
                              AccessMode accessMode,
                              EvaluationContext parent,
                              Map<String, Object> locals,
                              boolean insideLoop) {
        this.rootObject = rootObject;
        this.functionRegistry = functionRegistry;
        this.accessMode = accessMode;
        this.parent = parent;
        this.locals = locals;
        this.insideLoop = insideLoop;
    }

    public static EvaluationContext root(Object rootObject, FunctionRegistry functionRegistry, AccessMode accessMode) {
        return new EvaluationContext(rootObject, functionRegistry, accessMode, null,
                new LinkedHashMap<String, Object>(), false);
    }

    public EvaluationContext childScope() {
        return new EvaluationContext(rootObject, functionRegistry, accessMode, this,
                new LinkedHashMap<String, Object>(), insideLoop);
    }

    public EvaluationContext loopScope(int index, Object item) {
        EvaluationContext scope = new EvaluationContext(rootObject, functionRegistry, accessMode, this,
                new LinkedHashMap<String, Object>(), true);
        scope.locals.put("index", index);
        scope.locals.put("item", item);
        return scope;
    }

    public void setLocal(String name, Object value) {
        if (DslReservedNames.isReserved(name)) {
            throw new VariableConflictException("变量名属于保留字，禁止使用: " + name);
        }
        if (containsVisibleVariable(name)) {
            throw new VariableConflictException("变量名冲突: " + name);
        }
        locals.put(name, value);
    }

    public Object resolveVariable(String name) {
        if (locals.containsKey(name)) {
            return locals.get(name);
        }
        if (parent != null) {
            return parent.resolveVariable(name);
        }
        return ValueSupport.resolveRootValue(rootObject, name, accessMode);
    }

    public Object access(Object target, Object member) {
        return ValueSupport.access(target, member, accessMode);
    }

    public Object invokeFunction(String name, Object... args) {
        return functionRegistry.invoke(name, args);
    }

    public void ensureInsideLoop(String keyword) {
        if (!insideLoop) {
            throw new ControlFlowUsageException(keyword + " 只能在 for 块内部使用");
        }
    }

    public boolean containsVisibleVariable(String name) {
        if (locals.containsKey(name)) {
            return true;
        }
        if (parent != null) {
            return parent.containsVisibleVariable(name);
        }
        return ValueSupport.containsRootValue(rootObject, name);
    }
}
