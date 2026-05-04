package xyz.ytora.toolkit.reflect.classcache;

import xyz.ytora.toolkit.reflect.classcache.meta.ClassMeta;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类元缓存
 *
 * <p>提供静态方式，供调用者方便快捷地使用类元缓存</p>
 *
 * @author ytora 
 * @since 1.0.0
 */
public final class ClassCache {

    private static final Map<Class<?>, ClassMeta<?>> classMetaCache = new ConcurrentHashMap<>(32);

    private ClassCache() {
    }

    /**
     * 根据class获取对应的类元缓存
     * @param clazz class对象
     * @return 类元婚车
     * @param <T> class类型
     */
    @SuppressWarnings("unchecked")
    public static <T> ClassMeta<T> getClassMeta(Class<T> clazz) {
        if (isPlatformType(clazz)) {
            throw new IllegalArgumentException("类型[" + clazz.getName() + "]不可被缓存");
        }
        return (ClassMeta<T>) classMetaCache.computeIfAbsent(clazz, ClassMeta::new);
    }

    /**
     * 获取目标类上的指定注解
     * @param clazz 目标类
     * @param annoType 指定注解类型
     * @return 注解对象，如果该类没有标注annoType注解，则返回空
     * @param <A> 注解类型
     */
    public static <A extends Annotation> A getClazzAnno(Class<?> clazz, Class<A> annoType) {
        ClassMeta<?> classMeta = getClassMeta(clazz);
        return classMeta.getAnnotation(annoType);
    }

    /**
     * 判断指定类型是否属于JDK平台类型，JDK平台类型通常不希望被缓存
     * 通常不希望缓存平台类型，基本类型
     * @param clazz 类型 平台类型
     * @return true or false
     */
    public static boolean isPlatformType(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }

        if (clazz.isArray()) {
            return true;
        }

        if (clazz.isInterface()) {
            return true;
        }

        if (clazz.isAnnotation()) {
            return true;
        }

        if (clazz.isEnum()) {
            return true;
        }

        if (clazz.isAnonymousClass() || clazz.isLocalClass()) {
            return true;
        }

        ClassLoader cl = clazz.getClassLoader();
        if (cl == null) {
            return true;
        }

        Package pkg = clazz.getPackage();
        String packageName = (pkg == null ? "" : pkg.getName());

        return packageName.startsWith("java.")
                || packageName.startsWith("javax.")
                || packageName.startsWith("jdk.")
                || packageName.startsWith("sun.")
                || packageName.startsWith("com.sun.");
    }

}
