package xyz.ytora.toolkit.text;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegsTest {

    @Test
    void shouldMatchByRegexString() {
        assertTrue(Regs.matches("abc123", "^[a-z0-9]+$"));
        assertFalse(Regs.matches("abc-123", "^[a-z0-9]+$"));
        assertFalse(Regs.matches(null, ".*"));
        assertFalse(Regs.matches("abc", (String) null));
    }

    @Test
    void shouldMatchByPattern() {
        Pattern pattern = Pattern.compile("^[A-Z]{2}\\d{3}$");

        assertTrue(Regs.matches("AB123", pattern));
        assertFalse(Regs.matches("ab123", pattern));
        assertFalse(Regs.matches("AB123", (Pattern) null));
    }

    @Test
    void shouldCheckChineseAndNumbers() {
        assertTrue(Regs.isChinese("你好世界"));
        assertFalse(Regs.isChinese("你好123"));

        assertTrue(Regs.isNumber("2024"));
        assertFalse(Regs.isNumber("-12"));

        assertTrue(Regs.isInteger("-12"));
        assertFalse(Regs.isInteger("12.5"));

        assertTrue(Regs.isDecimal("12.5"));
        assertTrue(Regs.isDecimal(".75"));
        assertFalse(Regs.isDecimal("12a"));
    }

    @Test
    void shouldCheckLettersAndAlphanumeric() {
        assertTrue(Regs.isLetter("AbCd"));
        assertFalse(Regs.isLetter("Ab12"));

        assertTrue(Regs.isAlphanumeric("Ab12"));
        assertFalse(Regs.isAlphanumeric("Ab-12"));
    }

    @Test
    void shouldCheckCommonStructuredValues() {
        assertTrue(Regs.isDate("2024-02-29"));
        assertFalse(Regs.isDate("2023-02-29"));
        assertFalse(Regs.isDate("2024-04-31"));
        assertFalse(Regs.isDate("2024-2-29"));
        assertFalse(Regs.isDate("2024/02/29"));

        assertTrue(Regs.isDateTime("2024-02-29 23:59:59"));
        assertFalse(Regs.isDateTime("2023-02-29 23:59:59"));
        assertFalse(Regs.isDateTime("2024-04-31 12:00:00"));
        assertFalse(Regs.isDateTime("2024-02-29 24:00:00"));
        assertFalse(Regs.isDateTime("2024-02-29 23:60:00"));
        assertFalse(Regs.isDateTime("2024-02-29T23:59:59"));

        assertTrue(Regs.isEmail("user@example.com"));
        assertTrue(Regs.isEmail("user.name+tag@example.co.uk"));
        assertFalse(Regs.isEmail("user@@example.com"));
        assertFalse(Regs.isEmail(".user@example.com"));
        assertFalse(Regs.isEmail("user@example..com"));
        assertFalse(Regs.isEmail("user@-example.com"));

        assertTrue(Regs.isMobile("13800138000"));
        assertFalse(Regs.isMobile("12800138000"));
        assertFalse(Regs.isMobile("1380013800"));
        assertTrue(Regs.isLandline("010-88886666"));
        assertTrue(Regs.isLandline("057188886666"));
        assertTrue(Regs.isLandline("88886666"));
        assertTrue(Regs.isLandline("400-123-4567"));
        assertFalse(Regs.isLandline("12345"));
        assertTrue(Regs.isPhone("13800138000"));
        assertTrue(Regs.isPhone("010-88886666"));
        assertTrue(Regs.isPhone("400-123-4567"));
        assertFalse(Regs.isPhone("12345"));

        assertTrue(Regs.isIdCard("11010519491231002X"));
        assertTrue(Regs.isIdCard("110105200002290021"));
        assertTrue(Regs.isIdCard("130503670401001"));
        assertFalse(Regs.isIdCard("11010519491331002X"));
        assertFalse(Regs.isIdCard("110105194912310021"));
        assertFalse(Regs.isIdCard("110105200102290021"));
        assertFalse(Regs.isIdCard("130503670431001"));
        assertTrue(Regs.isIdCard18("11010519491231002X"));
        assertFalse(Regs.isIdCard18("130503670401001"));
        assertTrue(Regs.isIdCard15("130503670401001"));
        assertFalse(Regs.isIdCard15("11010519491231002X"));

        assertTrue(Regs.isUrl("https://example.com"));
        assertTrue(Regs.isUrl("https://example.com/path?q=1"));
        assertFalse(Regs.isUrl("ftp://example.com"));
        assertFalse(Regs.isUrl("https:///path-only"));
        assertFalse(Regs.isUrl("https://exa mple.com"));

        assertTrue(Regs.isIpv4("192.168.1.1"));
        assertFalse(Regs.isIpv4("256.168.1.1"));

        assertTrue(Regs.isUuid("550e8400-e29b-41d4-a716-446655440000"));
        assertFalse(Regs.isUuid("not-a-uuid"));
    }
}
