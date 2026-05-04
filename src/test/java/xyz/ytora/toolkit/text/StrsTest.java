package xyz.ytora.toolkit.text;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrsTest {

    @Test
    void shouldCheckEmptyState() {
        assertTrue(Strs.isEmpty(null));
        assertTrue(Strs.isEmpty(""));
        assertFalse(Strs.isEmpty(" "));
        assertTrue(Strs.isNotEmpty("abc"));
        assertFalse(Strs.isNotEmpty(""));
    }

    @Test
    void shouldCheckBlankState() {
        assertTrue(Strs.isBlank(null));
        assertTrue(Strs.isBlank(""));
        assertTrue(Strs.isBlank(" \t "));
        assertFalse(Strs.isBlank("abc"));
        assertTrue(Strs.isNotBlank("abc"));
        assertFalse(Strs.isNotBlank("   "));
    }

    @Test
    void shouldConvertNullAndEmpty() {
        assertEquals("", Strs.nullToEmpty(null));
        assertEquals("abc", Strs.nullToEmpty("abc"));
        assertNull(Strs.emptyToNull(""));
        assertEquals("abc", Strs.emptyToNull("abc"));
    }

    @Test
    void shouldTrimStrings() {
        assertNull(Strs.trim(null));
        assertEquals("abc", Strs.trim(" abc "));
        assertNull(Strs.trimToNull("   "));
        assertEquals("abc", Strs.trimToNull(" abc "));
        assertEquals("", Strs.trimToEmpty(null));
        assertEquals("abc", Strs.trimToEmpty(" abc "));
    }

    @Test
    void shouldApplyDefaultValues() {
        assertEquals("default", Strs.defaultIfEmpty("", "default"));
        assertEquals("value", Strs.defaultIfEmpty("value", "default"));
        assertEquals("default", Strs.defaultIfBlank("   ", "default"));
        assertEquals("value", Strs.defaultIfBlank("value", "default"));
    }

    @Test
    void shouldCompareStringsSafely() {
        assertTrue(Strs.equals(null, null));
        assertFalse(Strs.equals(null, "abc"));
        assertTrue(Strs.equals("abc", "abc"));
        assertTrue(Strs.equalsIgnoreCase("ABC", "abc"));
        assertFalse(Strs.equalsIgnoreCase("abc", "abd"));
    }

    @Test
    void shouldCheckContains() {
        assertTrue(Strs.contains("abcdef", "cd"));
        assertFalse(Strs.contains("abcdef", "xy"));
        assertFalse(Strs.contains(null, "a"));
        assertTrue(Strs.containsIgnoreCase("AbCdEf", "cde"));
        assertFalse(Strs.containsIgnoreCase("abcdef", "xyz"));
    }

    @Test
    void shouldCheckPrefixAndSuffix() {
        assertTrue(Strs.startsWith("abcdef", "abc"));
        assertFalse(Strs.startsWith("abcdef", "ABC"));
        assertTrue(Strs.startsWithIgnoreCase("abcdef", "ABC"));
        assertTrue(Strs.endsWith("abcdef", "def"));
        assertFalse(Strs.endsWith("abcdef", "DEF"));
        assertTrue(Strs.endsWithIgnoreCase("abcdef", "DEF"));
    }

    @Test
    void shouldExtractSubstrings() {
        assertEquals("abc", Strs.substringBefore("abc-def", "-"));
        assertEquals("def", Strs.substringAfter("abc-def", "-"));
        assertEquals("abc-def", Strs.substringBefore("abc-def", "/"));
        assertEquals("", Strs.substringAfter("abc-def", "/"));
        assertEquals("abc-def", Strs.substringBeforeLast("abc-def-ghi", "-"));
        assertEquals("ghi", Strs.substringAfterLast("abc-def-ghi", "-"));
        assertEquals("name", Strs.substringBetween("user[name]", "[", "]"));
        assertNull(Strs.substringBetween("user[name", "[", "]"));
    }

    @Test
    void shouldRemovePrefixAndSuffix() {
        assertEquals("value", Strs.removePrefix("pre-value", "pre-"));
        assertEquals("value", Strs.removePrefix("value", "pre-"));
        assertNull(Strs.removePrefix(null, "pre-"));

        assertEquals("report", Strs.removeSuffix("report.txt", ".txt"));
        assertEquals("report", Strs.removeSuffix("report", ".txt"));
        assertNull(Strs.removeSuffix(null, ".txt"));
    }

    @Test
    void shouldRepeatStrings() {
        assertEquals("", Strs.repeat("abc", 0));
        assertEquals("", Strs.repeat("", 3));
        assertEquals("abcabcabc", Strs.repeat("abc", 3));
        assertThrows(IllegalArgumentException.class, () -> Strs.repeat("abc", -1));
    }

    @Test
    void shouldJoinValues() {
        assertEquals("a,b,c", Strs.join(Arrays.asList("a", "b", "c"), ","));
        assertEquals("a,,c", Strs.join(Arrays.asList("a", null, "c"), ","));
        assertEquals("a-b-c", Strs.join(new Object[]{"a", "b", "c"}, "-"));
        assertEquals("ab", Strs.join(new Object[]{"a", "b"}, null));
        assertEquals("", Strs.join((Iterable<?>) null, ","));
        assertEquals("", Strs.join((Object[]) null, ","));
    }

    @Test
    void shouldSplitToList() {
        assertEquals(Arrays.asList("a", "b", "c"), Strs.splitToList("a,b,c", ","));
        assertEquals(Arrays.asList("a", "", "c"), Strs.splitToList("a,,c", ","));
        assertEquals(Collections.singletonList("abc"), Strs.splitToList("abc", ","));
        assertEquals(Collections.emptyList(), Strs.splitToList(null, ","));
        assertThrows(IllegalArgumentException.class, () -> Strs.splitToList("abc", ""));
    }

    @Test
    void shouldSplitAndTrim() {
        assertEquals(Arrays.asList("a", "b", "c"), Strs.splitAndTrim(" a , b , c ", ","));
        assertEquals(Arrays.asList("a", "", "c"), Strs.splitAndTrim(" a ,   , c ", ","));
        assertEquals(Arrays.asList("a", "c"), Strs.splitAndTrimIgnoreEmpty(" a ,   , c ", ","));
        assertEquals(Collections.emptyList(), Strs.splitAndTrim(null, ","));
        assertEquals(Collections.emptyList(), Strs.splitAndTrimIgnoreEmpty("   ,   ", ","));
    }

    @Test
    void shouldFormatString() {
        assertEquals("你好，ytora", Strs.format("你好，{}", "ytora"));
        assertEquals("1 + 2 = 3", Strs.format("{} + {} = {}", 1, 2, 3));
        assertEquals("只有一个参数：A，保留：{}", Strs.format("只有一个参数：{}，保留：{}", "A"));
        assertEquals("没有占位符", Strs.format("没有占位符"));
        assertNull(Strs.format(null, "A"));
    }

    @Test
    void shouldFormatNamedString() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("name", "ytora");
        args.put("count", 3);
        args.put("nullable", null);

        assertEquals("你好，ytora", Strs.formatNamed("你好，{name}", args));
        assertEquals("ytora 有 3 条消息", Strs.formatNamed("{name} 有 {count} 条消息", args));
        assertEquals("缺失参数：{missing}", Strs.formatNamed("缺失参数：{missing}", args));
        assertEquals("空值：null", Strs.formatNamed("空值：{nullable}", args));
        assertEquals("没有命名占位符", Strs.formatNamed("没有命名占位符", args));
        assertNull(Strs.formatNamed(null, args));
        assertEquals("{name}", Strs.formatNamed("{name}", Collections.<String, Object>emptyMap()));
    }

    @Test
    void shouldTruncateStrings() {
        assertEquals("abcd", Strs.truncate("abcdef", 4));
        assertEquals("abc", Strs.truncate("abc", 4));
        assertNull(Strs.truncate(null, 4));

        assertEquals("abc..", Strs.truncate("abcdef", 5, ".."));
        assertEquals("abc", Strs.truncate("abc", 5, ".."));
        assertEquals("abcde", Strs.truncate("abcdef", 5, null));
        assertThrows(IllegalArgumentException.class, () -> Strs.truncate("abc", -1));
        assertThrows(IllegalArgumentException.class, () -> Strs.truncate("abcdef", 1, ".."));
    }

    @Test
    void shouldChangeCharacterCase() {
        assertEquals("Hello", Strs.capitalize("hello"));
        assertEquals("hello", Strs.uncapitalize("Hello"));
        assertEquals("", Strs.capitalize(""));
        assertNull(Strs.uncapitalize(null));
    }

    @Test
    void shouldLeftPadStrings() {
        assertEquals("007", Strs.leftPad("7", 3, '0'));
        assertEquals("abc", Strs.leftPad("abc", 2, '0'));
        assertNull(Strs.leftPad(null, 2, '0'));
        assertThrows(IllegalArgumentException.class, () -> Strs.leftPad("7", -1, '0'));
    }

    @Test
    void shouldCheckNumericAndAlphanumeric() {
        assertTrue(Strs.isNumeric("00123"));
        assertFalse(Strs.isNumeric("12a3"));
        assertFalse(Strs.isNumeric(""));
        assertTrue(Strs.isAlphanumeric("abc123"));
        assertFalse(Strs.isAlphanumeric("abc-123"));
        assertFalse(Strs.isAlphanumeric(null));
    }

    @Test
    void shouldCountMatchesAndNormalizeWhitespace() {
        assertEquals(1, Strs.countMatches("ababa", "aba"));
        assertEquals(2, Strs.countMatches("a,b,c", ","));
        assertEquals(0, Strs.countMatches("abc", ""));
        assertEquals(0, Strs.countMatches(null, ","));

        assertEquals("a b c", Strs.normalizeWhitespace("  a\t b \n c  "));
        assertEquals("", Strs.normalizeWhitespace("   "));
        assertNull(Strs.normalizeWhitespace(null));
    }

    @Test
    void shouldGenerateRandomNumber() {
        String value = Strs.randomNumber(32);

        assertNotNull(value);
        assertEquals(32, value.length());
        assertTrue(value.matches("\\d+"));
    }

    @Test
    void shouldHandleRandomNumberEdgeCases() {
        assertEquals("", Strs.randomNumber(0));
        assertThrows(IllegalArgumentException.class, () -> Strs.randomNumber(-1));
    }

    @Test
    void shouldGenerateRandomString() {
        String value = Strs.randomString(32);

        assertNotNull(value);
        assertEquals(32, value.length());
        assertTrue(value.matches("[0-9A-Za-z]+"));
    }

    @Test
    void shouldConvertNamingStyle() {
        assertEquals("userName", Strs.toCamelCase("user_name"));
        assertEquals("userName", Strs.toCamelCase("user-name"));
        assertEquals("userName", Strs.toCamelCase("USER_NAME"));
        assertEquals("user_name", Strs.toSnakeCase("userName"));
        assertEquals("http_server_url", Strs.toSnakeCase("HTTPServerURL"));
        assertEquals("user-name", Strs.toKebabCase("userName"));
        assertEquals("http-server-url", Strs.toKebabCase("HTTPServerURL"));
    }
}
