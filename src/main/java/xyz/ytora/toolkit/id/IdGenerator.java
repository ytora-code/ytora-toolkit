package xyz.ytora.toolkit.id;

/**
 * ID 生成器
 *
 * @author ytora 
 * @since 1.0
 */
public interface IdGenerator<T> {
    T nextId();
}
