package xyz.ytora.toolkit.text.dsl.support;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DSL 保留名称集合。
 */
public final class DslReservedNames {

    private static final Set<String> RESERVED = new LinkedHashSet<String>(Arrays.asList(
            "if", "else", "for", "where", "when", "case", "default", "set", "raw",
            "break", "continue", "true", "false", "null", "item", "index"
    ));

    private DslReservedNames() {
    }

    public static boolean isReserved(String name) {
        return RESERVED.contains(name);
    }
}
