package xyz.ytora.toolkit.text.dsl;

import xyz.ytora.toolkit.text.dsl.function.BuiltinFunctions;
import xyz.ytora.toolkit.text.dsl.function.DslFunction;
import xyz.ytora.toolkit.text.dsl.function.FunctionRegistry;
import xyz.ytora.toolkit.text.dsl.runtime.AccessMode;
import xyz.ytora.toolkit.text.dsl.template.TemplateNode;
import xyz.ytora.toolkit.text.dsl.template.TemplateParser;

import java.util.Map;

/**
 * DSL 模板引擎。
 *
 * <p>使用方法：</p>
 * <p>1. 构建引擎并按需注册自定义函数。</p>
 * <p>2. 按需调用 {@link #render(String, Object)}、{@link #renderSql(String, Object)}
 * 或对应的 compile 方法即可。</p>
 *
 * <pre>{@code
 * YtoraDslEngine engine = YtoraDslEngine.builder()
 *         .registerFunction(new CustomFunc() {
 *             public String funcName() {
 *                 return "upper";
 *             }
 *
 *             public Object invoke(Object... args) {
 *                 return String.valueOf(args[0]).toUpperCase();
 *             }
 *         })
 *         .build();
 *
 * String result = engine.render("姓名：#{upper(name)}", context);
 * }</pre>
 */
public final class YtoraDslEngine {

    /**
     * 自定义函数注册器
     */
    private final FunctionRegistry functionRegistry;
    /**
     * 访问模式
     */
    private final AccessMode accessMode;
    /**
     * 模板解析器
     */
    private final TemplateParser templateParser;

    private YtoraDslEngine(FunctionRegistry functionRegistry, AccessMode accessMode) {
        this.functionRegistry = functionRegistry;
        this.accessMode = accessMode;
        this.templateParser = new TemplateParser(functionRegistry, accessMode);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static YtoraDslEngine createDefault() {
        return builder().build();
    }

    public String render(String template, Object context) {
        return compile(template).render(context);
    }

    public SqlRenderResult renderSql(String template, Object context) {
        return compileSql(template).render(context);
    }

    public String render(String template, Map<String, Object> context) {
        return render(template, (Object) context);
    }

    public SqlRenderResult renderSql(String template, Map<String, Object> context) {
        return renderSql(template, (Object) context);
    }

    public CompiledTemplate compile(String template) {
        TemplateNode root = templateParser.parse(template);
        return new CompiledTemplate(root, functionRegistry, accessMode);
    }

    public CompiledSqlTemplate compileSql(String template) {
        TemplateNode root = templateParser.parse(template);
        return new CompiledSqlTemplate(root, functionRegistry, accessMode);
    }

    public FunctionRegistry functionRegistry() {
        return functionRegistry;
    }

    public AccessMode accessMode() {
        return accessMode;
    }

    /**
     * 引擎构建器。
     */
    public static final class Builder {

        private final FunctionRegistry functionRegistry = new FunctionRegistry();
        private AccessMode accessMode = AccessMode.LENIENT;

        private Builder() {
            BuiltinFunctions.registerInto(functionRegistry);
        }

        public Builder strictMode() {
            this.accessMode = AccessMode.STRICT;
            return this;
        }

        public Builder lenientMode() {
            this.accessMode = AccessMode.LENIENT;
            return this;
        }

        public Builder registerFunction(DslFunction function) {
            functionRegistry.register(function);
            return this;
        }

        public YtoraDslEngine build() {
            return new YtoraDslEngine(functionRegistry.copy(), accessMode);
        }
    }
}
