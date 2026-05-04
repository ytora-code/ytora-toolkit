package xyz.ytora.toolkit.bean;

import xyz.ytora.toolkit.convert.Converts;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JavaBean 工具类。
 *
 * <p>提供常用的属性拷贝、Bean 与 Map 互转能力。属性匹配遵循 JavaBeans 规范，
 * 以 getter/setter 暴露的属性名为准。</p>
 *
 * @author ytora
 * @since 1.0-SNAPSHOT
 */
public final class Beans {

    private static final ConcurrentMap<Class<?>, Map<String, PropertyMethod>> readablePropertiesCache =
            new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Map<String, PropertyMethod>> writablePropertiesCache =
            new ConcurrentHashMap<>();

    private static final ConcurrentMap<CopyPlanKey, CopyPlan> copyPlanCache =
            new ConcurrentHashMap<>();

    private Beans() {
        throw new AssertionError("不允许实例化工具类");
    }

    /**
     * 将源对象中名称相同且类型兼容的属性拷贝到目标对象。
     *
     * <p>该方法执行浅拷贝：引用类型属性只会复制引用，不会递归复制对象内容。</p>
     *
     * @param source 源对象，不能为 {@code null}
     * @param target 目标对象，不能为 {@code null}
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     */
    public static <S, T> void copyProperties(S source, T target) {
        copyProperties(source, target, (String[]) null);
    }

    /**
     * 将源对象中名称相同且类型兼容的属性拷贝到目标对象，并忽略指定属性。
     *
     * @param source 源对象，不能为 {@code null}
     * @param target 目标对象，不能为 {@code null}
     * @param ignoreProperties 需要忽略的属性名
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     */
    public static <S, T> void copyProperties(S source, T target, String... ignoreProperties) {
        requireNonNull(source, "源对象不能为 null");
        requireNonNull(target, "目标对象不能为 null");

        CopyPlan copyPlan = getCopyPlan(source.getClass(), target.getClass());
        Set<String> ignored = toIgnoreSet(ignoreProperties);
        copyPlan.copy(source, target, ignored);
    }

    /**
     * 创建目标类型实例，并将源对象属性拷贝到新实例。
     *
     * @param source 源对象，不能为 {@code null}
     * @param targetClass 目标类型，必须包含无参构造器
     * @param <T> 目标类型
     * @return 完成属性拷贝的新实例
     */
    public static <T> T copyTo(Object source, Class<T> targetClass) {
        return copyTo(source, targetClass, (String[]) null);
    }

    /**
     * 创建目标类型实例，并将源对象属性拷贝到新实例，同时忽略指定属性。
     *
     * @param source 源对象，不能为 {@code null}
     * @param targetClass 目标类型，必须包含无参构造器
     * @param ignoreProperties 需要忽略的属性名
     * @param <T> 目标类型
     * @return 完成属性拷贝的新实例
     */
    public static <T> T copyTo(Object source, Class<T> targetClass, String... ignoreProperties) {
        requireNonNull(source, "源对象不能为 null");
        requireNonNull(targetClass, "目标类型不能为 null");

        T target = newInstance(targetClass);
        copyProperties(source, target, ignoreProperties);
        return target;
    }

    /**
     * 将 Bean 转换为 Map，包含值为 {@code null} 的属性。
     *
     * @param bean Bean 对象，不能为 {@code null}
     * @return 属性 Map
     */
    public static Map<String, Object> toMap(Object bean) {
        return toMap(bean, false);
    }

    /**
     * 将 Bean 转换为 Map。
     *
     * @param bean Bean 对象，不能为 {@code null}
     * @param ignoreNullValue 是否忽略值为 {@code null} 的属性
     * @return 属性 Map
     */
    public static Map<String, Object> toMap(Object bean, boolean ignoreNullValue) {
        return toMap(bean, ignoreNullValue, (String[]) null);
    }

    /**
     * 将 Bean 转换为 Map，并忽略指定属性。
     *
     * @param bean Bean 对象，不能为 {@code null}
     * @param ignoreNullValue 是否忽略值为 {@code null} 的属性
     * @param ignoreProperties 需要忽略的属性名
     * @return 属性 Map
     */
    public static Map<String, Object> toMap(Object bean, boolean ignoreNullValue, String... ignoreProperties) {
        requireNonNull(bean, "Bean 对象不能为 null");

        Set<String> ignored = toIgnoreSet(ignoreProperties);
        Map<String, PropertyMethod> properties = getReadableProperties(bean.getClass());
        Map<String, Object> result = new LinkedHashMap<>(properties.size());
        for (Map.Entry<String, PropertyMethod> entry : properties.entrySet()) {
            if (ignored.contains(entry.getKey())) {
                continue;
            }
            Object value = entry.getValue().get(bean);
            if (!ignoreNullValue || value != null) {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    /**
     * 创建目标类型实例，并将 Map 中名称匹配的键值写入 Bean。
     *
     * <p>当 Map 值类型与属性类型不一致时，会尝试使用 {@link Converts} 进行类型转换；
     * 转换失败、属性不存在或属性不可写时会跳过该键。</p>
     *
     * @param source 源 Map，不能为 {@code null}
     * @param targetClass 目标类型，必须包含无参构造器
     * @param <T> 目标类型
     * @return 完成属性填充的新实例
     */
    public static <T> T mapToBean(Map<String, ?> source, Class<T> targetClass) {
        return mapToBean(source, targetClass, (String[]) null);
    }

    /**
     * 创建目标类型实例，并将 Map 中名称匹配的键值写入 Bean，同时忽略指定属性。
     *
     * @param source 源 Map，不能为 {@code null}
     * @param targetClass 目标类型，必须包含无参构造器
     * @param ignoreProperties 需要忽略的属性名
     * @param <T> 目标类型
     * @return 完成属性填充的新实例
     */
    public static <T> T mapToBean(Map<String, ?> source, Class<T> targetClass, String... ignoreProperties) {
        requireNonNull(source, "源 Map 不能为 null");
        requireNonNull(targetClass, "目标类型不能为 null");

        T target = newInstance(targetClass);
        copyMapToBean(source, target, ignoreProperties);
        return target;
    }

    /**
     * 将 Map 中名称匹配的键值写入目标 Bean。
     *
     * @param source 源 Map，不能为 {@code null}
     * @param target 目标 Bean，不能为 {@code null}
     */
    public static void copyMapToBean(Map<String, ?> source, Object target) {
        copyMapToBean(source, target, (String[]) null);
    }

    /**
     * 将 Map 中名称匹配的键值写入目标 Bean，并忽略指定属性。
     *
     * @param source 源 Map，不能为 {@code null}
     * @param target 目标 Bean，不能为 {@code null}
     * @param ignoreProperties 需要忽略的属性名
     */
    public static void copyMapToBean(Map<String, ?> source, Object target, String... ignoreProperties) {
        requireNonNull(source, "源 Map 不能为 null");
        requireNonNull(target, "目标 Bean 不能为 null");

        Set<String> ignored = toIgnoreSet(ignoreProperties);
        Map<String, PropertyMethod> properties = getWritableProperties(target.getClass());
        for (Map.Entry<String, PropertyMethod> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            if (ignored.contains(propertyName) || !source.containsKey(propertyName)) {
                continue;
            }

            PropertyMethod property = entry.getValue();
            Object value = convertValue(source.get(propertyName), property.propertyType);
            if (value != null || !property.propertyType.isPrimitive()) {
                property.set(target, value);
            }
        }
    }

    /**
     * 清空 Beans 内部缓存。
     *
     * <p>主要用于热加载、测试或明确需要释放类元数据引用的场景。</p>
     */
    public static void clearCache() {
        readablePropertiesCache.clear();
        writablePropertiesCache.clear();
        copyPlanCache.clear();
        Introspector.flushCaches();
    }

    /**
     * 将基本类型转为包装类型。
     *
     * @param type 类型
     * @return 包装类型；非基本类型原样返回
     */
    public static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return Void.class;
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (isAssignable(targetType, value.getClass())) {
            return value;
        }
        try {
            return Converts.convert(value, wrapPrimitive(targetType));
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static CopyPlan getCopyPlan(Class<?> sourceClass, Class<?> targetClass) {
        CopyPlanKey key = new CopyPlanKey(sourceClass, targetClass);
        CopyPlan copyPlan = copyPlanCache.get(key);
        if (copyPlan != null) {
            return copyPlan;
        }

        CopyPlan created = buildCopyPlan(sourceClass, targetClass);
        CopyPlan previous = copyPlanCache.putIfAbsent(key, created);
        return previous == null ? created : previous;
    }

    private static CopyPlan buildCopyPlan(Class<?> sourceClass, Class<?> targetClass) {
        Map<String, PropertyMethod> sourceProperties = getReadableProperties(sourceClass);
        Map<String, PropertyMethod> targetProperties = getWritableProperties(targetClass);
        List<PropertyCopy> copies = new ArrayList<>();

        for (Map.Entry<String, PropertyMethod> entry : targetProperties.entrySet()) {
            PropertyMethod sourceProperty = sourceProperties.get(entry.getKey());
            if (sourceProperty == null) {
                continue;
            }

            PropertyMethod targetProperty = entry.getValue();
            if (isAssignable(targetProperty.propertyType, sourceProperty.propertyType)) {
                copies.add(new PropertyCopy(entry.getKey(), sourceProperty.method, targetProperty.method));
            }
        }

        return new CopyPlan(copies);
    }

    private static Map<String, PropertyMethod> getReadableProperties(Class<?> beanClass) {
        Map<String, PropertyMethod> properties = readablePropertiesCache.get(beanClass);
        if (properties != null) {
            return properties;
        }

        Map<String, PropertyMethod> created = inspectProperties(beanClass, true);
        Map<String, PropertyMethod> previous = readablePropertiesCache.putIfAbsent(beanClass, created);
        return previous == null ? created : previous;
    }

    private static Map<String, PropertyMethod> getWritableProperties(Class<?> beanClass) {
        Map<String, PropertyMethod> properties = writablePropertiesCache.get(beanClass);
        if (properties != null) {
            return properties;
        }

        Map<String, PropertyMethod> created = inspectProperties(beanClass, false);
        Map<String, PropertyMethod> previous = writablePropertiesCache.putIfAbsent(beanClass, created);
        return previous == null ? created : previous;
    }

    private static Map<String, PropertyMethod> inspectProperties(Class<?> beanClass, boolean readable) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass, Object.class);
            PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
            Map<String, PropertyMethod> properties = new HashMap<>(descriptors.length * 2);

            for (PropertyDescriptor descriptor : descriptors) {
                Method method = readable ? descriptor.getReadMethod() : descriptor.getWriteMethod();
                if (method == null || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                makeAccessible(method);
                Class<?> propertyType = readable ? method.getReturnType() : method.getParameterTypes()[0];
                properties.put(descriptor.getName(), new PropertyMethod(method, propertyType));
            }

            if (!readable) {
                addChainedWritableProperties(beanClass, properties);
            }

            return Collections.unmodifiableMap(properties);
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("解析 Bean 类型失败：" + beanClass.getName(), e);
        }
    }

    private static void addChainedWritableProperties(Class<?> beanClass, Map<String, PropertyMethod> properties) {
        for (Class<?> currentClass = beanClass;
             currentClass != null && currentClass != Object.class;
             currentClass = currentClass.getSuperclass()) {
            Method[] methods = currentClass.getDeclaredMethods();
            for (Method method : methods) {
                if (!isChainedSetter(beanClass, method)) {
                    continue;
                }

                String propertyName = resolvePropertyName(method.getName());
                if (propertyName == null || properties.containsKey(propertyName)) {
                    continue;
                }

                makeAccessible(method);
                properties.put(propertyName, new PropertyMethod(method, method.getParameterTypes()[0]));
            }
        }
    }

    private static boolean isChainedSetter(Class<?> beanClass, Method method) {
        int modifiers = method.getModifiers();
        if (Modifier.isStatic(modifiers) || method.isSynthetic() || method.isBridge()) {
            return false;
        }
        if (method.getParameterTypes().length != 1) {
            return false;
        }
        if (!method.getName().startsWith("set") || method.getName().length() <= 3) {
            return false;
        }

        Class<?> returnType = method.getReturnType();
        return returnType != Void.TYPE && returnType.isAssignableFrom(beanClass);
    }

    private static String resolvePropertyName(String setterName) {
        return Introspector.decapitalize(setterName.substring(3));
    }

    private static void makeAccessible(Method method) {
        if (!Modifier.isPublic(method.getModifiers())
                || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            method.setAccessible(true);
        }
    }

    private static boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
        Class<?> actualTargetType = wrapPrimitive(targetType);
        Class<?> actualSourceType = wrapPrimitive(sourceType);
        return actualTargetType.isAssignableFrom(actualSourceType);
    }

    private static Set<String> toIgnoreSet(String[] ignoreProperties) {
        if (ignoreProperties == null || ignoreProperties.length == 0) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(ignoreProperties));
    }

    private static <T> T newInstance(Class<T> targetClass) {
        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            if (!Modifier.isPublic(constructor.getModifiers())
                    || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers())) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("目标类型缺少无参构造器：" + targetClass.getName(), e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("目标类型不能被实例化：" + targetClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("目标类型构造器不可访问：" + targetClass.getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("目标类型构造器执行失败：" + targetClass.getName(), e);
        }
    }

    private static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static final class PropertyMethod {

        private final Method method;

        private final Class<?> propertyType;

        private PropertyMethod(Method method, Class<?> propertyType) {
            this.method = method;
            this.propertyType = propertyType;
        }

        private Object get(Object bean) {
            try {
                return method.invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Bean 属性读取失败：" + method.getName(), e);
            }
        }

        private void set(Object bean, Object value) {
            try {
                method.invoke(bean, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Bean 属性写入失败：" + method.getName(), e);
            }
        }
    }

    private static final class PropertyCopy {

        private final String name;

        private final Method readMethod;

        private final Method writeMethod;

        private PropertyCopy(String name, Method readMethod, Method writeMethod) {
            this.name = name;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
        }

        private void copy(Object source, Object target) {
            try {
                Object value = readMethod.invoke(source);
                writeMethod.invoke(target, value);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Bean 属性访问失败：" + name, e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Bean 属性拷贝失败：" + name, e);
            }
        }
    }

    private static final class CopyPlan {

        private final List<PropertyCopy> copies;

        private CopyPlan(List<PropertyCopy> copies) {
            this.copies = Collections.unmodifiableList(new ArrayList<>(copies));
        }

        private void copy(Object source, Object target, Set<String> ignored) {
            for (PropertyCopy copy : copies) {
                if (!ignored.contains(copy.name)) {
                    copy.copy(source, target);
                }
            }
        }
    }

    private static final class CopyPlanKey {

        private final Class<?> sourceClass;

        private final Class<?> targetClass;

        private CopyPlanKey(Class<?> sourceClass, Class<?> targetClass) {
            this.sourceClass = sourceClass;
            this.targetClass = targetClass;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CopyPlanKey)) {
                return false;
            }
            CopyPlanKey other = (CopyPlanKey) obj;
            return sourceClass.equals(other.sourceClass) && targetClass.equals(other.targetClass);
        }

        @Override
        public int hashCode() {
            int result = sourceClass.hashCode();
            result = 31 * result + targetClass.hashCode();
            return result;
        }
    }
}
