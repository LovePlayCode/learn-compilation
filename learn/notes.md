# Lox 解释器学习笔记

## 一、NFA 和 DFA

### 什么是有限自动机？

有限自动机（Finite Automaton）是一种用于识别模式的抽象机器，它在词法分析中用于识别 Token。

### DFA（确定有限自动机）

**DFA** = Deterministic Finite Automaton

**特点：**
1. **确定性**：在任何状态下，对于每个输入字符，**只有一个**确定的下一状态
2. **无 ε 转换**：不存在不消耗任何输入就能转换的边
3. **执行效率高**：每个字符只需查表一次

**Scanner.java 中的 DFA 例子：**

```java
case '/':
    if (match('/')) {
        // A comment goes until the end of the line.
        while (peek() != '\n' && !isAtEnd())
            advance();
    } else {
        addToken(SLASH);
    }
    break;
```

这段代码可以表示为 DFA：

```
     '/'           '/'
[开始] ──→ [状态1] ──→ [注释状态] ──→ 继续读取直到换行
              │
              │ 其他字符
              ↓
         [SLASH Token]
```

### NFA（非确定有限自动机）

**NFA** = Nondeterministic Finite Automaton

**特点：**
1. **非确定性**：在某状态下，对于同一输入字符，可以有**多个**可能的下一状态
2. **允许 ε 转换**：可以不消耗任何输入就转换到另一个状态
3. **更容易构造**：正则表达式可以直接转换为 NFA

### NFA vs DFA 对比

| 特性 | NFA | DFA |
|------|-----|-----|
| 状态转换 | 一个输入可能有多个目标状态 | 一个输入只有一个目标状态 |
| ε 转换 | 允许 | 不允许 |
| 构造难度 | 简单（从正则直接构造） | 复杂（需要子集构造法） |
| 状态数量 | 通常较少 | 可能指数级增长 |
| 执行效率 | 需要回溯或并行模拟 | O(n)，每字符一次查表 |
| 实际应用 | 理论模型、正则引擎 | 词法分析器 |

### 总结

1. **正则表达式 → NFA → DFA → 代码**：这是词法分析器生成的标准流程
2. **Scanner.java**：直接手写 DFA，跳过了 NFA 阶段
3. **工业级工具**（如 Lex/Flex）：自动完成 NFA→DFA 转换并生成代码

---

## 二、Visitor 设计模式

### 核心问题：为什么需要 Visitor 模式？

假设你有多种表达式类型（Binary、Unary、Literal 等），需要对它们执行多种操作（打印、求值、类型检查等）。

**方案一：在每个类中添加方法（❌ 不好）**

```java
class Binary {
    String print() { ... }
    Object evaluate() { ... }
    void typeCheck() { ... }
}
class Unary {
    String print() { ... }
    Object evaluate() { ... }
    void typeCheck() { ... }
}
// 每次添加新操作，都要修改所有类！
```

**方案二：Visitor 模式（✅ 推荐）**

把操作抽离到单独的 Visitor 类中，每个 Visitor 负责一种操作。

### 代码结构解析

#### 1. Expr.java - 被访问的元素

```java
// Visitor 接口：定义了访问每种表达式类型的方法
interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    // ...
}

// 每个表达式类都有 accept() 方法
static class Binary extends Expr {
    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitBinaryExpr(this);
    }
}
```

#### 2. AstPrinter.java - 具体的 Visitor

```java
class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }
    
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }
    
    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }
    // ...
}
```

### 执行流程

表达式 `(-123) * (45.67)` 的打印过程：

```
Main → AstPrinter.print(expression)
     → Binary.accept(this)
     → AstPrinter.visitBinaryExpr(this)
         → 打印左子树：Unary.accept(this) → visitUnaryExpr → ...
         → 打印右子树：Grouping.accept(this) → visitGroupingExpr → ...
     → 返回 "(* (- 123) (group 45.67))"
```

---

## 三、Visitor 模式的权衡

### 场景一：添加新操作（Visitor 模式的优势 ✅）

**需求：给 AST 添加一个"求值器"功能**

| 方式 | 操作 |
|------|------|
| 传统方式 | 修改 12 个 Expr 子类，每个类都要添加 `evaluate()` 方法 |
| Visitor 方式 | 只需新建 1 个 `Interpreter.java` 文件 |

```java
// Visitor 方式：只需创建一个新文件
class Interpreter implements Expr.Visitor<Object> {
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);
        // 计算结果...
    }
    
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }
    // ...
}
```

### 场景二：添加新表达式类型（Visitor 模式的劣势 ❌）

**需求：添加一个三元表达式 `a ? b : c`**

| 方式 | 操作 |
|------|------|
| 传统方式 | 只需创建 1 个新类 |
| Visitor 方式 | 需要修改 Visitor 接口 + 修改所有 Visitor 实现类 |

### 如何选择？

```
你的需求是什么？
    ↓
类型稳定吗？
    ├── 类型固定，操作经常变 → ✅ 用 Visitor 模式（如编译器 AST）
    └── 类型经常变，操作固定 → ❌ 不用 Visitor 模式（如插件系统）
```

**编译器/解释器场景**：
- 表达式类型（Binary、Unary、Literal 等）在语言设计完成后**基本固定**
- 但需要很多操作：打印、求值、类型检查、优化、代码生成...
- 所以 **Visitor 模式非常适合**！
