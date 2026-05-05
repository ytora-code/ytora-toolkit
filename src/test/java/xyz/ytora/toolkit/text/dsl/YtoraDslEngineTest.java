package xyz.ytora.toolkit.text.dsl;

import org.junit.jupiter.api.Test;
import xyz.ytora.toolkit.text.dsl.function.DslFunction;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 面向使用方式的示例测试。
 *
 */
public class YtoraDslEngineTest {

    @Test
    public void shouldRenderTemplateWithControlFlow() {
        String template =
                "姓名：#{name}\n" +
                "年龄：#{age}\n" +
                "\n" +
                "if: age >= 18 {\n" +
                "  成年人\n" +
                "} else {\n" +
                "  未成年人\n" +
                "}\n" +
                "\n" +
                "for: hobbies where index % 2 == 0 {\n" +
                "  #{index} - #{item}\n" +
                "}\n" +
                "城市：#{default(address.city, \"未知\")}\n" +
                "爱好合并：#{hobbies join \",\"}\n" +
                "分隔线：#{\"-\" repeat 5}\n" +
                "位运算：#{2 << 3}\n";

        Map<String, Object> address = new LinkedHashMap<String, Object>();
        address.put("city", "京州市");

        Map<String, Object> context = new LinkedHashMap<String, Object>();
        context.put("name", "张三");
        context.put("age", 99);
        context.put("hobbies", Arrays.asList("唱", "跳", "rap", "篮球"));
        context.put("address", address);

        String result = YtoraDslEngine.createDefault().render(template, context);
        assertTrue(result.contains("姓名：张三"));
        assertTrue(result.contains("成年人"));
        assertTrue(result.contains("0 - 唱"));
        assertTrue(result.contains("2 - rap"));
        assertTrue(result.contains("爱好合并：唱,跳,rap,篮球"));
        assertTrue(result.contains("分隔线：-----"));
        assertTrue(result.contains("位运算：16"));
    }

    @Test
    public void shouldSupportCustomFunction() {
        YtoraDslEngine engine = YtoraDslEngine.builder()
                .registerFunction(new DslFunction() {
                    @Override
                    public String name() {
                        return "upper";
                    }

                    @Override
                    public Object invoke(Object... args) {
                        return String.valueOf(args[0]).toUpperCase();
                    }
                })
                .build();

        Map<String, Object> context = new LinkedHashMap<String, Object>();
        context.put("name", "ytora");

        assertEquals("YTORA", engine.render("#{upper(name)}", context));
    }
}
