package xyz.ytora.toolkit.reflect.classcache.meta;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 构造器元数据
 *
 * <p>缓存了构造器学习，提供了高性能构建对象的API</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class ConstructorMeta<T> {

    /**
     * 构造器
     */
    private final Constructor<?> constructor;
    /**
     * 构造器参数
     */
    private List<ParameterMeta> parameters;
    /**
     * 构造器注解
     */
    private Map<Class<? extends Annotation>, Annotation> annotations;

    /**
     * 构造器调用句柄
     */
    private volatile MethodHandle constructorHandle;

    /**
     * 适配为 Object[] 入参的调用句柄，避免 invokeWithArguments 的额外开销
     */
    private volatile MethodHandle spreaderHandle;

    public ConstructorMeta(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    /**
     * 获取原始构造器
     *
     * @return Constructor对象
     */
    public Constructor<?> sourceConstructor() {
        return constructor;
    }

    /**
     * 获取构造器参数列表
     *
     * @return 参数元数据列表
     */
    public List<ParameterMeta> parameters() {
        if (parameters == null) {
            Parameter[] ps = constructor.getParameters();

            List<ParameterMeta> list = new ArrayList<>(ps.length);
            for (int i = 0; i < ps.length; i++) {
                Parameter p = ps[i];
                Map<Class<? extends Annotation>, Annotation> annotations = Arrays.stream(p.getAnnotations())
                        .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));

                list.add(new ParameterMeta(i, p.getName(), p.getType(), annotations));
            }
            parameters = list;
        }
        return parameters;
    }

    /**
     * 获取构造器上指定类型的注解
     *
     * @param type 注解Class
     * @param <A> 注解类型
     * @return 注解对象，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A annotation(Class<A> type) {
        if (annotations == null) {
            annotations = Arrays.stream(constructor.getAnnotations())
                    .collect(Collectors.toMap(Annotation::annotationType, Function.identity()));
        }
        return (A) annotations.get(type);
    }

    /**
     * 根据传入的实参，使用当前构造器来实例化对象
     *
     * @param args 参数列表
     * @return T类型的对象
     */
    @SuppressWarnings("unchecked")
    public T instance(Object... args) {
        try {
            return (T) constructor.newInstance(args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("构造对象失败: " + constructor, e);
        }
    }
}
