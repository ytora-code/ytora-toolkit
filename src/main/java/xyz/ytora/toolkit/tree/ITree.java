package xyz.ytora.toolkit.tree;

import java.util.List;

/**
 * 树形数据规范
 *
 * @author ytora
 * @since 1.0
 */
public interface ITree<T> {
    /**
     * 获取当前数据id
     */
    String getId();

    /**
     * 获取当前数据父id，如果一个元素没有父元素，则pid = 0
     */
    String getPid();

    /**
     * 获取当前数据的所有子数据
     */
    List<T> getChildren();

    /**
     * 设置当前数据的所有子数据
     */
    void setChildren(List<T> children);

}
