package xyz.ytora.toolkit.bean;

import java.lang.reflect.Method;

/**
 * 描述
 *
 * @author ytora
 * @since 1.0
 */
public class PropertyMethod {
    final Method method;

    final Class<?> propertyType;

    PropertyMethod(Method method, Class<?> propertyType) {
        this.method = method;
        this.propertyType = propertyType;
    }
}
