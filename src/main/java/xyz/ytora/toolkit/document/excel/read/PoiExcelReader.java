package xyz.ytora.toolkit.document.excel.read;

import xyz.ytora.toolkit.document.excel.ExcelException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 Apache POI 的 Excel 内容读取器。
 *
 * <p>通过 {@link WorkbookFactory} 自动识别 xls/xlsx 等 Excel 版本。</p>
 *
 * @author ytora
 * @since 1.0
 */
public class PoiExcelReader implements IExcelReader {

    private final int sheetIndex;

    private final int headerRowIndex;

    private final int startRowIndex;

    public PoiExcelReader() {
        this(0, 0);
    }

    public PoiExcelReader(int sheetIndex, int headerRowIndex) {
        this(sheetIndex, headerRowIndex, headerRowIndex + 1);
    }

    public PoiExcelReader(int sheetIndex, int headerRowIndex, int startRowIndex) {
        if (sheetIndex < 0) {
            throw new IllegalArgumentException("sheet 下标不能小于 0");
        }
        if (headerRowIndex < 0) {
            throw new IllegalArgumentException("表头行下标不能小于 0");
        }
        if (startRowIndex < 0) {
            throw new IllegalArgumentException("数据起始行下标不能小于 0");
        }
        this.sheetIndex = sheetIndex;
        this.headerRowIndex = headerRowIndex;
        this.startRowIndex = startRowIndex;
    }

    @Override
    public List<Map<String, Object>> doRead(InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("Excel 输入流不能为 null");
        }

        try (Workbook workbook = WorkbookFactory.create(is)) {
            if (workbook.getNumberOfSheets() <= sheetIndex) {
                return new ArrayList<>();
            }

            Sheet sheet = workbook.getSheetAt(sheetIndex);
            Row headerRow = sheet.getRow(headerRowIndex);
            if (headerRow == null) {
                return new ArrayList<>();
            }

            List<String> headers = readHeaders(headerRow);
            if (headers.isEmpty()) {
                return new ArrayList<>();
            }

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            List<Map<String, Object>> result = new ArrayList<>();
            int lastRowNum = sheet.getLastRowNum();
            for (int rowIndex = startRowIndex; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Map<String, Object> rowData = readRow(row, headers, evaluator);
                if (!rowData.isEmpty()) {
                    result.add(rowData);
                }
            }
            return result;
        } catch (IOException e) {
            throw new ExcelException("读取 Excel 失败", e);
        }
    }

    private List<String> readHeaders(Row headerRow) {
        short lastCellNum = headerRow.getLastCellNum();
        List<String> headers = new ArrayList<>(Math.max(0, lastCellNum));
        for (int cellIndex = 0; cellIndex < lastCellNum; cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex);
            Object value = readCellValue(cell, null);
            String header = value == null ? null : String.valueOf(value).trim();
            headers.add(header == null || header.isEmpty() ? null : header);
        }
        return headers;
    }

    private Map<String, Object> readRow(Row row, List<String> headers, FormulaEvaluator evaluator) {
        Map<String, Object> rowData = new LinkedHashMap<>(headers.size());
        boolean hasValue = false;
        for (int cellIndex = 0; cellIndex < headers.size(); cellIndex++) {
            String header = headers.get(cellIndex);
            if (header == null) {
                continue;
            }

            Object value = readCellValue(row.getCell(cellIndex), evaluator);
            if (value != null) {
                hasValue = true;
            }
            rowData.put(header, value);
        }
        return hasValue ? rowData : new LinkedHashMap<>();
    }

    private Object readCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            if (evaluator == null) {
                return cell.getCellFormula();
            }
            cellType = evaluator.evaluateFormulaCell(cell);
        }

        if (cellType == CellType.STRING) {
            String value = cell.getStringCellValue();
            return value == null || value.isEmpty() ? null : value;
        }
        if (cellType == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                return date == null ? null : new Date(date.getTime());
            }
            return normalizeNumber(cell.getNumericCellValue());
        }
        if (cellType == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        }
        return null;
    }

    private Object normalizeNumber(double value) {
        BigDecimal decimal = BigDecimal.valueOf(value).stripTrailingZeros();
        if (decimal.scale() <= 0) {
            try {
                return decimal.intValueExact();
            } catch (ArithmeticException e) {
                try {
                    return decimal.longValueExact();
                } catch (ArithmeticException ignored) {
                    return decimal.toBigInteger();
                }
            }
        }
        return decimal;
    }
}
