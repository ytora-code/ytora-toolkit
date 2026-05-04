package xyz.ytora.toolkit.id;

import xyz.ytora.toolkit.id.support.LocalIdGenerator;
import xyz.ytora.toolkit.id.support.SnowflakeId;
import xyz.ytora.toolkit.id.support.ULID;
import xyz.ytora.toolkit.id.support.UuidGenerator;

/**
 * ID 生成工具类
 *
 * @author ytora 
 * @since 1.0
 */
public final class Ids {

    /**
     * 使用雪花算法产生ID
     */
    private static final IdGenerator<Long> snowflakeId = new SnowflakeId(0, 0);

    /**
     * 使用UUID产生ID
     */
    private static final IdGenerator<String> uuid = new UuidGenerator();

    /**
     * 使用ULID产生ID
     */
    private static final IdGenerator<String> ulid = new ULID();

    /**
     * 基于内存的本地ID产生器，中心化
     */
    private static final IdGenerator<Long> localId = new LocalIdGenerator();

    /**
     * 通过雪花算法产生下一个ID
     * @return ID
     */
    public static Long nextSnowflakeId() {
        return snowflakeId.nextId();
    }

    /**
     * 通过UUID算法产生下一个ID
     * @return ID
     */
    public static String nextUuid() {
        return uuid.nextId();
    }

    /**
     * 通过ULID算法产生下一个ID
     * @return ID
     */
    public static String nextUlid() {
        return ulid.nextId();
    }

    /**
     * 基于本地内存产生下一个ID
     * @return ID
     */
    public static Long nextLocalId() {
        return localId.nextId();
    }

}
