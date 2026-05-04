package xyz.ytora.toolkit.io;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * IO 工具类。
 *
 * <p>提供常用的流读取、写入、复制和资源关闭方法。</p>
 * <p>该类仅包含静态方法，不应被实例化。</p>
 *
 * @author ytora
 * @since 1.0-SNAPSHOT
 */
public final class Ios {

    /**
     * 默认缓冲区大小。
     */
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private Ios() {
        throw new AssertionError("工具类不允许实例化");
    }

    /**
     * 复制输入流到输出流。
     *
     * <p>输入流和输出流都不会被自动关闭。</p>
     *
     * @param input 输入流
     * @param output 输出流
     * @return 实际复制的字节数
     */
    public static long copy(InputStream input, OutputStream output) {
        requireInputOutput(input, output);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0L;
        int read;
        try {
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
                count += read;
            }
            return count;
        } catch (IOException ex) {
            throw new IllegalStateException("复制字节流失败", ex);
        }
    }

    /**
     * 复制字符流到字符流。
     *
     * <p>读取器和写入器都不会被自动关闭。</p>
     *
     * @param reader 读取器
     * @param writer 写入器
     * @return 实际复制的字符数
     */
    public static long copy(Reader reader, Writer writer) {
        requireReaderWriter(reader, writer);
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0L;
        int read;
        try {
            while ((read = reader.read(buffer)) >= 0) {
                writer.write(buffer, 0, read);
                count += read;
            }
            return count;
        } catch (IOException ex) {
            throw new IllegalStateException("复制字符流失败", ex);
        }
    }

    /**
     * 安静关闭资源。
     *
     * <p>关闭失败时忽略异常。</p>
     *
     * @param closeable 可关闭资源
     */
    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * 读取输入流中的全部字节。
     *
     * <p>输入流不会被自动关闭。</p>
     *
     * @param input 输入流
     * @return 全部字节内容；输入为 {@code null} 时返回 {@code null}
     */
    public static byte[] toByteArray(InputStream input) {
        if (input == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    /**
     * 读取输入流中的全部文本，使用 UTF-8 编码。
     *
     * <p>输入流不会被自动关闭。</p>
     *
     * @param input 输入流
     * @return 全部文本内容；输入为 {@code null} 时返回 {@code null}
     */
    public static String toString(InputStream input) {
        return toString(input, StandardCharsets.UTF_8);
    }

    /**
     * 读取输入流中的全部文本。
     *
     * <p>输入流不会被自动关闭。</p>
     *
     * @param input 输入流
     * @param charset 字符集
     * @return 全部文本内容；输入为 {@code null} 时返回 {@code null}
     */
    public static String toString(InputStream input, Charset charset) {
        if (input == null) {
            return null;
        }
        Charset actualCharset = requireCharset(charset);
        return new String(toByteArray(input), actualCharset);
    }

    /**
     * 读取字符流中的全部文本。
     *
     * <p>字符流不会被自动关闭。</p>
     *
     * @param reader 字符流
     * @return 全部文本内容；输入为 {@code null} 时返回 {@code null}
     */
    public static String toString(Reader reader) {
        if (reader == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        copy(reader, writer);
        return writer.toString();
    }

    /**
     * 将字节数组包装为输入流。
     *
     * @param bytes 字节数组
     * @return 输入流；输入为 {@code null} 时返回 {@code null}
     */
    public static InputStream toInputStream(byte[] bytes) {
        return bytes == null ? null : new ByteArrayInputStream(bytes);
    }

    /**
     * 将字符串包装为输入流，使用 UTF-8 编码。
     *
     * @param value 文本内容
     * @return 输入流；输入为 {@code null} 时返回 {@code null}
     */
    public static InputStream toInputStream(String value) {
        return toInputStream(value, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串包装为输入流。
     *
     * @param value 文本内容
     * @param charset 字符集
     * @return 输入流；输入为 {@code null} 时返回 {@code null}
     */
    public static InputStream toInputStream(String value, Charset charset) {
        if (value == null) {
            return null;
        }
        Charset actualCharset = requireCharset(charset);
        return new ByteArrayInputStream(value.getBytes(actualCharset));
    }

    /**
     * 将字符串包装为字符流。
     *
     * @param value 文本内容
     * @return 字符流；输入为 {@code null} 时返回 {@code null}
     */
    public static Reader toReader(String value) {
        return value == null ? null : new StringReader(value);
    }

    /**
     * 将字节数组写入输出流。
     *
     * <p>输出流不会被自动关闭。</p>
     *
     * @param bytes 字节数组
     * @param output 输出流
     */
    public static void write(byte[] bytes, OutputStream output) {
        if (bytes == null) {
            return;
        }
        if (output == null) {
            throw new IllegalArgumentException("输出流不能为空");
        }
        try {
            output.write(bytes);
        } catch (IOException ex) {
            throw new IllegalStateException("写入字节流失败", ex);
        }
    }

    /**
     * 将文本写入输出流，使用 UTF-8 编码。
     *
     * <p>输出流不会被自动关闭。</p>
     *
     * @param value 文本内容
     * @param output 输出流
     */
    public static void write(String value, OutputStream output) {
        write(value, output, StandardCharsets.UTF_8);
    }

    /**
     * 将文本写入输出流。
     *
     * <p>输出流不会被自动关闭。</p>
     *
     * @param value 文本内容
     * @param output 输出流
     * @param charset 字符集
     */
    public static void write(String value, OutputStream output, Charset charset) {
        if (value == null) {
            return;
        }
        Charset actualCharset = requireCharset(charset);
        write(value.getBytes(actualCharset), output);
    }

    /**
     * 将文本写入字符流。
     *
     * <p>写入器不会被自动关闭。</p>
     *
     * @param value 文本内容
     * @param writer 写入器
     */
    public static void write(String value, Writer writer) {
        if (value == null) {
            return;
        }
        if (writer == null) {
            throw new IllegalArgumentException("写入器不能为空");
        }
        try {
            writer.write(value);
        } catch (IOException ex) {
            throw new IllegalStateException("写入字符流失败", ex);
        }
    }

    /**
     * 校验字符集不能为空。
     *
     * @param charset 字符集
     * @return 非空字符集
     */
    private static Charset requireCharset(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("字符集不能为空");
        }
        return charset;
    }

    /**
     * 校验输入流和输出流不能为空。
     *
     * @param input 输入流
     * @param output 输出流
     */
    private static void requireInputOutput(InputStream input, OutputStream output) {
        if (input == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }
        if (output == null) {
            throw new IllegalArgumentException("输出流不能为空");
        }
    }

    /**
     * 校验读取器和写入器不能为空。
     *
     * @param reader 读取器
     * @param writer 写入器
     */
    private static void requireReaderWriter(Reader reader, Writer writer) {
        if (reader == null) {
            throw new IllegalArgumentException("读取器不能为空");
        }
        if (writer == null) {
            throw new IllegalArgumentException("写入器不能为空");
        }
    }
}
