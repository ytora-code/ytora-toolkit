package xyz.ytora.toolkit.text.dsl.expression;

import xyz.ytora.toolkit.text.dsl.runtime.EvaluationContext;

/**
 * 表达式统一抽象。
 */
public interface Expression {

    Object evaluate(EvaluationContext context);
}
