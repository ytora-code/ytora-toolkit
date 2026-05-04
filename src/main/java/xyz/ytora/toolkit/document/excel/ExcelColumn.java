package xyz.ytora.toolkit.document.excel;

/**
 * Excel 列元数据。
 *
 * @author ytora
 * @since 1.0
 */
public final class ExcelColumn {

    private final String propertyName;

    private final String columnName;

    private final int width;

    private final String format;

    public ExcelColumn(String propertyName, String columnName, int width, String format) {
        this.propertyName = propertyName;
        this.columnName = columnName;
        this.width = width;
        this.format = format == null ? "" : format;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getColumnName() {
        return columnName;
    }

    public int getWidth() {
        return width;
    }

    public String getFormat() {
        return format;
    }
}
