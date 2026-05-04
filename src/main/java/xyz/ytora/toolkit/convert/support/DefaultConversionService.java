package xyz.ytora.toolkit.convert.support;

import xyz.ytora.toolkit.convert.*;
import xyz.ytora.toolkit.number.Nums;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * created by yangtong on 2025/4/4 下午4:59
 * 默认的类型转换器
 */
public class DefaultConversionService implements ConverterRegistry, ConversionService {
    private final Map<TypePair, Converter<?, ?>> converterMap = new ConcurrentHashMap<>();

    public static DefaultConversionService init(String basePackage) {
        DefaultConversionService service = new DefaultConversionService();
        String path = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            // 使用 getResources 获取所有可能的路径（包括依赖包里的）
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    // 处理本地开发环境
                    scanFromDirectory(service, basePackage, new File(resource.toURI()));
                } else if ("jar".equals(protocol)) {
                    // 处理 JAR 包环境
                    scanFromJar(service, basePackage, resource);
                }
            }
        } catch (Exception e) {
            throw new ConverterException("初始化转换器失败", e);
        }
        return service;
    }

    /**
     * 进行类型转换
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T convert(Object source, Class<T> targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("目标类型不能为 null");
        }

        Class<?> actualTargetType = wrapPrimitive(targetType);

        if (source == null) {
            if (targetType.isPrimitive()) {
                throw new ConverterException("不能将 null 转换为基本类型：" + targetType.getName());
            }
            return null;
        }

        if (actualTargetType.isInstance(source)) {
            return (T) source;
        }

        if (source instanceof Number && Nums.isNum(actualTargetType)) {
            return (T) convertNumber((Number) source, actualTargetType);
        }

        if (actualTargetType == String.class) {
            Converter<Object, T> converter = findConverter(source.getClass(), actualTargetType);
            if (converter != null) {
                return converter.convert(source);
            }
            return (T) String.valueOf(source);
        }

        Converter<Object, T> converter = findConverter(source.getClass(), actualTargetType);
        if (converter != null) {
            return converter.convert(source);
        }

        throw new ConverterException("无法进行类型转换：" + source.getClass().getName() + " -> " + targetType.getName());
    }

    /**
     * 注册类型转换器
     */
    @Override
    public <S, T> void addConverter(Class<S> sourceType, Class<T> targetType, Converter<S, T> converter) {
        if (sourceType == null || targetType == null || converter == null) {
            throw new IllegalArgumentException("转换器注册参数不能为 null");
        }
        Class<?> actualSourceType = wrapPrimitive(sourceType);
        Class<?> actualTargetType = wrapPrimitive(targetType);
        converterMap.put(new TypePair(actualSourceType, actualTargetType), converter);
        converterMap.put(new TypePair(actualTargetType, actualSourceType), new ReverseConverter<T, S>(converter));
    }

    /**
     * 扫描本地目录
     */
    private static void scanFromDirectory(DefaultConversionService service, String basePackage, File directory) throws Exception {
        if (!directory.exists()) return;

        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                // 如果需要递归扫描，可以递归调用，这里演示单层
                continue;
            }
            String fileName = file.getName();
            if (fileName.endsWith(".class")) {
                String className = basePackage + "." + fileName.replace(".class", "");
                registerClass(service, className);
            }
        }
    }

    /**
     * 扫描 JAR 包
     */
    private static void scanFromJar(DefaultConversionService service, String basePackage, URL resource) throws IOException {
        JarURLConnection jarURLConnection = (JarURLConnection) resource.openConnection();
        try (JarFile jarFile = jarURLConnection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            String path = basePackage.replace('.', '/');

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // 查找以包路径开头且以 .class 结尾的文件
                if (name.startsWith(path) && name.endsWith(".class")) {
                    // 排除目录本身和内部类（根据需求）
                    if (name.contains("$") || name.endsWith("/")) continue;

                    String className = name.replace("/", ".").replace(".class", "");
                    registerClass(service, className);
                }
            }
        }
    }

    /**
     * 核心注册逻辑
     */
    @SuppressWarnings("unchecked")
    private static void registerClass(DefaultConversionService service, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (Converter.class.isAssignableFrom(clazz)
                    && !clazz.isInterface()
                    && !Modifier.isAbstract(clazz.getModifiers())) {
                Type[] genericInterfaces = clazz.getGenericInterfaces();
                for (Type type : genericInterfaces) {
                    if (type instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) type;
                        if (pt.getRawType() == Converter.class) {
                            Class<?> sourceType = (Class<?>) pt.getActualTypeArguments()[0];
                            Class<?> targetType = (Class<?>) pt.getActualTypeArguments()[1];

                            Converter<?, ?> converter = (Converter<?, ?>) clazz.getDeclaredConstructor().newInstance();
                            registerConverter(service, sourceType, targetType, converter);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ConverterException("加载转换器失败：" + className, e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerConverter(
            DefaultConversionService service,
            Class<?> sourceType,
            Class<?> targetType,
            Converter<?, ?> converter) {
        service.addConverter((Class) sourceType, (Class) targetType, (Converter) converter);
    }

    /**
     * 数字类型直接转换，不用调用底层的转换组件
     */
    private Object convertNumber(Number number, Class<?> targetType) {
        if (targetType == int.class || targetType == Integer.class) {
            return number.intValue();
        } else if (targetType == long.class || targetType == Long.class) {
            return number.longValue();
        } else if (targetType == double.class || targetType == Double.class) {
            return number.doubleValue();
        } else if (targetType == float.class || targetType == Float.class) {
            return number.floatValue();
        } else if (targetType == short.class || targetType == Short.class) {
            return number.shortValue();
        } else if (targetType == byte.class || targetType == Byte.class) {
            return number.byteValue();
        } else if (targetType == java.math.BigInteger.class) {
            return java.math.BigInteger.valueOf(number.longValue());
        } else if (targetType == java.math.BigDecimal.class) {
            return new java.math.BigDecimal(number.toString());
        }
        throw new IllegalArgumentException("不支持的数字类型: " + targetType.getName());
    }

    @SuppressWarnings("unchecked")
    private <T> Converter<Object, T> findConverter(Class<?> sourceType, Class<?> targetType) {
        Converter<?, ?> exact = converterMap.get(new TypePair(wrapPrimitive(sourceType), wrapPrimitive(targetType)));
        if (exact != null) {
            return (Converter<Object, T>) exact;
        }

        for (Map.Entry<TypePair, Converter<?, ?>> entry : converterMap.entrySet()) {
            TypePair typePair = entry.getKey();
            if (typePair.getSourceType().isAssignableFrom(sourceType)
                    && targetType.isAssignableFrom(typePair.getTargetType())) {
                return (Converter<Object, T>) entry.getValue();
            }
        }
        return null;
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
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
}
