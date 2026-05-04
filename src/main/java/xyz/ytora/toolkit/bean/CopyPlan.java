package xyz.ytora.toolkit.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 拷贝计划
 *
 * @author ytora 
 * @since 1.0
 */
public class CopyPlan {

    final List<PropertyCopy> copies;

    CopyPlan(List<PropertyCopy> copies) {
        this.copies = Collections.unmodifiableList(new ArrayList<PropertyCopy>(copies));
    }

    void copy(Object source, Object target, Set<String> ignored) {
        for (PropertyCopy copy : copies) {
            if (!ignored.contains(copy.name)) {
                copy.copy(source, target);
            }
        }
    }

}
