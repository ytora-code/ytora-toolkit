package xyz.ytora.toolkit.text.dsl.template;

import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;

/**
 * 模板节点统一抽象。
 */
public interface TemplateNode {

    void render(EvaluationContext context, StringBuilder output);
}
