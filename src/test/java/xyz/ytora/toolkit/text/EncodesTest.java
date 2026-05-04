package xyz.ytora.toolkit.text;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncodesTest {

    @Test
    void shouldEncodeAndDecodeUrl() {
        assertEquals("a+b%2Bc", Encodes.urlEncode("a b+c"));
        assertEquals("你好", Encodes.urlDecode("%E4%BD%A0%E5%A5%BD"));
        assertEquals("a b+c", Encodes.urlDecode("a+b%2Bc"));
        assertEquals("%E4%BD%A0%E5%A5%BD", Encodes.urlEncode("你好", StandardCharsets.UTF_8));
        assertNull(Encodes.urlEncode(null));
        assertNull(Encodes.urlDecode(null));
    }

    @Test
    void shouldRejectInvalidUrlEncoding() {
        assertThrows(IllegalArgumentException.class, () -> Encodes.urlDecode("%"));
        assertThrows(IllegalArgumentException.class, () -> Encodes.urlDecode("%ZZ"));
        assertThrows(IllegalArgumentException.class, () -> Encodes.urlDecode("你"));
    }

    @Test
    void shouldEncodeAndDecodeBase64() {
        assertEquals("eXRvcmE=", Encodes.base64Encode("ytora"));
        assertEquals("你好", Encodes.base64DecodeToString("5L2g5aW9"));
        assertEquals("eXRvcmE=", Encodes.base64Encode("ytora", StandardCharsets.UTF_8));
        assertEquals("ytora", Encodes.base64DecodeToString("eXRvcmE=", StandardCharsets.UTF_8));
        assertNull(Encodes.base64Encode(null));
        assertNull(Encodes.base64DecodeToString(null));
    }

    @Test
    void shouldEscapeAndUnescapeHtml() {
        assertEquals("&lt;div class=&quot;x&quot;&gt;&amp;&#39;&lt;/div&gt;",
                Encodes.htmlEscape("<div class=\"x\">&'</div>"));
        assertEquals("<div>&\"'</div>",
                Encodes.htmlUnescape("&lt;div&gt;&amp;&quot;&#39;&lt;/div&gt;"));
        assertEquals("A A", Encodes.htmlUnescape("&#65; &#x41;"));
        assertEquals("&unknown;", Encodes.htmlUnescape("&unknown;"));
        assertNull(Encodes.htmlEscape(null));
        assertNull(Encodes.htmlUnescape(null));
    }
}
