package xyz.ytora.toolkit.document.excel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bean 上 Excel 注解解析结果。
 *
 * @author ytora
 * @since 1.0
 */
final class ExcelBeanMeta {

    private final String fileName;

    private final int headerRowIndex;

    private final int startRowIndex;

    private final List<ExcelColumn> columns;

    private final Map<String, ExcelColumn> columnNameMap;

    private final Set<String> ignoredNames;

    private final boolean annotated;

    private ExcelBeanMeta(
            String fileName,
            int headerRowIndex,
            int startRowIndex,
            List<ExcelColumn> columns,
            Set<String> ignoredNames,
            boolean annotated) {
        this.fileName = fileName;
        this.headerRowIndex = headerRowIndex;
        this.startRowIndex = startRowIndex;
        this.columns = Collections.unmodifiableList(columns);
        this.columnNameMap = buildColumnNameMap(columns);
        this.ignoredNames = Collections.unmodifiableSet(ignoredNames);
        this.annotated = annotated;
    }

    static ExcelBeanMeta of(Class<?> beanClass) {
        Excel typeExcel = beanClass.getAnnotation(Excel.class);
        String fileName = typeExcel == null ? "" : typeExcel.value();
        int headerRowIndex = typeExcel == null ? 0 : Math.max(0, typeExcel.headerRowIndex());
        int startRowIndex = typeExcel == null
                ? headerRowIndex + 1
                : Math.max(headerRowIndex + 1, typeExcel.startRow());
        boolean annotated = typeExcel != null;

        List<FieldColumn> fieldColumns = new ArrayList<FieldColumn>();
        Set<String> ignoredNames = new LinkedHashSet<String>();
        List<Field> fields = getFields(beanClass);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Excel excel = field.getAnnotation(Excel.class);
            if (excel != null) {
                annotated = true;
            }
            if (excel != null && excel.ignore()) {
                ignoredNames.add(field.getName());
                if (!excel.value().trim().isEmpty()) {
                    ignoredNames.add(excel.value().trim());
                }
                continue;
            }

            String propertyName = field.getName();
            String columnName = excel == null || excel.value().trim().isEmpty()
                    ? propertyName
                    : excel.value().trim();
            int index = excel == null ? Integer.MAX_VALUE : excel.index();
            int width = excel == null ? 20 : Math.max(1, excel.width());
            String format = excel == null ? "" : excel.format();
            fieldColumns.add(new FieldColumn(
                    index,
                    i,
                    new ExcelColumn(propertyName, columnName, width, format)
            ));
        }

        Collections.sort(fieldColumns, new Comparator<FieldColumn>() {
            @Override
            public int compare(FieldColumn left, FieldColumn right) {
                int indexCompare = Integer.compare(left.index, right.index);
                if (indexCompare != 0) {
                    return indexCompare;
                }
                return Integer.compare(left.declaredOrder, right.declaredOrder);
            }
        });

        List<ExcelColumn> columns = new ArrayList<ExcelColumn>(fieldColumns.size());
        for (FieldColumn fieldColumn : fieldColumns) {
            columns.add(fieldColumn.column);
        }
        return new ExcelBeanMeta(fileName, headerRowIndex, startRowIndex, columns, ignoredNames, annotated);
    }

    String getFileName() {
        return fileName;
    }

    int getHeaderRowIndex() {
        return headerRowIndex;
    }

    int getStartRowIndex() {
        return startRowIndex;
    }

    List<ExcelColumn> getColumns() {
        return columns;
    }

    boolean isAnnotated() {
        return annotated;
    }

    Map<String, Object> toPropertyMap(Map<String, Object> row) {
        Map<String, Object> result = new HashMap<String, Object>(row == null ? 0 : row.size());
        if (row == null || row.isEmpty()) {
            return result;
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            ExcelColumn column = columnNameMap.get(entry.getKey());
            if (column == null && ignoredNames.contains(entry.getKey())) {
                continue;
            }
            result.put(column == null ? entry.getKey() : column.getPropertyName(), entry.getValue());
        }
        return result;
    }

    private static Map<String, ExcelColumn> buildColumnNameMap(List<ExcelColumn> columns) {
        Map<String, ExcelColumn> result = new HashMap<String, ExcelColumn>(columns.size() * 2);
        for (ExcelColumn column : columns) {
            result.put(column.getColumnName(), column);
            result.put(column.getPropertyName(), column);
        }
        return result;
    }

    private static List<Field> getFields(Class<?> beanClass) {
        List<Class<?>> hierarchy = new ArrayList<Class<?>>();
        Class<?> current = beanClass;
        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }
        Collections.reverse(hierarchy);

        Set<String> seen = new LinkedHashSet<String>();
        List<Field> result = new ArrayList<Field>();
        for (Class<?> type : hierarchy) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (seen.add(field.getName())) {
                    result.add(field);
                }
            }
        }
        return result;
    }

    private static final class FieldColumn {

        private final int index;

        private final int declaredOrder;

        private final ExcelColumn column;

        private FieldColumn(int index, int declaredOrder, ExcelColumn column) {
            this.index = index;
            this.declaredOrder = declaredOrder;
            this.column = column;
        }
    }
}
