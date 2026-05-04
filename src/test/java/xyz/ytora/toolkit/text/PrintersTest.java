package xyz.ytora.toolkit.text;

import org.junit.jupiter.api.Test;

/**
 * 描述
 *
 * <p>说明</p>
 *
 * @author ytora 
 * @since 1.0
 */
public class PrintersTest {

    String sql = "CREATE TABLE sys_user (\n" +
            "    id varchar(255),\n" +
            "    create_by varchar(255),\n" +
            "    create_time timestamp,\n" +
            "    update_by varchar(255),\n" +
            "    update_time timestamp,\n" +
            "    depart_code varchar(255),\n" +
            "    version integer,\n" +
            "    user_name varchar(255) NOT NULL,\n" +
            "    real_name varchar(255) NOT NULL,\n" +
            "    password varchar(255) NOT NULL,\n" +
            "    avatar varchar(255),\n" +
            "    phone varchar(16),\n" +
            "    email varchar(255),\n" +
            "    birthday date,\n" +
            "    id_card varchar(255),\n" +
            "    PRIMARY KEY (id)\n" +
            ");";

    @Test
    public void test() {
        Printers.print(sql, Printers.PrintStyle.BOX_HEAVY);
    }
}
