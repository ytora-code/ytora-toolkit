package xyz.ytora.toolkit.text.dsl.template;

import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;

/**
 * 模板渲染上下文。
 *
 * <p>同时封装变量求值作用域和渲染输出目标，避免模板节点感知具体输出介质。</p>
 */
public interface TemplateRenderContext {

    EvaluationContext evaluationContext();

    TemplateRenderContext childScope();

    TemplateRenderContext loopScope(int index, Object item);

    void appendText(String text);

    void appendValue(Object value);
}
