package xyz.ytora.toolkit.text.dsl.template;

/**
 * 模板节点统一抽象。
 */
public interface TemplateNode {

    void render(TemplateRenderContext context);
}
