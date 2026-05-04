package xyz.ytora.toolkit.document.excel;

import java.util.Locale;

/**
 * Excel 文件版本。
 *
 * @author ytora
 * @since 1.0
 */
public enum ExcelVersion {

    /**
     * Excel 97-2003，文件扩展名为 xls。
     */
    XLS,

    /**
     * Excel 2007 及之后版本，文件扩展名为 xlsx。
     */
    XLSX;

    /**
     * 根据文件名推断 Excel 版本。
     *
     * @param fileName 文件名
     * @return Excel 版本
     */
    public static ExcelVersion fromFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return XLSX;
        }
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".xls")) {
            return XLS;
        }
        return XLSX;
    }
}
