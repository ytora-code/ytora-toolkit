package xyz.ytora.toolkit.reflect.classcache.meta;

import xyz.ytora.toolkit.reflect.classcache.ClassCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 类元数据
 *
 * <p>缓存了类元信息，提供字段、方法、构造器等元数据的访问入口</p>
 *
 * @author ytora 
 * @since 1.0
 *
 */
public class ClassMeta<T> {

    /**
     * sourceClass 原始类的类型
     */
    private final Class<T> sourceClazz;
    /**
     * name 全类名
     */
    private String name;
    /**
     * 类的注解合集
     */
    private Map<Class<? extends Annotation>, Annotation> annotations;
    /**
     * 类的字段合集
     */
    private Map<String, FieldMeta> fields;
    /**
     * 类的构造器合集
     */
    private Map<String, ConstructorMeta<T>> constructors;
    /**
     * 类的方法合集
     */
    private Map<String, MethodMeta> methods;

    private static final Set<String> IGNORE_METHOD_LIST = new HashSet<String>(Arrays.asList("toString", "equals", "canEqual", "hashCode", "clone")) {
    };

    public ClassMeta(Class<T> sourceClazz) {
        // 判断即将缓存的类是平台类还是业务类
        if (ClassCache.isPlatformType(sourceClazz)) {
            // 进入这里说明sourceClazz属于平台类，不能缓存
            throw new IllegalArgumentException("平台类型[" + sourceClazz.getName() + "]不能被缓存!");
        }

        this.sourceClazz = sourceClazz;
    }

    /**
     * 获取类名称
     * @return 全类名
     */
    public String name() {
        if (name == null) {
            name = sourceClazz.getName();
        }
        return name;
    }

    /**
     * 获取到指定类型的参数注解
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> type) {
        if (annotations == null) {
            this.annotations = Arrays.stream(sourceClazz.getAnnotations())
                    .collect(Collectors.toMap(Annotation::annotationType, a -> a));
        }
        return (A) annotations.get(type);
    }

    /**
     * 根据字段名称获取字段
     * @param name 字段名称
     * @return FieldMetadata对象
     */
    public FieldMeta getField(String name) {
        if (fields == null) {
            collectFields(sourceClazz);
        }
        return fields.get(name);
    }


    /**
     * 根据使用者指定的规则，筛选所有符合条件的字段
     * @param predicate 过滤规则
     * @return FieldMeta对象数组
     */
    public List<FieldMeta> getFields(Predicate<FieldMeta> predicate) {
        if (fields == null) {
            collectFields(sourceClazz);
        }
        return fields.values().stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * 根据参数列表，返回对应的构造器
     * @param args 参数列表
     * @return 方法元数据
     */
    public ConstructorMeta<T> getConstructor(Type... args) {
        if (constructors == null) {
            collectConstructor(sourceClazz);
        }
        return constructors.get(buildKey(null, args));
    }

    /**
     * 根据方法名称以及参数列表，返回对应的方法
     * @param name 方法名称
     * @param argTypes 参数类型列表
     * @return MethodMeta对象
     */
    public MethodMeta getMethod(String name, Class<?>... argTypes) {
        if (methods == null) {
            collectMethods(sourceClazz);
        }
        return methods.get(buildKey(name, argTypes));
    }

    /**
     * 根据使用者指定的规则，筛选所有符合条件的方法元数据
     *
     * @param predicate 过滤规则
     * @return MethodMeta对象
     */
    public List<MethodMeta> getMethods(Predicate<MethodMeta> predicate) {
        if (methods == null) {
            collectMethods(sourceClazz);
        }
        return methods.values().stream().filter(predicate).collect(Collectors.toList());
    }

    // ========================== 内部方法 ==========================>
    private void collectFields(Class<?> type) {
        if (fields != null) {
            return;
        }
        fields = new LinkedHashMap<>();
        doCollectFields(type);
    }

    private void doCollectFields(Class<?> type) {
        if (type != null && type != Object.class) {
            // 先收集父类字段
            doCollectFields(type.getSuperclass());

            Field[] fields = type.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                // 子类优先
                this.fields.put(field.getName(), new FieldMeta(field));
            }
        }
    }

    private void collectConstructor(Class<?> type) {
        if (constructors != null) {
            return;
        }
        constructors = new LinkedHashMap<>();
        if (type == null || type == Object.class) {
            return;
        }
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            constructors.put(buildKey(null, constructor.getParameterTypes()), new ConstructorMeta<>(constructor));
        }
    }

    private void collectMethods(Class<?> type) {
        if (methods != null) {
            return;
        }
        methods = new LinkedHashMap<>();
        doCollectMethods(type);
    }

    private void doCollectMethods(Class<?> type) {
        if (type != null && type != Object.class) {
            // 优先收集父类的方法
            doCollectMethods(type.getSuperclass());
            Method[] methods = type.getDeclaredMethods();
            for (Method method : methods) {
                if (IGNORE_METHOD_LIST.contains(method.getName())) {
                    continue;
                }
                method.setAccessible(true);
                this.methods.put(buildKey(method.getName(), method.getParameterTypes()), new MethodMeta(method));
            }
        }
    }

    private String buildKey(String methodName, Type... argTypes) {
        String argsKey = "";
        if (argTypes != null && argTypes.length > 0) {
            argsKey = Arrays.stream(argTypes).map(Type::getTypeName).collect(Collectors.joining(","));
        }
        if (methodName == null) {
            return "(" + argsKey + ")";
        }
        return methodName + "(" + argsKey + ")";
    }

}
