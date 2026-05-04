package xyz.ytora.toolkit.document.excel.read;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Excel内容读取器
 *
 * <p>将 Excel InputStream 读取成 List&lt;Map&gt; 对象</p>
 *
 * @author ytora
 * @since 1.0
 */
public interface IExcelReader {

    /**
     * 将 EXCEL 数据读入内存
     * @param is EXCEL 输入流
     * @return 读取到的数据
     */
    List<Map<String, Object>> doRead(InputStream is);

}
