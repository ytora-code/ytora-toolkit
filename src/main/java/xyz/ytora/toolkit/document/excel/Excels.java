package xyz.ytora.toolkit.document.excel;

import xyz.ytora.toolkit.bean.Beans;
import xyz.ytora.toolkit.document.excel.read.IExcelReader;
import xyz.ytora.toolkit.document.excel.read.PoiExcelReader;
import xyz.ytora.toolkit.document.excel.write.PoiExcelWriter;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel 操作工具类。
 *
 * <p>底层读写以 {@code List<Map<String, Object>>} 为标准数据模型；
 * Bean 读写由本工具类借助 {@link Beans} 完成。</p>
 *
 * @author ytora
 * @since 1.0
 */
public final class Excels {

    private static final IExcelReader DEFAULT_READER = new PoiExcelReader();

    private static final PoiExcelWriter DEFAULT_WRITER = new PoiExcelWriter();

    private Excels() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 从 Excel 输入流读取数据。
     *
     * @param inputStream Excel 输入流
     * @return 行数据列表
     */
    public static List<Map<String, Object>> read(InputStream inputStream) {
        return DEFAULT_READER.doRead(inputStream);
    }

    /**
     * 从 Excel 文件读取数据。
     *
     * @param file Excel 文件
     * @return 行数据列表
     */
    public static List<Map<String, Object>> read(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Excel 文件不能为 null");
        }
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return read(inputStream);
        } catch (IOException e) {
            throw new ExcelException("读取 Excel 文件失败：" + file.getAbsolutePath(), e);
        }
    }

    /**
     * 从 Excel 文件读取数据。
     *
     * @param filePath Excel 文件路径
     * @return 行数据列表
     */
    public static List<Map<String, Object>> read(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Excel 文件路径不能为空");
        }
        return read(new File(filePath));
    }

    /**
     * 从 Excel 输入流读取数据并转换为 Bean。
     *
     * @param inputStream Excel 输入流
     * @param beanClass Bean 类型
     * @param <T> Bean 类型
     * @return Bean 列表
     */
    public static <T> List<T> readBeans(InputStream inputStream, Class<T> beanClass) {
        if (beanClass == null) {
            throw new IllegalArgumentException("Bean 类型不能为 null");
        }
        ExcelBeanMeta meta = ExcelBeanMeta.of(beanClass);
        IExcelReader reader = meta.isAnnotated()
                ? new PoiExcelReader(0, meta.getHeaderRowIndex(), meta.getStartRowIndex())
                : DEFAULT_READER;
        return toBeans(reader.doRead(inputStream), beanClass, meta);
    }

    /**
     * 从 Excel 文件读取数据并转换为 Bean。
     *
     * @param file Excel 文件
     * @param beanClass Bean 类型
     * @param <T> Bean 类型
     * @return Bean 列表
     */
    public static <T> List<T> readBeans(File file, Class<T> beanClass) {
        if (file == null) {
            throw new IllegalArgumentException("Excel 文件不能为 null");
        }
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return readBeans(inputStream, beanClass);
        } catch (IOException e) {
            throw new ExcelException("读取 Excel 文件失败：" + file.getAbsolutePath(), e);
        }
    }

    /**
     * 从 Excel 文件读取数据并转换为 Bean。
     *
     * @param filePath Excel 文件路径
     * @param beanClass Bean 类型
     * @param <T> Bean 类型
     * @return Bean 列表
     */
    public static <T> List<T> readBeans(String filePath, Class<T> beanClass) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Excel 文件路径不能为空");
        }
        return readBeans(new File(filePath), beanClass);
    }

    /**
     * 将数据写成 xlsx 字节数组。
     *
     * @param data 行数据列表
     * @return xlsx 字节数组
     */
    public static byte[] write(List<Map<String, Object>> data) {
        OutputStream outputStream = DEFAULT_WRITER.doWrite(data);
        if (outputStream instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) outputStream).toByteArray();
        }
        throw new ExcelException("默认 Excel 写出器没有返回字节数组输出流");
    }

    /**
     * 将数据按指定列元数据写成 xlsx 字节数组。
     *
     * @param data 行数据列表
     * @param columns 列元数据
     * @return xlsx 字节数组
     */
    public static byte[] write(List<Map<String, Object>> data, List<ExcelColumn> columns) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DEFAULT_WRITER.write(data, outputStream, columns, 0);
        return outputStream.toByteArray();
    }

    /**
     * 将数据写入输出流。
     *
     * @param data 行数据列表
     * @param outputStream 输出流
     */
    public static void write(List<Map<String, Object>> data, OutputStream outputStream) {
        DEFAULT_WRITER.write(data, outputStream);
    }

    /**
     * 将数据按指定列元数据写入输出流。
     *
     * @param data 行数据列表
     * @param outputStream 输出流
     * @param columns 列元数据
     */
    public static void write(List<Map<String, Object>> data, OutputStream outputStream, List<ExcelColumn> columns) {
        DEFAULT_WRITER.write(data, outputStream, columns, 0);
    }

    /**
     * 将数据写入文件。
     *
     * @param data 行数据列表
     * @param file 目标文件
     */
    public static void write(List<Map<String, Object>> data, File file) {
        if (file == null) {
            throw new IllegalArgumentException("Excel 文件不能为 null");
        }
        try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
            PoiExcelWriter writer = new PoiExcelWriter("Sheet1", ExcelVersion.fromFileName(file.getName()));
            writer.write(data, outputStream);
        } catch (IOException e) {
            throw new ExcelException("写入 Excel 文件失败：" + file.getAbsolutePath(), e);
        }
    }

    /**
     * 将数据按指定列元数据写入文件。
     *
     * @param data 行数据列表
     * @param file 目标文件
     * @param columns 列元数据
     */
    public static void write(List<Map<String, Object>> data, File file, List<ExcelColumn> columns) {
        if (file == null) {
            throw new IllegalArgumentException("Excel 文件不能为 null");
        }
        try (OutputStream outputStream = Files.newOutputStream(file.toPath())) {
            PoiExcelWriter writer = new PoiExcelWriter("Sheet1", ExcelVersion.fromFileName(file.getName()));
            writer.write(data, outputStream, columns, 0);
        } catch (IOException e) {
            throw new ExcelException("写入 Excel 文件失败：" + file.getAbsolutePath(), e);
        }
    }

    /**
     * 将数据写入文件。
     *
     * @param data 行数据列表
     * @param filePath 目标文件路径
     */
    public static void write(List<Map<String, Object>> data, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Excel 文件路径不能为空");
        }
        write(data, new File(filePath));
    }

    /**
     * 将数据按指定列元数据写入文件。
     *
     * @param data 行数据列表
     * @param filePath 目标文件路径
     * @param columns 列元数据
     */
    public static void write(List<Map<String, Object>> data, String filePath, List<ExcelColumn> columns) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Excel 文件路径不能为空");
        }
        write(data, new File(filePath), columns);
    }

    /**
     * 将 Bean 列表写成 xlsx 字节数组。
     *
     * @param beans Bean 列表
     * @return xlsx 字节数组
     */
    public static byte[] writeBeans(List<?> beans) {
        ExcelBeanMeta meta = resolveMeta(beans);
        if (meta == null || !meta.isAnnotated()) {
            return write(toMaps(beans));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DEFAULT_WRITER.write(toMaps(beans, meta), outputStream, meta.getColumns(), meta.getHeaderRowIndex());
        return outputStream.toByteArray();
    }

    /**
     * 将 Bean 列表按指定 Bean 类型写成 xlsx 字节数组。
     *
     * @param beans Bean 列表
     * @param beanClass Bean 类型
     * @return xlsx 字节数组
     */
    public static byte[] writeBeans(List<?> beans, Class<?> beanClass) {
        ExcelBeanMeta meta = resolveMeta(beans, beanClass);
        if (meta == null || !meta.isAnnotated()) {
            return write(toMaps(beans));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DEFAULT_WRITER.write(toMaps(beans, meta), outputStream, meta.getColumns(), meta.getHeaderRowIndex());
        return outputStream.toByteArray();
    }

    /**
     * 将 Bean 列表写入输出流。
     *
     * @param beans Bean 列表
     * @param outputStream 输出流
     */
    public static void writeBeans(List<?> beans, OutputStream outputStream) {
        ExcelBeanMeta meta = resolveMeta(beans);
        if (meta == null || !meta.isAnnotated()) {
            write(toMaps(beans), outputStream);
            return;
        }
        DEFAULT_WRITER.write(toMaps(beans, meta), outputStream, meta.getColumns(), meta.getHeaderRowIndex());
    }

    /**
     * 将 Bean 列表按指定 Bean 类型写入输出流。
     *
     * @param beans Bean 列表
     * @param beanClass Bean 类型
     * @param outputStream 输出流
     */
    public static void writeBeans(List<?> beans, Class<?> beanClass, OutputStream outputStream) {
        ExcelBeanMeta meta = resolveMeta(beans, beanClass);
        if (meta == null || !meta.isAnnotated()) {
            write(toMaps(beans), outputStream);
            return;
        }
        DEFAULT_WRITER.write(toMaps(beans, meta), outputStream, meta.getColumns(), meta.getHeaderRowIndex());
    }

    /**
     * 将 Bean 列表写入文件。
     *
     * @param beans Bean 列表
     * @param file 目标文件
     */
    public static void writeBeans(List<?> beans, File file) {
        ExcelBeanMeta meta = resolveMeta(beans);
        File actualFile = resolveOutputFile(file, meta);
        if (meta == null || !meta.isAnnotated()) {
            write(toMaps(beans), actualFile);
            return;
        }

        if (actualFile == null) {
            throw new IllegalArgumentException("Excel 文件不能为 null");
        }
        try (OutputStream outputStream = Files.newOutputStream(actualFile.toPath())) {
            PoiExcelWriter writer = new PoiExcelWriter("Sheet1", ExcelVersion.fromFileName(actualFile.getName()));
            writer.write(toMaps(beans, meta), outputStream, meta.getColumns(), meta.getHeaderRowIndex());
        } catch (IOException e) {
            throw new ExcelException("写入 Excel 文件失败：" + actualFile.getAbsolutePath(), e);
        }
    }

    /**
     * 将 Bean 列表按指定 Bean 类型写入文件。
     *
     * @param beans Bean 列表
     * @param beanClass Bean 类型
     * @param file 目标文件
     */
    public static void writeBeans(List<?> beans, Class<?> beanClass, File file) {
        ExcelBeanMeta meta = resolveMeta(beans, beanClass);
        File actualFile = resolveOutputFile(file, meta);
        if (meta == null || !meta.isAnnotated()) {
            write(toMaps(beans), actualFile);
            return;
        }

        if (actualFile == null) {
            throw new IllegalArgumentException("Excel 文件不能为 null");
        }
        try (OutputStream outputStream = Files.newOutputStream(actualFile.toPath())) {
            PoiExcelWriter writer = new PoiExcelWriter("Sheet1", ExcelVersion.fromFileName(actualFile.getName()));
            writer.write(toMaps(beans, meta), outputStream, meta.getColumns(), meta.getHeaderRowIndex());
        } catch (IOException e) {
            throw new ExcelException("写入 Excel 文件失败：" + actualFile.getAbsolutePath(), e);
        }
    }

    /**
     * 将 Bean 列表写入文件。
     *
     * @param beans Bean 列表
     * @param filePath 目标文件路径
     */
    public static void writeBeans(List<?> beans, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Excel 文件路径不能为空");
        }
        writeBeans(beans, new File(filePath));
    }

    /**
     * 将 Bean 列表按指定 Bean 类型写入文件。
     *
     * @param beans Bean 列表
     * @param beanClass Bean 类型
     * @param filePath 目标文件路径
     */
    public static void writeBeans(List<?> beans, Class<?> beanClass, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Excel 文件路径不能为空");
        }
        writeBeans(beans, beanClass, new File(filePath));
    }

    private static <T> List<T> toBeans(List<Map<String, Object>> data, Class<T> beanClass, ExcelBeanMeta meta) {
        List<T> beans = new ArrayList<>(data == null ? 0 : data.size());
        if (data == null || data.isEmpty()) {
            return beans;
        }
        for (Map<String, Object> row : data) {
            Map<String, Object> actualRow = meta != null && meta.isAnnotated() ? meta.toPropertyMap(row) : row;
            beans.add(Beans.mapToBean(actualRow, beanClass));
        }
        return beans;
    }

    private static List<Map<String, Object>> toMaps(List<?> beans) {
        List<Map<String, Object>> maps = new ArrayList<>(beans == null ? 0 : beans.size());
        if (beans == null || beans.isEmpty()) {
            return maps;
        }
        for (Object bean : beans) {
            if (bean != null) {
                maps.add(Beans.toMap(bean));
            }
        }
        return maps;
    }

    private static List<Map<String, Object>> toMaps(List<?> beans, ExcelBeanMeta meta) {
        List<Map<String, Object>> maps = new ArrayList<>(beans == null ? 0 : beans.size());
        if (beans == null || beans.isEmpty()) {
            return maps;
        }
        for (Object bean : beans) {
            if (bean != null) {
                Map<String, Object> propertyMap = Beans.toMap(bean);
                maps.add(toColumnMap(propertyMap, meta));
            }
        }
        return maps;
    }

    private static Map<String, Object> toColumnMap(Map<String, Object> propertyMap, ExcelBeanMeta meta) {
        Map<String, Object> result = new java.util.LinkedHashMap<>(meta.getColumns().size());
        for (ExcelColumn column : meta.getColumns()) {
            result.put(column.getColumnName(), propertyMap.get(column.getPropertyName()));
        }
        return result;
    }

    private static ExcelBeanMeta resolveMeta(List<?> beans) {
        if (beans == null || beans.isEmpty()) {
            return null;
        }
        for (Object bean : beans) {
            if (bean != null) {
                return ExcelBeanMeta.of(bean.getClass());
            }
        }
        return null;
    }

    private static ExcelBeanMeta resolveMeta(List<?> beans, Class<?> beanClass) {
        ExcelBeanMeta meta = resolveMeta(beans);
        if (meta != null) {
            return meta;
        }
        if (beanClass == null) {
            return null;
        }
        return ExcelBeanMeta.of(beanClass);
    }

    private static File resolveOutputFile(File file, ExcelBeanMeta meta) {
        if (file == null || meta == null || meta.getFileName().trim().isEmpty()) {
            return file;
        }
        if (file.exists() && file.isDirectory()) {
            return new File(file, normalizeFileName(meta.getFileName()));
        }
        return file;
    }

    private static String normalizeFileName(String fileName) {
        String trimmed = fileName.trim();
        String lower = trimmed.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) {
            return trimmed;
        }
        return trimmed + ".xlsx";
    }
}
