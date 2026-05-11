package xyz.ytora.toolkit.text.dsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SQL 模式渲染结果。
 *
 * <p>包含可直接交给 JDBC 的 SQL 文本和按占位符出现顺序收集的参数列表。</p>
 */
public final class SqlRenderResult {

    private final String sql;
    private final List<Object> parameters;

    public SqlRenderResult(String sql, List<Object> parameters) {
        this.sql = sql;
        this.parameters = Collections.unmodifiableList(new ArrayList<Object>(parameters));
    }

    public String sql() {
        return sql;
    }

    public List<Object> parameters() {
        return parameters;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
