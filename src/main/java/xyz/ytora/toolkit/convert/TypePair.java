package xyz.ytora.toolkit.convert;

import java.util.Objects;

/**
 * created by yangtong on 2025/4/4 下午4:56
 * <br/>
 * 类型对包装类
 */
public class TypePair {

    Class<?> sourceType;
    Class<?> targetType;

    public TypePair(Class<?> sourceType, Class<?> targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    public Class<?> getSourceType() {
        return sourceType;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypePair)) {
            return false;
        }
        TypePair pair = (TypePair) o;
        return sourceType.equals(pair.sourceType) && targetType.equals(pair.targetType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, targetType);
    }
}
