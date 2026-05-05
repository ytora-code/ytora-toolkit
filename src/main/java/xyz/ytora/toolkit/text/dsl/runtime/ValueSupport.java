package xyz.ytora.toolkit.text.dsl.runtime;

import xyz.ytora.toolkit.text.dsl.exception.*;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 值处理工具。
 *
 * <p>这里集中处理类型判断、访问规则和字符串渲染，避免逻辑散落在解析器和节点中。</p>
 */
public final class ValueSupport {

    private ValueSupport() {
    }

    public static Object resolveRootValue(Object rootObject, String name, AccessMode accessMode) {
        if (rootObject == null) {
            return onMissingVariable(name, accessMode);
        }
        Object result = accessInternal(rootObject, name, accessMode, true);
        if (result == null && accessMode == AccessMode.STRICT && !containsRootValue(rootObject, name)) {
            throw new UnknownVariableException("变量不存在: " + name);
        }
        return result;
    }

    public static Object access(Object target, Object member, AccessMode accessMode) {
        if (target == null) {
            return onTypeMismatch("不能在 null 上继续访问成员", accessMode);
        }
        return accessInternal(target, member, accessMode, false);
    }

    public static boolean containsRootValue(Object rootObject, String name) {
        if (rootObject == null) {
            return false;
        }
        if (rootObject instanceof Map) {
            return ((Map<?, ?>) rootObject).containsKey(name);
        }
        return findGetter(rootObject.getClass(), name) != null || findField(rootObject.getClass(), name) != null;
    }

    private static Object accessInternal(Object target, Object member, AccessMode accessMode, boolean rootLookup) {
        if (target instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) target;
            String key = String.valueOf(member);
            if (map.containsKey(key)) {
                return map.get(key);
            }
            return rootLookup ? onMissingVariable(key, accessMode) : onMissingMember(key, accessMode);
        }
        if (target instanceof List) {
            int index = toIndex(member, accessMode);
            List<?> list = (List<?>) target;
            if (index >= 0 && index < list.size()) {
                return list.get(index);
            }
            return onIndexError(index, accessMode);
        }
        if (target.getClass().isArray()) {
            int index = toIndex(member, accessMode);
            int length = Array.getLength(target);
            if (index >= 0 && index < length) {
                return Array.get(target, index);
            }
            return onIndexError(index, accessMode);
        }
        if (member instanceof Number || isNumberText(String.valueOf(member))) {
            return onTypeMismatch("类型 " + target.getClass().getName() + " 不支持数字下标访问", accessMode);
        }
        String property = String.valueOf(member);
        Method getter = findGetter(target.getClass(), property);
        if (getter != null) {
            try {
                return getter.invoke(target);
            } catch (Exception ex) {
                throw new DslEvaluationException("访问 getter 失败: " + property, ex);
            }
        }
        Field field = findField(target.getClass(), property);
        if (field != null) {
            try {
                field.setAccessible(true);
                return field.get(target);
            } catch (Exception ex) {
                throw new DslEvaluationException("访问字段失败: " + property, ex);
            }
        }
        return rootLookup ? onMissingVariable(property, accessMode) : onMissingMember(property, accessMode);
    }

    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).isEmpty();
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        return false;
    }

    public static boolean isTruthy(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        throw new TypeMismatchException("期望得到布尔值，实际得到: " + typeOf(value));
    }

    public static BigDecimal requireNumber(Object value, String message) {
        if (value instanceof Number) {
            return new BigDecimal(String.valueOf(value));
        }
        throw new TypeMismatchException(message + ", actual: " + typeOf(value));
    }

    public static int requireInteger(Object value, String message) {
        BigDecimal number = requireNumber(value, message);
        try {
            return number.intValueExact();
        } catch (ArithmeticException ex) {
            throw new TypeMismatchException(message + "，实际不是整数: " + number);
        }
    }

    public static String requireString(Object value, String message) {
        if (value instanceof String) {
            return (String) value;
        }
        throw new TypeMismatchException(message + "，实际类型: " + typeOf(value));
    }

    @SuppressWarnings("unchecked")
    public static List<Object> requireSequence(Object value, String message) {
        if (value instanceof List) {
            return (List<Object>) value;
        }
        if (value != null && value.getClass().isArray()) {
            List<Object> list = new ArrayList<Object>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                list.add(Array.get(value, i));
            }
            return list;
        }
        throw new TypeMismatchException(message + "，实际类型: " + typeOf(value));
    }

    public static String stringifyRenderValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        throw new RenderValueException("复合类型不能直接渲染，请先显式转换");
    }

    public static boolean equalsValue(Object left, Object right) {
        if (left == null || right == null) {
            return left == right;
        }
        if (left instanceof Number && right instanceof Number) {
            return requireNumber(left, "非法数值").compareTo(requireNumber(right, "非法数值")) == 0;
        }
        return left.equals(right);
    }

    public static String join(List<?> values, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value instanceof Map || value instanceof Collection || (value != null && value.getClass().isArray())) {
                throw new TypeMismatchException("join 不支持拼接复合类型元素，问题下标: " + i);
            }
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(value == null ? "" : String.valueOf(value));
        }
        return builder.toString();
    }

    public static BigDecimal divide(BigDecimal left, BigDecimal right) {
        if (BigDecimal.ZERO.compareTo(right) == 0) {
            throw new DslEvaluationException("除数不能为 0");
        }
        return left.divide(right, 16, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public static BigDecimal remainder(BigDecimal left, BigDecimal right) {
        if (BigDecimal.ZERO.compareTo(right) == 0) {
            throw new DslEvaluationException("取余时除数不能为 0");
        }
        return left.remainder(right);
    }

    private static Method findGetter(Class<?> type, String property) {
        String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        try {
            return type.getMethod("get" + suffix);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            return type.getMethod("is" + suffix);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String property) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(property);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static int toIndex(Object member, AccessMode accessMode) {
        if (member instanceof Number) {
            return ((Number) member).intValue();
        }
        String text = String.valueOf(member);
        if (isNumberText(text)) {
            return Integer.parseInt(text);
        }
        if (accessMode == AccessMode.STRICT) {
            throw new IndexAccessException("期望得到数组下标，实际得到: " + member);
        }
        return -1;
    }

    private static boolean isNumberText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static Object onMissingVariable(String name, AccessMode accessMode) {
        if (accessMode == AccessMode.STRICT) {
            throw new UnknownVariableException("变量不存在: " + name);
        }
        return null;
    }

    private static Object onMissingMember(String name, AccessMode accessMode) {
        if (accessMode == AccessMode.STRICT) {
            throw new UnknownMemberException("字段不存在: " + name);
        }
        return null;
    }

    private static Object onIndexError(int index, AccessMode accessMode) {
        if (accessMode == AccessMode.STRICT) {
            throw new IndexAccessException("数组下标越界: " + index);
        }
        return null;
    }

    private static Object onTypeMismatch(String message, AccessMode accessMode) {
        if (accessMode == AccessMode.STRICT) {
            throw new TypeMismatchException(message);
        }
        return null;
    }

    private static String typeOf(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }
}
