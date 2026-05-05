# Ytora DSL

面向字符串生成的轻量级模板解析框架，基于 JDK8。

核心模型：

```text
字符串模板 + 上下文参数 = 最终字符串
```

## 1. 当前语义

本版本的 DSL 只保留一种序列类型：`数组`。

不再支持：

- 元组字面量，例如 `("a", 1)`
- 内置 `json(...)` 函数

如果需要 `json(...)`，请由使用者自己实现 `CustomFunc` 后注册到引擎。

## 2. 快速使用

```java
Map<String, Object> context = new LinkedHashMap<String, Object>();
context.put("name", "张三");
context.put("age", 18);

YtoraDslEngine engine = YtoraDslEngine.builder().build();

String result = engine.render(
        "姓名：#{name}\n" +
        "if: age >= 18 {\n" +
        "  成年人\n" +
        "}",
        context
);
```

自定义函数：

```java
YtoraDslEngine engine = YtoraDslEngine.builder()
        .registerFunction(new CustomFunc() {
            @Override
            public String funcName() {
                return "upper";
            }

            @Override
            public Object invoke(Object... args) {
                return String.valueOf(args[0]).toUpperCase();
            }
        })
        .build();
```

## 3. 数据类型

支持以下类型：

- 字符串：`"张三"`、`'李四'`
- 数值：`99`、`3.14`、`-1`
- 布尔：`true`、`false`
- 空值：`null`
- 数组：`["唱", "跳", "rap"]`
- 对象：`{"name": "张三", "age": 18}`

## 4. 表达式

### 4.1 变量

变量默认从根上下文读取：

```text
name
age
address.city
hobbies.0
```

### 4.2 访问表达式

支持：

- 点号访问：`address.city`
- 数组下标点号访问：`hobbies.0`
- 方括号访问：`user["user-name"]`、`hobbies[0]`
- 链式访问：`users.0.address.city`

### 4.3 运算符

支持：

- 比较：`== != > >= < <=`
- 逻辑：`&& || !`
- 算术：`+ - * / %`
- 字符串/数组：`join`、`repeat`
- 位运算：`& | << >>`
- 三元：`condition ? a : b`

说明：

- `+` 两侧都是数值时做数值加法，否则做字符串拼接
- `join` 左侧必须是数组，右侧必须是字符串
- `repeat` 左侧必须是字符串，右侧必须是非负整数
- 位运算只接受整数

当前实现的优先级从高到低为：

1. `()`、`[]`、`.`
2. `!`、一元 `-`
3. `*`、`/`、`%`
4. `+`、`-`
5. `join`、`repeat`
6. `<<`、`>>`
7. `&`
8. `|`
9. `>`、`>=`、`<`、`<=`
10. `==`、`!=`
11. `&&`
12. `||`
13. `?:`

## 5. 占位符

语法：

```text
#{expr}
```

渲染规则：

- 字符串：原样输出
- 数值：转为字符串
- 布尔：输出 `true` / `false`
- `null`：输出空字符串
- 数组、对象：默认不允许直接渲染，会抛出 `RenderValueException`

## 6. 关键字

支持以下关键字：

- `if`
- `else`
- `for`
- `where`
- `when`
- `case`
- `default`
- `set`
- `raw`
- `break`
- `continue`

### 6.1 if / else

```text
if: age >= 18 {
  成年人
} else {
  未成年人
}
```

### 6.2 for / where

```text
for: hobbies where index % 2 == 0 {
  #{index} - #{item}
}
```

规则：

- `for` 的数据源必须是数组
- 循环作用域内自动注入 `item` 和 `index`

### 6.3 when / case / default

```text
when: status {
  case: "ENABLE" {
    启用
  }
  case: "DISABLE" {
    禁用
  }
  default: {
    未知状态
  }
}
```

### 6.4 set

```text
set: cityName = address.city
```

规则：

- 变量名必须匹配：`[A-Za-z_][A-Za-z0-9_]*`
- 以下名称禁止使用：`if else for where when case default set raw break continue true false null item index`
- 不允许变量重名
- 不允许遮蔽外层变量
- 不允许覆盖根上下文中已经存在的变量名

### 6.5 raw

```text
raw: {
  这里面的 #{name} 不会被解析
}
```

### 6.6 break / continue

只能在 `for` 块内部使用。

## 7. 内置函数

当前内置函数只有：

- `empty(value)`
- `notEmpty(value)`
- `default(value, fallback)`
- `len(value)`
- `split(str, separator)`

说明：

- `len` 支持字符串、数组、对象
- `json` 不是内置函数

## 8. 自定义函数

推荐实现 `CustomFunc`：

```java
public interface CustomFunc extends DslFunction {
    String funcName();
}
```

例如：

```java
class JsonFunc implements CustomFunc {
    @Override
    public String funcName() {
        return "json";
    }

    @Override
    public Object invoke(Object... args) {
        return "...";
    }
}
```

## 9. 作用域

支持：

- 根作用域
- 块作用域
- 循环作用域

循环作用域内固定有：

- `item`
- `index`

注意：

- 本版本不允许变量遮蔽
- 只要新变量名与当前可见变量冲突，就会直接报错

## 10. 访问模式

### 10.1 宽松模式

变量不存在、字段不存在、索引越界时返回 `null`。

### 10.2 严格模式

变量不存在、字段不存在、索引越界时直接抛错。

```java
YtoraDslEngine engine = YtoraDslEngine.builder().strictMode().build();
```

## 11. 语法合法性校验

当前实现会主动校验以下问题：

- 未闭合占位符，例如 `#{name`
- 空占位符，例如 `#{}`
- 模板块缺少 `}`
- `else`、`case`、`default` 出现在非法位置
- `break`、`continue` 出现在 `for` 外部
- 非法变量名
- 使用了已废弃的元组写法，例如 `#{(1, 2)}`

## 12. 异常类型

为了让使用者能直接判断失败原因，当前实现提供了明确的异常类型：

- `TemplateSyntaxException`：模板结构错误
- `PlaceholderSyntaxException`：占位符语法错误
- `ExpressionSyntaxException`：表达式语法错误
- `VariableConflictException`：变量命名冲突
- `UnknownVariableException`：变量不存在
- `UnknownMemberException`：对象字段不存在
- `IndexAccessException`：数组索引错误
- `TypeMismatchException`：类型不匹配
- `FunctionNotFoundException`：函数不存在
- `FunctionArgumentException`：函数参数错误
- `RenderValueException`：复合值直接渲染错误
- `ControlFlowUsageException`：控制流关键字使用位置非法

## 13. 完整示例

模板：

```text
姓名：#{name}
年龄：#{age}

if: age >= 18 {
  成年人
} else {
  未成年人
}

for: hobbies where index % 2 == 0 {
  #{index} - #{item}
}

城市：#{default(address.city, "未知")}
爱好合并：#{hobbies join ","}
分隔线：#{"-" repeat 10}
位运算：#{2 << 3}
```

上下文：

```json
{
  "name": "张三",
  "age": 99,
  "hobbies": ["唱", "跳", "rap", "篮球"],
  "address": {
    "city": "京州市"
  }
}
```

输出：

```text
姓名：张三
年龄：99

成年人

0 - 唱
2 - rap

城市：京州市
爱好合并：唱,跳,rap,篮球
分隔线：----------
位运算：16
```
