package xyz.ytora.toolkit.tree;

import xyz.ytora.toolkit.collection.Colls;
import xyz.ytora.toolkit.text.Strs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树状数据工具类
 *
 * @author ytora 
 * @since 1.0
 */
public final class Trees {

    /**
     * 将树形结构扁平化
     * @param tree 树形数据
     * @return 扁平化后的列表
     */
    public static <T extends ITree<T>> List<T> flattenTree(List<T> tree) {
        List<T> result = new ArrayList<>();
        if (tree == null) {
            return result;
        }
        for (T node : tree) {
            // 加入当前节点
            result.add(node);
            if (Colls.isNotEmpty(node.getChildren())) {
                flattenTree(node.getChildren());
            }
            // 将当前阶段children置空
            node.setChildren(null);
        }
        return result;
    }

    /**
     * 将目标数据变成具有层级的数组结构
     * @param items 目标数据
     * @return 树状数据
     * @param <T> 目标数据元素类型
     */
    public static <T extends ITree<T>> List<T> toTree(List<T> items) {
        return toTree(items, (NodeVisitor<T>)null);
    }

    /**
     * 将目标数据变成具有层级的数组结构
     * @param items 目标数据
     * @return 树状数据
     * @param visitor 回调函数
     * @param <T> 目标数据元素类型
     */
    public static <T extends ITree<T>> List<T> toTree(List<T> items, NodeVisitor<T> visitor) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. 建立 ID 映射表
        Map<String, T> nodeMap = items.stream()
                .collect(Collectors.toMap(ITree::getId, Function.identity(), (k1, k2) -> k1));


        // 2. 【组装】父子关系
        List<T> roots = new ArrayList<>();
        for (T item : items) {
            String pid = item.getPid();
            T parent = nodeMap.get(pid);

            // 如果没有父节点，说明它是当前数据集中的顶级节点
            if (pid == null || "0".equals(pid) || parent == null) {
                roots.add(item);
            } else {
                // 此时 parent.getChildren() 已经在第一步初始化过了，直接添加即可
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(item);
            }
        }

        // 4. 回调：按层级遍历树
        if (visitor != null) {
            for (T root : roots) {
                walk(root, null, 0, visitor);
            }
        }
        return roots;
    }

    public static <T extends ITree<T>> void walk(T node, T parent, int level, NodeVisitor<T> visitor) {
        visitor.accept(level, node, parent);

        List<T> children = node.getChildren();
        if (children == null || children.isEmpty()) return;

        for (T child : children) {
            walk(child, node, level + 1, visitor);
        }
    }

}