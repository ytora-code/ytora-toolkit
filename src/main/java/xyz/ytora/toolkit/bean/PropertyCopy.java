package xyz.ytora.toolkit.bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 描述
 *
 * <p>说明</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class PropertyCopy {
    final String name;

    final Method readMethod;

    final Method writeMethod;

    PropertyCopy(String name, Method readMethod, Method writeMethod) {
        this.name = name;
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
    }

    void copy(Object source, Object target) {
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
