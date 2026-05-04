package xyz.ytora.toolkit.id.support;

import xyz.ytora.toolkit.id.IdGenerator;

import java.util.UUID;

/**
 * UUID 生成器
 *
 * @author ytora
 * @since 1.0
 */
public class UuidGenerator implements IdGenerator<String> {
    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}