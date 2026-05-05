package xyz.ytora.toolkit.text.dsl.function;

import xyz.ytora.toolkit.text.dsl.exception.DslEvaluationException;
import xyz.ytora.toolkit.text.dsl.exception.FunctionArgumentException;
import xyz.ytora.toolkit.text.dsl.runtime.ValueSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 内置函数统一注册入口。
 */
public final class BuiltinFunctions {

    private BuiltinFunctions() {
    }

    public static void registerInto(FunctionRegistry registry) {
        registry.register(new SimpleFunction("empty") {
            @Override
            protected Object doInvoke(Object... args) {
                requireArgCount("empty", args, 1);
                return ValueSupport.isEmpty(args[0]);
            }
        });
        registry.register(new SimpleFunction("notEmpty") {
            @Override
            protected Object doInvoke(Object... args) {
                requireArgCount("notEmpty", args, 1);
                return !ValueSupport.isEmpty(args[0]);
            }
        });
        registry.register(new SimpleFunction("default") {
            @Override
            protected Object doInvoke(Object... args) {
                requireArgCount("default", args, 2);
                return ValueSupport.isEmpty(args[0]) ? args[1] : args[0];
            }
        });
        registry.register(new SimpleFunction("len") {
            @Override
            protected Object doInvoke(Object... args) {
                requireArgCount("len", args, 1);
                Object value = args[0];
                if (value == null) {
                    return 0;
                }
                if (value instanceof String) {
                    return ((String) value).length();
                }
                if (value instanceof Collection) {
                    return ((Collection<?>) value).size();
                }
                if (value instanceof Map) {
                    return ((Map<?, ?>) value).size();
                }
                if (value.getClass().isArray()) {
                    return java.lang.reflect.Array.getLength(value);
                }
                throw new DslEvaluationException("len(...) 只支持字符串、集合、数组和对象");
            }
        });
        registry.register(new SimpleFunction("split") {
            @Override
            protected Object doInvoke(Object... args) {
                requireArgCount("split", args, 2);
                String text = ValueSupport.requireString(args[0], "split(...) 的第一个参数必须是字符串");
                String separator = ValueSupport.requireString(args[1], "split(...) 的第二个参数必须是字符串");
                String[] items = text.split(java.util.regex.Pattern.quote(separator), -1);
                List<String> result = new ArrayList<String>(items.length);
                for (String item : items) {
                    result.add(item);
                }
                return result;
            }
        });
    }

    private abstract static class SimpleFunction implements DslFunction {

        private final String name;

        private SimpleFunction(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object invoke(Object... args) {
            return doInvoke(args);
        }

        protected abstract Object doInvoke(Object... args);

        protected void requireArgCount(String functionName, Object[] args, int expected) {
            if (args.length != expected) {
                throw new FunctionArgumentException(functionName + "(...) 期望参数个数为 " + expected + "，实际得到 " + args.length);
            }
        }
    }
}
