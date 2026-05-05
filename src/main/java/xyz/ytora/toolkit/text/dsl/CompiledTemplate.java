package xyz.ytora.toolkit.text.dsl;

import xyz.ytora.toolkit.text.dsl.function.FunctionRegistry;
import xyz.ytora.toolkit.text.dsl.runtime.AccessMode;
import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;
import xyz.ytora.toolkit.text.dsl.template.TemplateNode;

/**
 * 编译后的模板。
 *
 * <p>适合在高频渲染场景中缓存后重复使用，避免重复解析模板。</p>
 */
public final class CompiledTemplate {

    private final TemplateNode root;
    private final FunctionRegistry functionRegistry;
    private final AccessMode accessMode;

    CompiledTemplate(TemplateNode root, FunctionRegistry functionRegistry, AccessMode accessMode) {
        this.root = root;
        this.functionRegistry = functionRegistry;
        this.accessMode = accessMode;
    }

    public String render(Object context) {
        StringBuilder output = new StringBuilder();
        EvaluationContext evaluationContext = EvaluationContext.root(context, functionRegistry, accessMode);
        root.render(evaluationContext, output);
        return output.toString();
    }
}
