# 字符串拼接扩展实现

## 概述

本文档记录了 Lox 解释器中 `+` 运算符的扩展实现，使其支持字符串与其他类型的自动转换拼接。

## 需求背景

许多编程语言（如 JavaScript、Python）定义了 `+` 运算符的字符串拼接行为：如果任一操作数是字符串，则将另一个操作数转换为字符串并进行拼接。

例如：
- `"scone" + 4` → `"scone4"`
- `42 + " is the answer"` → `"42 is the answer"`
- `true + " story"` → `"true story"`

## 实现方案

### 修改位置

文件：`src/com/craftinginterpreters/lox/Interpreter.java`
方法：`visitBinaryExpr(Expr.Binary expr)`

### 原有实现

```java
case PLUS:
    if (left instanceof Double && right instanceof Double) {
        return (double) left + (double) right;
    }

    if (left instanceof String && right instanceof String) {
        return (String) left + (String) right;
    }
    throw new RuntimeError(expr.operator,
            "Operands must be two numbers or two strings.");
```

**问题**：只支持两个数字相加或两个字符串拼接，不支持混合类型。

### 扩展后实现

```java
case PLUS:
    // 优先处理数字加法
    if (left instanceof Double && right instanceof Double) {
        return (double) left + (double) right;
    }

    // 如果任一操作数是字符串，将另一个转换为字符串并拼接
    if (left instanceof String || right instanceof String) {
        return stringify(left) + stringify(right);
    }

    throw new RuntimeError(expr.operator,
            "Operands must be two numbers or at least one string.");
```

### 关键改动

1. **条件判断变化**：从 `&&`（两者都是字符串）改为 `||`（任一是字符串）
2. **类型转换**：使用 `stringify()` 方法将非字符串操作数转换为字符串表示
3. **错误信息更新**：反映新的操作数要求

### stringify() 方法

该方法已存在于 `Interpreter` 类中，用于将任意 Lox 值转换为字符串表示：

```java
private String stringify(Object object) {
    if (object == null)
        return "nil";

    if (object instanceof Double) {
        String text = object.toString();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }

    return object.toString();
}
```

**转换规则**：
- `null` → `"nil"`
- `Double` → 去除尾部 `.0` 的数字字符串（如 `42.0` → `"42"`）
- 其他类型 → 调用 `toString()` 方法

## 处理流程

```
+-------------------+
| 进入 PLUS case    |
+-------------------+
         |
         v
+-------------------+
| 两者都是 Double?  |----是----> 执行数字加法
+-------------------+
         |
         否
         v
+-------------------+
| 任一是 String?    |----是----> stringify 转换 + 字符串拼接
+-------------------+
         |
         否
         v
+-------------------+
| 抛出 RuntimeError |
+-------------------+
```

## 测试用例

| 表达式 | 预期结果 | 说明 |
|--------|----------|------|
| `"scone" + 4` | `"scone4"` | 字符串 + 数字 |
| `4 + "scone"` | `"4scone"` | 数字 + 字符串 |
| `"hello" + " world"` | `"hello world"` | 字符串 + 字符串 |
| `42 + 8` | `50` | 数字 + 数字 |
| `true + " story"` | `"true story"` | 布尔值 + 字符串 |
| `"value: " + nil` | `"value: nil"` | 字符串 + nil |
| `3.14 + " is pi"` | `"3.14 is pi"` | 浮点数 + 字符串 |

## 设计考量

### 为什么数字加法优先？

将数字加法放在字符串拼接之前检查，确保 `42 + 8` 返回 `50` 而不是 `"428"`。这符合大多数语言的行为：只有当至少一个操作数是字符串时，才进行字符串拼接。

### 隐式类型转换的利弊

**优点**：
- 代码更简洁，减少显式转换调用
- 符合动态语言的使用习惯
- 方便字符串模板和日志输出

**缺点**：
- 可能导致意外的类型转换
- 调试时可能不易发现类型错误
- 与静态类型语言的行为不一致

### 与其他语言的比较

| 语言 | `"scone" + 4` 的行为 |
|------|---------------------|
| JavaScript | `"scone4"` |
| Python | TypeError（需要显式转换） |
| Java | `"scone4"` |
| Ruby | TypeError（需要显式转换） |
| Lox (扩展后) | `"scone4"` |

## 总结

通过简单的条件判断修改，我们扩展了 Lox 的 `+` 运算符，使其支持更灵活的字符串拼接操作。这种设计借鉴了 JavaScript 和 Java 的行为，使得字符串操作更加便捷。
