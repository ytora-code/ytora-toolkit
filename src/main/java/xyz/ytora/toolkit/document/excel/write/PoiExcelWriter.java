package xyz.ytora.toolkit.document.excel.write;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import xyz.ytora.toolkit.document.excel.ExcelColumn;
import xyz.ytora.toolkit.document.excel.ExcelException;
import xyz.ytora.toolkit.document.excel.ExcelVersion;
import xyz.ytora.toolkit.io.Ios;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * 基于 Apache POI 的 Excel 写出器。
 *
 * <p>默认使用 SXSSF 流式写出 xlsx，适合较大数据量场景。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class PoiExcelWriter implements IExcelWriter {

    private final String sheetName;

    private final ExcelVersion version;

    public PoiExcelWriter() {
        this("Sheet1", ExcelVersion.XLSX);
    }

    public PoiExcelWriter(String sheetName) {
        this(sheetName, ExcelVersion.XLSX);
    }

    public PoiExcelWriter(String sheetName, ExcelVersion version) {
        if (sheetName == null || sheetName.trim().isEmpty()) {
            throw new IllegalArgumentException("sheet 名称不能为空");
        }
        this.sheetName = sheetName;
        this.version = version == null ? ExcelVersion.XLSX : version;
    }

    @Override
    public OutputStream doWrite(List<Map<String, Object>> data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        write(data, outputStream);
        return outputStream;
    }

    /**
     * 将数据写入指定输出流。
     *
     * @param data 数据
     * @param outputStream 输出流
     */
    public void write(List<Map<String, Object>> data, OutputStream outputStream) {
        write(data, outputStream, null, 0);
    }

    /**
     * 将数据按指定列元数据写入输出流。
     *
     * @param data 数据
     * @param outputStream 输出流
     * @param columns 列元数据，为 {@code null} 或空时使用默认表头解析
     * @param headerRowIndex 表头行下标
     */
    public void write(
            List<Map<String, Object>> data,
            OutputStream outputStream,
            List<ExcelColumn> columns,
            int headerRowIndex) {
        if (outputStream == null) {
            throw new IllegalArgumentException("Excel 输出流不能为 null");
        }

        Workbook workbook = createWorkbook();
        try {
            Sheet sheet = workbook.createSheet(sheetName);
            List<ExcelColumn> actualColumns = resolveColumns(data, columns);
            int actualHeaderRowIndex = Math.max(0, headerRowIndex);
            CellStyle headerStyle = getHeaderStyle(workbook);
            CellStyle bodyStyle = getBodyStyle(workbook);
            writeHeader(sheet, actualColumns, actualHeaderRowIndex, headerStyle);
            applyColumnWidths(sheet, actualColumns);
            writeRows(workbook, sheet, actualColumns, data, actualHeaderRowIndex + 1, bodyStyle);
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new ExcelException("写出 Excel 失败", e);
        } finally {
            Ios.close(workbook);
        }
    }

    private Workbook createWorkbook() {
        if (version == ExcelVersion.XLS) {
            return new HSSFWorkbook();
        }
        return new SXSSFWorkbook(100);
    }

    private List<ExcelColumn> resolveColumns(List<Map<String, Object>> data, List<ExcelColumn> columns) {
        if (columns != null && !columns.isEmpty()) {
            return columns;
        }

        Set<String> headers = new LinkedHashSet<String>();
        if (data != null) {
            for (Map<String, Object> row : data) {
                if (row != null) {
                    headers.addAll(row.keySet());
                }
            }
        }
        List<ExcelColumn> result = new ArrayList<ExcelColumn>(headers.size());
        for (String header : headers) {
            result.add(new ExcelColumn(header, header, 20, ""));
        }
        return result;
    }

    private void writeHeader(Sheet sheet, List<ExcelColumn> columns, int headerRowIndex, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(headerRowIndex);
        headerRow.setHeightInPoints(30);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(columns.get(i).getColumnName());
        }
    }

    private void applyColumnWidths(Sheet sheet, List<ExcelColumn> columns) {
        for (int i = 0; i < columns.size(); i++) {
            sheet.setColumnWidth(i, columns.get(i).getWidth() * 256);
        }
    }

    private void writeRows(
            Workbook workbook,
            Sheet sheet,
            List<ExcelColumn> columns,
            List<Map<String, Object>> data,
            int firstDataRowIndex,
            CellStyle bodyStyle) {
        if (data == null || data.isEmpty() || columns.isEmpty()) {
            return;
        }

        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle dateStyle = createBodyFormatStyle(workbook, bodyStyle, "yyyy-mm-dd");
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        CellStyle dateTimeStyle = createBodyFormatStyle(workbook, bodyStyle, "yyyy-mm-dd hh:mm:ss");
        dateTimeStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        Map<String, CellStyle> formatStyleCache = new HashMap<String, CellStyle>();

        int rowIndex = firstDataRowIndex;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            if (rowData == null) {
                continue;
            }
            for (int cellIndex = 0; cellIndex < columns.size(); cellIndex++) {
                ExcelColumn column = columns.get(cellIndex);
                Object value = rowData.get(column.getColumnName());
                if (value == null) {
                    value = rowData.get(column.getPropertyName());
                }
                writeCell(row.createCell(cellIndex), value, column, bodyStyle, dateStyle, dateTimeStyle, formatStyleCache, workbook);
            }
        }
    }

    private void writeCell(
            Cell cell,
            Object value,
            ExcelColumn column,
            CellStyle bodyStyle,
            CellStyle dateStyle,
            CellStyle dateTimeStyle,
            Map<String, CellStyle> formatStyleCache,
            Workbook workbook) {
        cell.setCellStyle(bodyStyle);
        if (value == null) {
            return;
        }
        CellStyle formatStyle = getFormatStyle(workbook, formatStyleCache, column.getFormat());
        if (formatStyle != null) {
            cell.setCellStyle(formatStyle);
        }
        if (value instanceof Number) {
            cell.setCellValue(toDouble((Number) value));
            return;
        }
        if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            return;
        }
        if (value instanceof Date) {
            if (formatStyle == null) {
                cell.setCellStyle(dateTimeStyle);
            }
            cell.setCellValue((Date) value);
            return;
        }
        if (value instanceof LocalDate) {
            if (formatStyle == null) {
                cell.setCellStyle(dateStyle);
            }
            LocalDate localDate = (LocalDate) value;
            cell.setCellValue(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            return;
        }
        if (value instanceof LocalDateTime) {
            if (formatStyle == null) {
                cell.setCellStyle(dateTimeStyle);
            }
            LocalDateTime localDateTime = (LocalDateTime) value;
            cell.setCellValue(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }

    private double toDouble(Number number) {
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).doubleValue();
        }
        if (number instanceof BigInteger) {
            return ((BigInteger) number).doubleValue();
        }
        return number.doubleValue();
    }

    private CellStyle getFormatStyle(Workbook workbook, Map<String, CellStyle> cache, String format) {
        if (format == null || format.trim().isEmpty()) {
            return null;
        }
        CellStyle style = cache.get(format);
        if (style != null) {
            return style;
        }
        CellStyle created = getBodyStyle(workbook);
        created.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(format));
        cache.put(format, created);
        return created;
    }

    private CellStyle createBodyFormatStyle(Workbook workbook, CellStyle bodyStyle, String format) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(bodyStyle);
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat(format));
        return style;
    }

    /**
     * 表头样式。
     */
    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        //背景颜色
        style.setVerticalAlignment(VerticalAlignment.CENTER);
//        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
//        Color backgroundColor;
//        if (workbook instanceof HSSFWorkbook) {
//            backgroundColor = new HSSFColor(0x40, -1, new java.awt.Color(242, 242, 242));
//        } else {
//            backgroundColor = new XSSFColor(new java.awt.Color(242, 242, 242), null);
//        }
//        style.setFillForegroundColor(backgroundColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        applyThinBorder(style);

        Font font = workbook.createFont();
        font.setFontName("黑体");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        font.setItalic(false);
        style.setFont(font);

        return style;
    }

    /**
     * 表体样式。
     */
    private CellStyle getBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        applyThinBorder(style);

        Font font = workbook.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private void applyThinBorder(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
