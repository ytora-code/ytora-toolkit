package xyz.ytora.toolkit.document.excel;

/**
 * Excel 读写异常。
 *
 * @author ytora
 * @since 1.0
 */
public class ExcelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
