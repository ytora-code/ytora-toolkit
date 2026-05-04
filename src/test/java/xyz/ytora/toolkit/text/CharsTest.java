package xyz.ytora.toolkit.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CharsTest {

    @Test
    void shouldCheckAsciiKinds() {
        assertTrue(Chars.isAscii('A'));
        assertFalse(Chars.isAscii('中'));

        assertTrue(Chars.isAsciiLetter('A'));
        assertTrue(Chars.isAsciiLetter('z'));
        assertFalse(Chars.isAsciiLetter('1'));

        assertTrue(Chars.isAsciiUpperCase('A'));
        assertFalse(Chars.isAsciiUpperCase('a'));

        assertTrue(Chars.isAsciiLowerCase('a'));
        assertFalse(Chars.isAsciiLowerCase('A'));

        assertTrue(Chars.isAsciiDigit('7'));
        assertFalse(Chars.isAsciiDigit('x'));
    }

    @Test
    void shouldCheckChineseAndWhitespace() {
        assertTrue(Chars.isChinese('中'));
        assertFalse(Chars.isChinese('A'));

        assertTrue(Chars.isWhitespace(' '));
        assertTrue(Chars.isWhitespace('\n'));
        assertFalse(Chars.isWhitespace('x'));
    }

    @Test
    void shouldConvertWidthForChar() {
        assertEquals('A', Chars.toHalfWidth('Ａ'));
        assertEquals('1', Chars.toHalfWidth('１'));
        assertEquals(' ', Chars.toHalfWidth('\u3000'));
        assertEquals('中', Chars.toHalfWidth('中'));

        assertEquals('Ａ', Chars.toFullWidth('A'));
        assertEquals('１', Chars.toFullWidth('1'));
        assertEquals('\u3000', Chars.toFullWidth(' '));
        assertEquals('中', Chars.toFullWidth('中'));
    }

    @Test
    void shouldConvertWidthForString() {
        assertEquals("ABC 123", Chars.toHalfWidth("ＡＢＣ　１２３"));
        assertEquals("ＡＢＣ　１２３", Chars.toFullWidth("ABC 123"));
        assertNull(Chars.toHalfWidth(null));
        assertNull(Chars.toFullWidth(null));
    }
}
