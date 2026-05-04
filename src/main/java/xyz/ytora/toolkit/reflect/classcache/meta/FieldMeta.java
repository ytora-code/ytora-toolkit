package xyz.ytora.toolkit.reflect.classcache.meta;

import xyz.ytora.toolkit.reflect.classcache.ClassCache;
import xyz.ytora.toolkit.text.Strs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字段元数据
 *
 * <p>缓存了字段学习，提供了高性能读写API</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class FieldMeta {

    /**
     * 字段原始Field对象
     */
    private final Field sourceField;

    /**
     * 该字段所属的类元
     */
    private ClassMeta<?> classMeta;

    /**
     * 字段类型
     */
    private Type type;

    /**
     * 该字段的名称
     */
    private String name;

    /**
     * 修饰该字段的注解合集
     */
    private Map<Class<? extends Annotation>, Annotation> annotations;

    /**
     * 字段修饰符
     */
    private Integer modifiers;

    /**
     * 字段对应的getter方法
     */
    private MethodMeta getter;
    /**
     * 字段对应的setter方法
     */
    private MethodMeta setter;

    public FieldMeta(Field sourceField) {
        this.sourceField = sourceField;
    }

    /**
     * 获取字段原始Field对象
     * @return java.lang.reflectField 对象
     */
    public Field sourceField() {
        return sourceField;
    }

    /**
     * 获取该字段所在的类元数据
     * @return ClassMeta对象
     */
    public ClassMeta<?> classMeta() {
        if (classMeta == null) {
            // 获取字段所在的class对象
            Class<?> clazz = sourceField.getDeclaringClass();
            classMeta = ClassCache.getClassMeta(clazz);
        }
        return classMeta;
    }

    /**
     * 获取字段类型
     * @return java.lang.reflectType 对象
     */
    public Type type() {
        if (type == null) {
            type = sourceField.getGenericType();
        }
        return type;
    }

    /**
     * 获取字段名称
     * @return 字段名称
     */
    public String name() {
        if (name == null) {
            name = sourceField.getName();
        }
        return name;
    }

    /**
     * 获取当前字段的修饰符
     * @return int数字，可以结合 Modifier.isPrivate、isStatic 等方法使用
     */
    public Integer modifiers() {
        if (modifiers == null) {
            modifiers = sourceField.getModifiers();
        }
        return modifiers;
    }

    /**
     * 获取该字段上指定类型的注解
     * @param type 注解的class对象
     * @return 注解对象，如果该字段上没有标注该注解，则返回空
     * @param <A> 注解类型
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A annotation(Class<A> type) {
        if (annotations == null) {
            this.annotations = Arrays.stream(sourceField.getAnnotations())
                    .collect(Collectors.toMap(Annotation::annotationType, a -> a));
        }
        return (A) annotations.get(type);
    }

    /**
     * 获取该字段对应的getter字段，如果没有则返回空
     *
     * @return MethodMeta对象
     */
    public MethodMeta getter() {
        if (getter == null) {
            String methodName = "get" + Strs.firstUppercase(name());
            ClassMeta<?> cm = classMeta();
            getter = cm.getMethod(methodName);

            // 有些字段的getter方法可能并不是以getter开头，而是is开头
            if (getter == null) {
                methodName = "is" + Strs.firstUppercase(name());
                getter = cm.getMethod(methodName);

                // 如果方法名称和字段名称相同，也认为该方法是getter
                if (getter == null) {
                    getter = cm.getMethod(name());
                }
            }
        }

        return getter;
    }

    /**
     * 获取该字段对应的setter字段，如果没有则返回空
     *
     * @return MethodMeta对象
     */
    public MethodMeta setter() {
        if (setter == null) {
            String methodName = "set" + Strs.firstUppercase(name());
            ClassMeta<?> cm = classMeta();
            setter = cm.getMethod(methodName, sourceField.getType());
        }
        return setter;
    }

    // ======================= 字段的读写 =======================>

    /**
     * 读取目标对象里面的字段值
     *
     * @param obj 目标对象，如果是静态字段可以为空
     * @return 字段值
     */
    public Object get(Object obj) {
        try {
            return sourceField.get(obj);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 给目标对象的字段写入值
     *
     * @param obj 目标对象，如果是静态字段可以为空
     * @param value 要写入的字段值
     */
    public void set(Object obj, Object value) {
        try {
            sourceField.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
