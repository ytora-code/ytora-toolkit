package xyz.ytora.toolkit.text.dsl;

import xyz.ytora.toolkit.text.dsl.function.FunctionRegistry;
import xyz.ytora.toolkit.text.dsl.runtime.AccessMode;
import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;
import xyz.ytora.toolkit.text.dsl.template.SqlTemplateRenderContext;
import xyz.ytora.toolkit.text.dsl.template.TemplateNode;

/**
 * 编译后的 SQL 模板。
 *
 * <p>适合在高频动态 SQL 场景中缓存后重复使用。</p>
 */
public final class CompiledSqlTemplate {

    private final TemplateNode root;
    private final FunctionRegistry functionRegistry;
    private final AccessMode accessMode;

    CompiledSqlTemplate(TemplateNode root, FunctionRegistry functionRegistry, AccessMode accessMode) {
        this.root = root;
        this.functionRegistry = functionRegistry;
        this.accessMode = accessMode;
    }

    public SqlRenderResult render(Object context) {
        EvaluationContext evaluationContext = EvaluationContext.root(context, functionRegistry, accessMode);
        SqlTemplateRenderContext renderContext = SqlTemplateRenderContext.root(evaluationContext);
        root.render(renderContext);
        return renderContext.result();
    }
}
