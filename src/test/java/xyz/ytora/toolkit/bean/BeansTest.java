package xyz.ytora.toolkit.bean;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeansTest {

    @Test
    void shouldCopyMatchedProperties() {
        SourceBean source = new SourceBean();
        Address address = new Address("杭州");
        source.setName("ytora");
        source.setAge(18);
        source.setEnabled(true);
        source.setAddress(address);
        source.setCreatedAt(LocalDate.of(2026, 4, 21));

        TargetBean target = new TargetBean();
        Beans.copyProperties(source, target);

        assertEquals("ytora", target.getName());
        assertEquals(Integer.valueOf(18), target.getAge());
        assertTrue(target.isEnabled());
        assertSame(address, target.getAddress());
        assertNull(target.getCreatedAtText());
    }

    @Test
    void shouldIgnoreSpecifiedProperties() {
        SourceBean source = new SourceBean();
        source.setName("ytora");
        source.setAge(18);

        TargetBean target = new TargetBean();
        Beans.copyProperties(source, target, "age");

        assertEquals("ytora", target.getName());
        assertNull(target.getAge());
    }

    @Test
    void shouldCreateTargetAndCopyProperties() {
        SourceBean source = new SourceBean();
        source.setName("ytora");
        source.setAge(18);

        TargetBean target = Beans.copyTo(source, TargetBean.class);

        assertNotSame(source, target);
        assertEquals("ytora", target.getName());
        assertEquals(Integer.valueOf(18), target.getAge());
    }

    @Test
    void shouldSupportPrivateNoArgConstructor() {
        SourceBean source = new SourceBean();
        source.setName("ytora");

        PrivateConstructorTarget target = Beans.copyTo(source, PrivateConstructorTarget.class);

        assertEquals("ytora", target.getName());
    }

    @Test
    void shouldCopyToBeanWithChainedSetter() {
        SourceBean source = new SourceBean();
        source.setName("ytora");
        source.setAge(18);

        ChainedSetterTarget target = Beans.copyTo(source, ChainedSetterTarget.class);

        assertEquals("ytora", target.getName());
        assertEquals(Integer.valueOf(18), target.getAge());
    }

    @Test
    void shouldRejectNullArguments() {
        SourceBean source = new SourceBean();

        assertThrows(IllegalArgumentException.class, () -> Beans.copyProperties(null, new TargetBean()));
        assertThrows(IllegalArgumentException.class, () -> Beans.copyProperties(source, null));
        assertThrows(IllegalArgumentException.class, () -> Beans.copyTo(source, null));
    }

    @Test
    void shouldRejectTargetWithoutNoArgConstructor() {
        SourceBean source = new SourceBean();

        assertThrows(IllegalArgumentException.class, () -> Beans.copyTo(source, NoDefaultConstructorTarget.class));
    }

    @Test
    void shouldClearCache() {
        SourceBean source = new SourceBean();
        source.setName("ytora");

        Beans.copyTo(source, TargetBean.class);
        Beans.clearCache();
        TargetBean target = Beans.copyTo(source, TargetBean.class);

        assertEquals("ytora", target.getName());
    }

    @Test
    void shouldConvertBeanToMap() {
        SourceBean source = new SourceBean();
        source.setName("ytora");
        source.setAge(18);
        source.setEnabled(true);
        source.setCreatedAt(LocalDate.of(2026, 4, 21));

        Map<String, Object> map = Beans.toMap(source, true, "address");

        assertEquals("ytora", map.get("name"));
        assertEquals(18, map.get("age"));
        assertEquals(true, map.get("enabled"));
        assertEquals(LocalDate.of(2026, 4, 21), map.get("createdAt"));
        assertFalse(map.containsKey("address"));
    }

    @Test
    void shouldConvertMapToBeanWithTypeConversion() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "ytora");
        map.put("age", "18");
        map.put("enabled", 1);
        map.put("createdAt", "2026-04-21");
        map.put("unknown", "ignored");

        SourceBean bean = Beans.mapToBean(map, SourceBean.class);

        assertEquals("ytora", bean.getName());
        assertEquals(18, bean.getAge());
        assertTrue(bean.isEnabled());
        assertEquals(LocalDate.of(2026, 4, 21), bean.getCreatedAt());
    }

    @Test
    void shouldSkipInvalidMapValuesAndIgnoredProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "ytora");
        map.put("age", "abc");
        map.put("enabled", null);

        SourceBean bean = Beans.mapToBean(map, SourceBean.class, "name");

        assertNull(bean.getName());
        assertEquals(0, bean.getAge());
        assertFalse(bean.isEnabled());
    }

    @Test
    void shouldMapToBeanWithChainedSetter() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "ytora");
        map.put("age", "18");

        ChainedSetterTarget target = Beans.mapToBean(map, ChainedSetterTarget.class);

        assertEquals("ytora", target.getName());
        assertEquals(Integer.valueOf(18), target.getAge());
    }

    static class SourceBean {

        private String name;

        private int age;

        private boolean enabled;

        private Address address;

        private LocalDate createdAt;

        private String writeOnly;

        String packageValue;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public LocalDate getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDate createdAt) {
            this.createdAt = createdAt;
        }

        public void setWriteOnly(String writeOnly) {
            this.writeOnly = writeOnly;
        }
    }

    static class TargetBean {

        private String name;

        private Integer age;

        private boolean enabled;

        private Address address;

        private String createdAtText;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public String getCreatedAtText() {
            return createdAtText;
        }

        public void setCreatedAtText(String createdAtText) {
            this.createdAtText = createdAtText;
        }
    }

    static class PrivateConstructorTarget {

        private String name;

        private PrivateConstructorTarget() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class ChainedSetterTarget {

        private String name;

        private Integer age;

        public String getName() {
            return name;
        }

        public ChainedSetterTarget setName(String name) {
            this.name = name;
            return this;
        }

        public Integer getAge() {
            return age;
        }

        public ChainedSetterTarget setAge(Integer age) {
            this.age = age;
            return this;
        }
    }

    static class NoDefaultConstructorTarget {

        NoDefaultConstructorTarget(String name) {
        }
    }

    static class Address {

        private final String city;

        Address(String city) {
            this.city = city;
        }

        String getCity() {
            return city;
        }
    }
}
