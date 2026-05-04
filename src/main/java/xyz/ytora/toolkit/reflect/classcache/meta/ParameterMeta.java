package xyz.ytora.toolkit.reflect.classcache.meta;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 参数元数据
 *
 * <p>可以表示方法参数、构造器参数</p>
 *
 * @author ytora 
 * @since 1.0
 *

 */
public class ParameterMeta {
    int index;
    String name;
    Class<?> type;
    Map<Class<? extends Annotation>, Annotation> annotations;

    public ParameterMeta(int index, String name, Class<?> type, Map<Class<? extends Annotation>, Annotation> annotations) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.annotations = annotations;
    }

    /**
     * 获取到指定类型的参数注解
     */
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> type) {
        return (A) annotations.get(type);
    }
}
