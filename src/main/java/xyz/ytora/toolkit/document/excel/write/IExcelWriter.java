package xyz.ytora.toolkit.document.excel.write;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel写出器
 *
 * <p>将数据 List<Map> 写入 OutputStream</p>
 *
 * @author ytora 
 * @since 1.0
 */
public interface IExcelWriter {

    /**
     * 将内存数据写出 EXCEL
     * @param data 数据
     */
    OutputStream doWrite(List<Map<String, Object>> data);
}
