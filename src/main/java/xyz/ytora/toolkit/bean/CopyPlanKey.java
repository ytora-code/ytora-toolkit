package xyz.ytora.toolkit.bean;

/**
 * 描述
 *
 * <p>说明</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class CopyPlanKey {

    final Class<?> sourceClass;

    final Class<?> targetClass;

    CopyPlanKey(Class<?> sourceClass, Class<?> targetClass) {
        this.sourceClass = sourceClass;
        this.targetClass = targetClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CopyPlanKey)) {
            return false;
        }
        CopyPlanKey other = (CopyPlanKey) obj;
        return sourceClass.equals(other.sourceClass) && targetClass.equals(other.targetClass);
    }

    @Override
    public int hashCode() {
        int result = sourceClass.hashCode();
        result = 31 * result + targetClass.hashCode();
        return result;
    }

}
