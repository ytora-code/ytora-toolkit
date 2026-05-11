package xyz.ytora.toolkit.text.dsl.template;

import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;
import xyz.ytora.toolkit.text.dsl.runtime.ValueSupport;

/**
 * 普通文本渲染上下文。
 */
public final class TextTemplateRenderContext implements TemplateRenderContext {

    private final EvaluationContext evaluationContext;
    private final StringBuilder output;

    private TextTemplateRenderContext(EvaluationContext evaluationContext, StringBuilder output) {
        this.evaluationContext = evaluationContext;
        this.output = output;
    }

    public static TextTemplateRenderContext root(EvaluationContext evaluationContext) {
        return new TextTemplateRenderContext(evaluationContext, new StringBuilder());
    }

    @Override
    public EvaluationContext evaluationContext() {
        return evaluationContext;
    }

    @Override
    public TemplateRenderContext childScope() {
        return new TextTemplateRenderContext(evaluationContext.childScope(), output);
    }

    @Override
    public TemplateRenderContext loopScope(int index, Object item) {
        return new TextTemplateRenderContext(evaluationContext.loopScope(index, item), output);
    }

    @Override
    public void appendText(String text) {
        output.append(text);
    }

    @Override
    public void appendValue(Object value) {
        output.append(ValueSupport.stringifyRenderValue(value));
    }

    public String result() {
        return output.toString();
    }
}
