package xyz.ytora.toolkit.text.dsl.template;

import xyz.ytora.toolkit.text.dsl.SqlRenderResult;
import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 模式渲染上下文。
 */
public final class SqlTemplateRenderContext implements TemplateRenderContext {

    private final EvaluationContext evaluationContext;
    private final StringBuilder sql;
    private final List<Object> parameters;

    private SqlTemplateRenderContext(EvaluationContext evaluationContext, StringBuilder sql, List<Object> parameters) {
        this.evaluationContext = evaluationContext;
        this.sql = sql;
        this.parameters = parameters;
    }

    public static SqlTemplateRenderContext root(EvaluationContext evaluationContext) {
        return new SqlTemplateRenderContext(evaluationContext, new StringBuilder(), new ArrayList<Object>());
    }

    @Override
    public EvaluationContext evaluationContext() {
        return evaluationContext;
    }

    @Override
    public TemplateRenderContext childScope() {
        return new SqlTemplateRenderContext(evaluationContext.childScope(), sql, parameters);
    }

    @Override
    public TemplateRenderContext loopScope(int index, Object item) {
        return new SqlTemplateRenderContext(evaluationContext.loopScope(index, item), sql, parameters);
    }

    @Override
    public void appendText(String text) {
        sql.append(text);
    }

    @Override
    public void appendValue(Object value) {
        sql.append('?');
        parameters.add(value);
    }

    public SqlRenderResult result() {
        return new SqlRenderResult(sql.toString(), parameters);
    }
}
