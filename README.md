# ytora-toolkit

🍉一个基于 JDK 8 的通用 Java 工具类库，零第三方依赖，覆盖 Bean、集合、类型转换、Excel、ID、IO、数字、文本、时间、树结构和反射缓存等常见场景。

## 1 开始

### 引入方式

```xml
<dependency>
    <groupId>xyz.ytora</groupId>
    <artifactId>ytora-toolkit</artifactId>
    <version>1.0</version>
</dependency>
```

### 模块导览

🍎目前为止，该类库存在如下模块，还在持续更新ing...

| 模块 | 入口类 | 适用场景 |
| --- | --- | --- |
| Bean | `Beans` | Bean 拷贝、Bean 与 Map 互转 |
| 集合 | `Colls` | 交并差、映射、分组、分页、统计 |
| 转换 | `Converts` | 字符串、数字、日期之间的类型转换 |
| Excel | `Excels` | Excel 读写，支持 Map 和 Bean |
| ID | `Ids` | 生成雪花 ID、UUID、ULID、本地递增 ID |
| IO | `Ios` | 流读取、写入、复制、关闭 |
| 数字 | `Nums` | 精确计算、取模、百分比 |
| 文本 | `Strs` `Chars` `Encodes` `Regs` `Printers` | 字符串处理、字符判断、编码、校验、格式化打印 |
| 时间 | `Dates` `BusinessDates` `Crons` | 日期时间处理、工作日计算、Cron 解析 |
| 树 | `Trees` | 列表转树、树遍历 |
| 反射 | `ClassCache` | 高性能类元信息读取 |



## 2 Bean工具

🍑用最小成本完成对象拷贝和 Bean / Map 互转。属性匹配遵循 JavaBeans 规范，拷贝为浅拷贝。

### 2.1 Bean 拷贝

```java
SourceUser source = new SourceUser();
source.setName("ytora");
source.setAge(18);

TargetUser target = new TargetUser();
Beans.copyProperties(source, target);

TargetUser copied = Beans.copyTo(source, TargetUser.class);
```

输入：`name=ytora, age=18`  
输出：`target.name=ytora, target.age=18`

### 2.2 忽略指定属性

```java
TargetUser target = new TargetUser();
Beans.copyProperties(source, target, "age");
```

输入：`name=ytora, age=18`  
输出：`target.name=ytora, target.age=null`

### 2.3 Bean 转 Map

```java
Map<String, Object> map = Beans.toMap(source, true, "address");
```

输入：Bean 中 `name=ytora, age=18, address=null`  
输出：`{name=ytora, age=18}`

### 2.4 Map 转 Bean

```java
Map<String, Object> map = new HashMap<String, Object>();
map.put("name", "ytora");
map.put("age", "18");
map.put("enabled", "1");

User user = Beans.mapToBean(map, User.class);
```

输入：`{name=ytora, age=18, enabled=1}`  
输出：`user.name=ytora, user.age=18, user.enabled=true`

说明：当值类型不一致时，会尝试走 `Converts` 自动转换；无法转换的字段会被跳过。



## 3 集合工具

🍌提供常见集合操作，重点覆盖“查一个”“做集合运算”“批量处理”和“简单统计”。

### 3.1 找到唯一项

```java
Optional<String> result = Colls.one(
        Arrays.asList("A", "B", "C"),
        "B"::equals
);
```

输入：`[A, B, C]`  
输出：`Optional[B]`

### 3.2 交集、并集、差集

```java
List<Integer> left = Arrays.asList(1, 2, 3);
List<Integer> right = Arrays.asList(3, 4, 5);

List<Integer> intersection = Colls.intersection(left, right);
List<Integer> union = Colls.union(left, right);
List<Integer> diff = Colls.diff(left, right);
```

输入：`[1,2,3]` 和 `[3,4,5]`  
输出：

- `intersection -> [3]`
- `union -> [1, 2, 3, 4, 5]`
- `diff -> [1, 2]`

### 3.3 映射、过滤、分组

```java
List<String> names = Arrays.asList("alice", "bob", "amy");

List<String> upper = Colls.map(names, String::toUpperCase);
List<String> filtered = Colls.filter(names, name -> name.startsWith("a"));
Map<Integer, List<String>> grouped = Colls.groupBy(names, String::length);
```

输入：`[alice, bob, amy]`  
输出：

- `upper -> [ALICE, BOB, AMY]`
- `filtered -> [alice, amy]`
- `grouped -> {3=[bob, amy], 5=[alice]}`

### 3.4 分页、分批、去重

```java
List<Integer> page = Colls.page(Arrays.asList(1, 2, 3, 4, 5), 2, 2);
List<List<Integer>> parts = Colls.partition(Arrays.asList(1, 2, 3, 4, 5), 2);
List<Integer> distinct = Colls.distinct(Arrays.asList(1, 1, 2, 3, 3));
```

输入：`[1,2,3,4,5]`  
输出：

- `page -> [3, 4]`
- `parts -> [[1, 2], [3, 4], [5]]`
- `distinct -> [1, 2, 3]`

### 3.4 频次和统计

```java
Map<String, Long> frequency = Colls.frequency(Arrays.asList("a", "b", "a", "c", "a"));
Optional<String> most = Colls.mostFrequent(Arrays.asList("a", "b", "a", "c", "a"));
OptionalDouble avg = Colls.average(Arrays.asList(10, 20, 30));
```

输入：`[a, b, a, c, a]`  
输出：

- `frequency -> {a=3, b=1, c=1}`
- `most -> Optional[a]`
- `avg -> OptionalDouble[20.0]`

补充：`Colls` 还提供 `first`、`last`、`get`、`reverse`、`shuffle`、`listOf`、`setOf`、`repeat`、`range`、`sum`、`max`、`min`、`arrToList` 等便捷方法。



## 4 类型转换工具

🌰把一个值转换成另一个类型，默认已经内置常见字符串、数字、日期、Java 8 时间类型之间的转换器。

### 4.1 基础转换

```java
Integer age = Converts.convert("18", Integer.class);
Long count = Converts.convert("18", Long.class);
LocalDate date = Converts.convert("2026-04-21", LocalDate.class);
LocalDateTime time = Converts.convert("2026-04-21 10:30:15", LocalDateTime.class);
String value = Converts.convert(18, String.class);
```

输入：字符串和数字  
输出：

- `"18" -> 18`
- `"2026-04-21" -> 2026-04-21`
- `"2026-04-21 10:30:15" -> 2026-04-21T10:30:15`
- `18 -> "18"`

### 4.2 带默认值的转换

```java
Integer age = Converts.convert("abc", Integer.class, 9);
Integer fallback = Converts.convert(null, Integer.class, 9);
```

输入：`"abc"`、`null`  
输出：`9`

### 4.3 注册自定义转换器

```java
Converts.get().addConverter(String.class, Boolean.class, new Converter<String, Boolean>() {
    @Override
    public Boolean convert(String source) {
        return "Y".equalsIgnoreCase(source);
    }

    @Override
    public String reverseConvert(Boolean source) {
        return Boolean.TRUE.equals(source) ? "Y" : "N";
    }
});

Boolean enabled = Converts.convert("Y", Boolean.class);
```

输入：`"Y"`  
输出：`true`

默认已覆盖的常见方向包括：

- `String <-> Integer / Long / BigInteger`
- `String <-> Date / LocalDate / LocalDateTime`
- `Date <-> LocalDate / LocalDateTime`
- `Double <-> Integer / String`
- `Number <-> Boolean`



## 5 Excel工具

🍍封装了Bean <-> Excel的互转工具方法

### 5.1 写入和读取 Map

```java
List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
Map<String, Object> row = new LinkedHashMap<String, Object>();
row.put("name", "ytora");
row.put("age", 18);
rows.add(row);

byte[] bytes = Excels.write(rows);
List<Map<String, Object>> result = Excels.read(new ByteArrayInputStream(bytes));
```

输入：`[{name=ytora, age=18}]`  
输出：读取结果仍为 `[{name=ytora, age=18}]`

### 5.2 指定列头写出

```java
List<ExcelColumn> columns = Arrays.asList(
        new ExcelColumn("name", "姓名", 20, ""),
        new ExcelColumn("age", "年龄", 20, "0")
);

byte[] bytes = Excels.write(rows, columns);
```

输入：属性名 `name / age`  
输出：Excel 表头为 `姓名 / 年龄`

### 5.3  Bean 直接读写

```java
User user = new User();
user.setName("ytora");
user.setAge(18);
user.setBirthday(LocalDate.of(2026, 4, 21));

List<User> users = Arrays.asList(user);
byte[] bytes = Excels.writeBeans(users);
List<User> result = Excels.readBeans(new ByteArrayInputStream(bytes), User.class);
```

输入：`User(name=ytora, age=18, birthday=2026-04-21)`  
输出：读回后的 Bean 保持一致

### 5.4 用注解定义表头、列顺序、格式和文件名

```java
@Excel(value = "users.xlsx", headerRowIndex = 1)
public class User {

    @Excel(value = "姓名", index = 1, width = 30)
    private String name;

    @Excel(value = "年龄", index = 0, format = "0")
    private int age;

    @Excel(value = "生日", index = 2, format = "yyyy-mm-dd")
    private LocalDate birthday;

    @Excel(ignore = true)
    private String secret;
}
```

输入：Bean 列表  
输出：

- 表头顺序按 `index`
- 列宽和单元格格式按注解生效
- `secret` 不写出也不读回
- 如果把目录传给 `writeBeans`，会自动使用 `users.xlsx` 作为文件名



## 6 ID工具

🍐提供四种常用 ID 生成方式，分别适合分布式、通用字符串主键、有序字符串主键和单机递增编号。

```java
Long snowflakeId = Ids.nextSnowflakeId();
String uuid = Ids.nextUuid();
String ulid = Ids.nextUlid();
Long localId = Ids.nextLocalId();
```

示例输出：

- `nextSnowflakeId() -> 1916773661459621888`
- `nextUuid() -> 550e8400-e29b-41d4-a716-446655440000`
- `nextUlid() -> 01JSB6QK6N7A9VVV0K5S8VKT8B`
- `nextLocalId() -> 1`

选择建议：

- 需要 long 型分布式 ID：用 `nextSnowflakeId`
- 需要通用字符串主键：用 `nextUuid`
- 需要大致按时间有序的字符串 ID：用 `nextUlid`
- 只在单机内递增：用 `nextLocalId`



## 7 IO 模块

🍋统一处理输入流、输出流、Reader、Writer 的读取、复制、写入和关闭。默认文本编码为 UTF-8。

### 7.1 读取流内容

```java
InputStream input = Ios.toInputStream("你好，ytora");
byte[] bytes = Ios.toByteArray(input);
String text = Ios.toString(Ios.toInputStream("你好，ytora"));
```

输入：`"你好，ytora"`  
输出：

- `toByteArray -> UTF-8 字节数组`
- `toString -> "你好，ytora"`

### 7.2 复制流

```java
ByteArrayInputStream input = new ByteArrayInputStream("abcd".getBytes(StandardCharsets.UTF_8));
ByteArrayOutputStream output = new ByteArrayOutputStream();
long count = Ios.copy(input, output);
```

输入：`"abcd"`  
输出：

- `count -> 4`
- `output -> "abcd"`

### 7.3 写入输出流

```java
ByteArrayOutputStream output = new ByteArrayOutputStream();
Ios.write("hello", output);
```

输入：`"hello"`  
输出：输出流内容为 `"hello"`

### 7.4 包装和关闭

```java
InputStream input = Ios.toInputStream("abc");
Reader reader = Ios.toReader("hello");
Ios.close(input);
Ios.close(reader);
```

适合做临时测试、内存流转换和通用工具封装。



## 8 数字模块

🍓基于 `BigDecimal` 做精确计算，避免浮点误差，也把取余、取模、百分比统一成稳定语义。

### 8.1 四则运算

```java
BigDecimal add = Nums.add(1.1D, 2.2D);
BigDecimal subtract = Nums.subtract(10, 2.3D);
BigDecimal multiply = Nums.multiply(2, 3.0D);
BigDecimal divide = Nums.divide(10, 3, 2);
```

输入：`1.1 + 2.2`、`10 - 2.3`、`2 * 3.0`、`10 / 3`  
输出：

- `add -> 3.3`
- `subtract -> 7.7`
- `multiply -> 6.0`
- `divide -> 3.33`

说明：`Nums.divide(10, 3)` 会抛异常，因为结果无法精确表示；这时应使用带精度的重载方法。

### 8.2 求余、取模、百分比

```java
BigDecimal remainder = Nums.remainder(-10, 3);
BigDecimal mod = Nums.mod(-10, 3);
BigDecimal percent = Nums.percent(1, 4, 2);
```

输入：`-10` 和 `3`，`1 / 4`  
输出：

- `remainder -> -1`
- `mod -> 2`
- `percent -> 25.00`

区别：

- `remainder` 的符号跟随被除数
- `mod` 的结果始终非负

## 9 文本模块

🥬覆盖字符串处理中最常见的空值、切割、拼接、格式化、命名转换和展示格式化需求。

### 9.1 判空和默认值

```java
boolean blank = Strs.isBlank("   ");
String value = Strs.defaultIfBlank("   ", "N/A");
String trimmed = Strs.trimToNull("  abc  ");
```

输入：空白字符串和带空格文本  
输出：

- `isBlank -> true`
- `defaultIfBlank -> "N/A"`
- `trimToNull -> "abc"`

### 9.2 查找和裁剪

```java
String before = Strs.substringBefore("abc-def", "-");
String after = Strs.substringAfter("abc-def", "-");
String between = Strs.substringBetween("user[name]", "[", "]");
String noSuffix = Strs.removeSuffix("report.txt", ".txt");
```

输入：`abc-def`、`user[name]`、`report.txt`  
输出：

- `before -> "abc"`
- `after -> "def"`
- `between -> "name"`
- `noSuffix -> "report"`

### 9.3 拼接、切割、模板格式化

```java
String joined = Strs.join(Arrays.asList("a", "b", "c"), ",");
List<String> parts = Strs.splitAndTrimIgnoreEmpty(" a ,   , c ", ",");
String msg = Strs.format("你好，{}", "ytora");

Map<String, Object> args = new HashMap<String, Object>();
args.put("name", "ytora");
args.put("count", 3);
String named = Strs.formatNamed("{name} 有 {count} 条消息", args);
```

输入：列表、逗号文本、模板  
输出：

- `joined -> "a,b,c"`
- `parts -> [a, c]`
- `msg -> "你好，ytora"`
- `named -> "ytora 有 3 条消息"`

### 9.4 随机串、命名风格、展示格式

```java
String camel = Strs.toCamelCase("user_name");
String snake = Strs.toSnakeCase("HTTPServerURL");
String kebab = Strs.toKebabCase("userName");
String padded = Strs.fillZero(123, 5);
String size = Strs.formatSize(1536);
long bytes = Strs.parseSize("1.5 KB");
String uptime = Strs.formatMillis(183_000);
```

输入：命名字符串、数字、字节数、毫秒数  
输出：

- `camel -> "userName"`
- `snake -> "http_server_url"`
- `kebab -> "user-name"`
- `padded -> "00123"`
- `size -> "1.50 KB"`
- `bytes -> 1536`
- `uptime -> "3分钟3秒"`

补充：`Strs` 还提供 `repeat`、`truncate`、`leftPad`、`isNumeric`、`isAlphanumeric`、`countMatches`、`normalizeWhitespace`、`randomNumber`、`randomString` 等常用能力。

### 9.5 Chars

做单字符级别的判断和全角 / 半角转换，适合输入规范化、文本清洗和中文场景。

```java
boolean ascii = Chars.isAscii('A');
boolean chinese = Chars.isChinese('中');
String half = Chars.toHalfWidth("ＡＢＣ　１２３");
String full = Chars.toFullWidth("ABC 123");
```

输入：`'A'`、`'中'`、`"ＡＢＣ　１２３"`、`"ABC 123"`  
输出：

- `ascii -> true`
- `chinese -> true`
- `half -> "ABC 123"`
- `full -> "ＡＢＣ　１２３"`

### 9.6 Encodes

处理 URL 编解码、Base64 编解码和 HTML 转义。

```java
String encoded = Encodes.urlEncode("a b+c");
String decoded = Encodes.urlDecode("a+b%2Bc");
String base64 = Encodes.base64Encode("ytora");
String plain = Encodes.base64DecodeToString("eXRvcmE=");
String html = Encodes.htmlEscape("<div>&\"'</div>");
```

输入：普通文本、URL 编码文本、HTML 文本  
输出：

- `urlEncode -> "a+b%2Bc"`
- `urlDecode -> "a b+c"`
- `base64Encode -> "eXRvcmE="`
- `base64DecodeToString -> "ytora"`
- `htmlEscape -> "&lt;div&gt;&amp;&quot;&#39;&lt;/div&gt;"`

### 9.7 Regs

用来做常见文本校验，覆盖中文、数字、日期、邮箱、手机号、座机、身份证、URL、IPv4、UUID 等场景。

```java
boolean date = Regs.isDate("2024-02-29");
boolean email = Regs.isEmail("user@example.com");
boolean mobile = Regs.isMobile("13800138000");
boolean phone = Regs.isPhone("010-88886666");
boolean idCard = Regs.isIdCard("11010519491231002X");
boolean url = Regs.isUrl("https://example.com/path?q=1");
boolean ipv4 = Regs.isIpv4("192.168.1.1");
boolean uuid = Regs.isUuid("550e8400-e29b-41d4-a716-446655440000");
```

输入：常见业务文本  
输出：以上示例均为 `true`

### 9.8 Printers

把多行文本打印成更易读的块，适合日志、SQL、配置片段和调试输出。

```java
String text = "SELECT id, name\nFROM sys_user\nWHERE enabled = 1";
String block = Printers.format(text, Printers.PrintStyle.NUMBERED);
System.out.println(block);
```

输出示例：

```text
┌──────────────────────────┐
│ 1| SELECT id, name       │
│ 2| FROM sys_user         │
│ 3| WHERE enabled = 1     │
└──────────────────────────┘
```

可选风格包括：`BOX_ASCII`、`BOX_HEAVY`、`DOUBLE_LINE`、`MINIMAL`、`NUMBERED`、`COMMENT_BLOCK`。



## 10 时间工具

🧀统一处理 `Date`、`LocalDate`、`LocalDateTime` 三种日期类型的格式化、解析、计算、比较和转换。⌚️

### 10.1 格式化与解析

```java
String date = Dates.format(LocalDate.of(2026, 4, 12));
String time = Dates.format(LocalDateTime.of(2026, 4, 12, 10, 30, 15));
LocalDate parsedDate = Dates.parseDate("2026-04-12");
LocalDateTime parsedTime = Dates.parseDateTime("2026-04-12 10:30:15");
```

输入：日期对象和日期字符串  
输出：

- `date -> "2026-04-12"`
- `time -> "2026-04-12 10:30:15"`
- `parsedDate -> 2026-04-12`
- `parsedTime -> 2026-04-12T10:30:15`

### 10.2 起止时间和加减运算

```java
LocalDate day = LocalDate.of(2026, 4, 12);
LocalDateTime start = Dates.startOfDay(day);
LocalDateTime end = Dates.endOfDay(day);
LocalDate plusDays = Dates.plusDays(day, 3);
LocalDate plusMonths = Dates.plusMonths(day, 2);
```

输入：`2026-04-12`  
输出：

- `start -> 2026-04-12T00:00`
- `end -> 2026-04-12T23:59:59.999999999`
- `plusDays -> 2026-04-15`
- `plusMonths -> 2026-06-12`

### 10.3 比较和区间判断

```java
boolean before = Dates.isBefore(
        LocalDate.of(2026, 4, 12),
        LocalDate.of(2026, 4, 13)
);

boolean between = Dates.isBetween(
        LocalDate.of(2026, 4, 12),
        LocalDate.of(2026, 4, 12),
        LocalDate.of(2026, 4, 13)
);
```

输入：两个日期和一个区间  
输出：

- `before -> true`
- `between -> true`

### 10.4 类型转换和时间戳

```java
Date utilDate = Dates.toDate(LocalDateTime.of(2026, 4, 12, 10, 30, 15));
LocalDate localDate = Dates.toLocalDate(utilDate);
Long epoch = Dates.toEpochMilli(LocalDateTime.of(2026, 4, 12, 10, 30, 15));
LocalDateTime restored = Dates.fromEpochMilli(epoch);
```

输入：`LocalDateTime` 和毫秒值  
输出：

- `toDate -> Date`
- `toLocalDate -> 2026-04-12`
- `toEpochMilli -> 毫秒时间戳`
- `fromEpochMilli -> 原始 LocalDateTime`

### 10.5 BusinessDates

在不引入节假日表的前提下，按“周六周日为非工作日”的规则处理工作日计算。

```java
LocalDate friday = LocalDate.of(2026, 4, 10);

boolean weekend = BusinessDates.isWeekend(LocalDate.of(2026, 4, 11));
boolean businessDay = BusinessDates.isBusinessDay(friday);
LocalDate next = BusinessDates.nextBusinessDay(friday);
LocalDate previous = BusinessDates.previousBusinessDay(LocalDate.of(2026, 4, 13));
LocalDate plus = BusinessDates.plusBusinessDays(friday, 3);
```

输入：周五、周六、周一  
输出：

- `isWeekend(2026-04-11) -> true`
- `isBusinessDay(2026-04-10) -> true`
- `nextBusinessDay(2026-04-10) -> 2026-04-13`
- `previousBusinessDay(2026-04-13) -> 2026-04-10`
- `plusBusinessDays(2026-04-10, 3) -> 2026-04-15`

### 10.6 Crons

解析和计算 Cron 表达式，支持 5 段 Unix Cron，也支持 6/7 段 Quartz Cron。

```java
boolean standard = Crons.isStandard("*/5 8-18 * * 1-5");
boolean quartz = Crons.isQuartz("0 0/5 * * * ?");
boolean valid = Crons.isValid("0 15 10 ? * MON-FRI");

Long next = Crons.nextTime("0 0/15 9-10 ? * MON-FRI", System.currentTimeMillis());
CronExpression cron = Crons.parse("0 0 12 ? * MON-FRI");
```

输入：标准 Cron 和 Quartz Cron  
输出：

- `isStandard("*/5 8-18 * * 1-5") -> true`
- `isQuartz("0 0/5 * * * ?") -> true`
- `isValid("0 15 10 ? * MON-FRI") -> true`
- `nextTime(...) -> 下一次执行时间戳`

支持的 Quartz 特性包括 `?`、`L`、`W`、`LW`、`#`，例如：

- `0 0 9 LW * ?`：每月最后一个工作日 9 点
- `0 0 10 ? * 3#2`：每月第二个周二 10 点



## 11 树

🌳把扁平列表组装成树，并支持按层级遍历。

### 11.1 列表转树

```java
List<MenuNode> nodes = Arrays.asList(
        new MenuNode("1", "0", "系统"),
        new MenuNode("2", "1", "用户"),
        new MenuNode("3", "1", "角色")
);

List<MenuNode> tree = Trees.toTree(nodes);
```

输入：`id / pid` 扁平列表  
输出：根节点是 `系统`，其子节点为 `用户`、`角色`

要求：节点类型实现 `ITree<T>`，至少提供 `getId()`、`getPid()`、`getChildren()`、`setChildren()`。

### 11.1 遍历树

```java
Trees.toTree(nodes, new NodeVisitor<MenuNode>() {
    @Override
    public void accept(int level, MenuNode node, MenuNode parent) {
        System.out.println(level + ":" + node.getName());
    }
});
```

输入：树节点列表  
输出：

- `0:系统`
- `1:用户`
- `1:角色`
