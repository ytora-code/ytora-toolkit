package xyz.ytora.toolkit.reflect.classcache.meta;

import xyz.ytora.toolkit.reflect.classcache.ClassCache;
import xyz.ytora.toolkit.text.Strs;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 方法元数据
 *
 * <p>缓存了方法信息，提供了高性能调用API</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class MethodMeta {

    /**
     * 方法原始Method对象
     */
    private final Method sourceMethod;

    /**
     * 该方法所属的类元
     */
    private ClassMeta<?> classMeta;

    /**
     * 方法名称
     */
    private String name;

    /**
     * 方法参数列表
     */
    private List<ParameterMeta> args;

    /**
     * 方法注解合集
     */
    private Map<Class<? extends Annotation>, Annotation> annotations;

    /**
     * 方法返回值类型
     */
    private Type resultType;

    /**
     * 方法修饰符
     */
    private Integer modifiers;

    /**
     * 该方法对于的字段(当且仅当该方法是getter/setter方法时有值)
     */
    private FieldMeta fieldMeta;

    /**
     * 方法调用句柄，高性能高性能
     */
    private volatile MethodHandle methodHandle;

    /**
     * 适配为 Object[] 入参的调用句柄，避免 invokeWithArguments 的额外开销
     */
    private volatile MethodHandle spreaderHandle;

    public MethodMeta(Method sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    public Method sourceMethod() {
        return sourceMethod;
    }

    /**
     * 获取该方法所在类的类元缓存
     * @return ClassMeta对象
     */
    public ClassMeta<?> classMeta() {
        if (classMeta == null) {
            Class<?> clazzMeta = sourceMethod.getDeclaringClass();
            classMeta = ClassCache.getClassMeta(clazzMeta);
        }
        return classMeta;
    }

    /**
     * 获取方法名称
     * @return 方法名称
     */
    public String name() {
        if (name == null) {
            name = sourceMethod.getName();
        }
        return name;
    }

    /**
     * 获取方法的参数列表
     * @return ParameterMeta列表
     */
    public List<ParameterMeta> args() {
        if (args == null) {
            Parameter[] parameters = sourceMethod.getParameters();
            args = new ArrayList<>();
            for (int i = 0; i < parameters.length; i++) {
                Parameter p = parameters[i];
                Annotation[] argAnnotations = p.getAnnotations();
                Map<Class<? extends Annotation>, Annotation> annotations = Arrays.stream(argAnnotations)
                        .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
                ParameterMeta pm = new ParameterMeta(i, p.getName(), p.getType(), annotations);
                args.add(pm);
            }
        }
        return args;
    }

    /**
     * 获取该方法上指定类型的注解
     * @param type 注解的class对象
     * @return 注解对象，如果该方法上没有标注该注解，则返回空
     * @param <A> 注解类型
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A annotation(Class<A> type) {
        if (annotations == null) {
            this.annotations = Arrays.stream(sourceMethod.getAnnotations())
                    .collect(Collectors.toMap(Annotation::annotationType, a -> a));
        }
        return (A) annotations.get(type);
    }

    /**
     * 获取方法的返回值类型
     * @return 返回值类型 Type
     */
    public Type resultType() {
        if (resultType == null) {
            resultType = sourceMethod.getGenericReturnType();
        }
        return resultType;
    }

    /**
     * 方法的修饰符
     * @return int数字，可以结合 Modifier.isPrivate、isStatic 等方法使用
     */
    public Integer modifiers() {
        if (modifiers == null) {
            modifiers = sourceMethod.getModifiers();
        }
        return modifiers;
    }

    /**
     * 获取该方法对于的字段元
     * @return FieldMeta，当且仅当该方法是getter/setter时，才会返回FieldMeta，否则返回空
     */
    public FieldMeta fieldMeta() {
        if (fieldMeta == null) {
            String methodName = name();
            String fieldName = null;

            if (methodName.startsWith("get") && sourceMethod.getParameterCount() == 0 && sourceMethod.getReturnType() != void.class) {
                fieldName = Strs.firstLowercase(methodName.substring(3));
            } else if (methodName.startsWith("is") && sourceMethod.getParameterCount() == 0
                    && (sourceMethod.getReturnType() == boolean.class || sourceMethod.getReturnType() == Boolean.class)) {
                fieldName = Strs.firstLowercase(methodName.substring(2));
            } else if (methodName.startsWith("set") && sourceMethod.getParameterCount() == 1) {
                fieldName = Strs.firstLowercase(methodName.substring(3));
            }

            if (fieldName != null) {
                ClassMeta<?> cm = classMeta();
                fieldMeta = cm.getField(fieldName);
            }
        }
        return fieldMeta;
    }

    // ======================= 方法的执行 =======================>

    /**
     * 执行方法
     * @param obj 在obj对象上执行，如果是静态方法可以为空
     * @param args 参数列表
     * @return 方法执行结果
     */
    public Object invoke(Object obj, Object... args) {
        try {
            return sourceMethod.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
