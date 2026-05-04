package xyz.ytora.toolkit.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IosTest {

    @Test
    void shouldReadBytesAndStringFromInputStream() {
        byte[] bytes = "你好，ytora".getBytes(StandardCharsets.UTF_8);
        InputStream input = new ByteArrayInputStream(bytes);

        assertArrayEquals(bytes, Ios.toByteArray(input));
        assertEquals("你好，ytora", Ios.toString(new ByteArrayInputStream(bytes)));
        assertEquals("abc", Ios.toString(new ByteArrayInputStream("abc".getBytes(StandardCharsets.ISO_8859_1)),
                StandardCharsets.ISO_8859_1));
        assertNull(Ios.toByteArray(null));
        assertNull(Ios.toString((InputStream) null));
    }

    @Test
    void shouldReadStringFromReader() {
        Reader reader = Ios.toReader("hello");

        assertEquals("hello", Ios.toString(reader));
        assertNull(Ios.toReader(null));
        assertNull(Ios.toString((Reader) null));
    }

    @Test
    void shouldConvertToInputStreamAndReader() {
        assertEquals("abc", Ios.toString(Ios.toInputStream("abc")));
        assertEquals("你好", Ios.toString(Ios.toInputStream("你好", StandardCharsets.UTF_8)));
        assertNull(Ios.toInputStream((String) null));
        assertNull(Ios.toInputStream((byte[]) null));
    }

    @Test
    void shouldCopyStreamAndReader() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        long byteCount = Ios.copy(new ByteArrayInputStream("abcd".getBytes(StandardCharsets.UTF_8)), output);

        assertEquals(4L, byteCount);
        assertEquals("abcd", new String(output.toByteArray(), StandardCharsets.UTF_8));

        StringWriter writer = new StringWriter();
        long charCount = Ios.copy(Ios.toReader("你好"), writer);

        assertEquals(2L, charCount);
        assertEquals("你好", writer.toString());
    }

    @Test
    void shouldWriteBytesAndString() {
        ByteArrayOutputStream output1 = new ByteArrayOutputStream();
        Ios.write("abc", output1);
        assertEquals("abc", new String(output1.toByteArray(), StandardCharsets.UTF_8));

        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        Ios.write("你好", output2, StandardCharsets.UTF_8);
        assertEquals("你好", new String(output2.toByteArray(), StandardCharsets.UTF_8));

        StringWriter writer = new StringWriter();
        Ios.write("writer", writer);
        assertEquals("writer", writer.toString());
    }

    @Test
    void shouldClose() {
        Ios.close(new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException("close error");
            }
        });

        Ios.close(null);
    }

    @Test
    void shouldRejectInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> Ios.toString(Ios.toInputStream("abc"), null));
        assertThrows(IllegalArgumentException.class, () -> Ios.toInputStream("abc", null));
        assertThrows(IllegalArgumentException.class, () -> Ios.copy((InputStream) null, new ByteArrayOutputStream()));
        assertThrows(IllegalArgumentException.class, () -> Ios.copy(new ByteArrayInputStream(new byte[0]), null));
        assertThrows(IllegalArgumentException.class, () -> Ios.copy((Reader) null, new StringWriter()));
        assertThrows(IllegalArgumentException.class, () -> Ios.write("abc", (ByteArrayOutputStream) null));
        assertThrows(IllegalArgumentException.class, () -> Ios.write("abc", (StringWriter) null));
    }
}
