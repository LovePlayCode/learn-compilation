# LeftHandSideExpression 解析详解

本文档详细分析 JavaScript ES5 中左值表达式（LeftHandSideExpression）的解析策略，包括 MemberExpression、CallExpression 和 PostfixExpression 的实现原理。

---

## 1. 语法结构概览

### 1.1 BNF 定义

```bnf
LeftHandSideExpression
    : CallExpression
    | MemberExpression
    ;

CallExpression
    : MemberExpression Arguments (Arguments | '[' Expression ']' | '.' IDENTIFIER)*
    ;

MemberExpression
    : PrimaryExpression ('[' Expression ']' | '.' IDENTIFIER)*
    | 'new' MemberExpression Arguments?
    ;

PostfixExpression
    : LeftHandSideExpression ('++' | '--')?
    ;

Arguments
    : '(' (AssignmentExpression (',' AssignmentExpression)*)? ')'
    ;
```

### 1.2 表达式优先级链

```
UnaryExpr           →  ! - + typeof
    ↓
PostfixExpr         →  ++ --
    ↓
CallExpr            →  函数调用
    ↓
MemberExpr          →  . []
    ↓
PrimaryExpr         →  字面量、标识符、括号
```

---

## 2. 为什么先解析 MemberExpression 再解析 CallExpression

### 2.1 共同前缀问题

`CallExpression` 和 `MemberExpression` 存在**共同前缀**：

```
MemberExpression  →  foo.bar.baz
CallExpression    →  foo.bar.baz()
                     ^^^^^^^^^^^
                     共同前缀
```

当解析器看到 `foo.bar` 时，无法立即判断这是：
- 单纯的 `MemberExpression`（如 `x = foo.bar`）
- `CallExpression` 的一部分（如 `foo.bar()`）

### 2.2 解析策略：左因子提取

这是递归下降解析器处理共同前缀的标准技术——**左因子提取（Left Factoring）**。

**原始语法（有歧义）：**
```
A → αβ | αγ
```

**提取后：**
```
A  → αA'
A' → β | γ
```

**应用到 LeftHandSideExpression：**
```
原始：
  LeftHandSideExpression → CallExpression | MemberExpression
  
实际解析逻辑：
  LeftHandSideExpression → MemberExpression (Arguments...)?
                           ^^^^^^^^^^^^^^^   ^^^^^^^^^^^^
                           共同前缀 α         区分后缀 β/γ
```

### 2.3 解析流程图

```
          foo.bar
             │
             ▼
    ┌─────────────────┐
    │ MemberExpression │
    └─────────────────┘
             │
             ▼
       下一个 token?
        /        \
      '('        其他
       │          │
       ▼          ▼
  CallExpression  返回 MemberExpression
```

### 2.4 代码实现

```java
private Expr LeftHandSideExpression() {
    // 1. 先解析共同前缀 MemberExpression
    var expr = MemberExpression();

    // 2. 根据下一个 token 决定分支
    if (check(TokenType.LEFT_PAREN)) {
        expr = CallExpression(expr);  // 有 '(' → CallExpression
    }
    return expr;  // 无 '(' → 就是 MemberExpression
}
```

---

## 3. CallExpression 实现

### 3.1 支持的语法

```javascript
foo()              // 简单调用
foo()()            // 链式调用（返回函数再调用）
foo().bar          // 调用后访问属性
foo()[0]           // 调用后访问索引
foo().bar()[0]()   // 混合链式
```

### 3.2 实现代码

```java
/**
 * CallExpression 解析
 * 前提：callee 已经是解析好的 MemberExpression，当前 token 是 '('
 */
private Expr CallExpression(Expr callee) {
    // 处理第一个函数调用
    List<Expr> args = Arguments();
    Token paren = previous();
    Expr expr = new Expr.Call(callee, paren, args);

    // 处理链式：() 或 . 或 []
    while (true) {
        if (check(TokenType.LEFT_PAREN)) {
            // foo()() - 链式函数调用
            args = Arguments();
            paren = previous();
            expr = new Expr.Call(expr, paren, args);
        } else if (match(TokenType.DOT)) {
            // foo().bar - 调用后属性访问
            Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
            expr = new Expr.Member(expr, name, false);
        } else if (match(TokenType.LEFT_BRACKET)) {
            // foo()[0] - 调用后索引访问
            Expr index = Expression();
            consume(TokenType.RIGHT_BRACKET, "Expect ']' after index.");
            expr = new Expr.Member(expr, index, true);
        } else {
            break;
        }
    }
    return expr;
}
```

### 3.3 解析示例

以 `obj.method(1).value[0]()` 为例：

| 步骤 | 操作 | 结果 AST |
|------|------|----------|
| 1 | MemberExpression | `Member(Identifier(obj), "method")` |
| 2 | Arguments `(1)` | `Call(Member(...), [1])` |
| 3 | `.value` | `Member(Call(...), "value")` |
| 4 | `[0]` | `Member(Member(...), 0)` |
| 5 | `()` | `Call(Member(...), [])` |

---

## 4. PostfixExpression 实现

### 4.1 BNF 定义

```bnf
PostfixExpression
    : LeftHandSideExpression ('++' | '--')?
    ;
```

### 4.2 实现要点

1. **先解析 LeftHandSideExpression**
2. **可选匹配** `++` 或 `--`（用 `if` 而非 `while`，因为 `i++++` 是非法的）
3. **验证操作数合法性**：只有 `Identifier` 和 `Member` 可以作为后缀运算符的操作数

### 4.3 代码实现

```java
private Expr PostfixExpression() {
    var expr = LeftHandSideExpression();
    
    if (match(TokenType.PLUS_PLUS, TokenType.MINUS_MINUS)) {
        var operator = previous();
        
        // 验证操作数是否为有效的赋值目标
        if (!(expr instanceof Expr.Identifier || expr instanceof Expr.Member)) {
            throw error(operator, "Invalid left-hand side expression in postfix operation.");
        }
        
        // prefix = false 表示后缀运算符
        expr = new Expr.Unary(operator, expr, false);
    }
    return expr;
}
```

### 4.4 合法与非法示例

| 表达式 | 合法性 | 原因 |
|--------|--------|------|
| `x++` | ✅ 合法 | x 是标识符 |
| `obj.prop++` | ✅ 合法 | 成员访问表达式 |
| `arr[0]++` | ✅ 合法 | 索引访问表达式 |
| `foo()++` | ❌ 非法 | 函数调用结果不可赋值 |
| `(a + b)++` | ❌ 非法 | 表达式结果不可赋值 |
| `5++` | ❌ 非法 | 字面量不可赋值 |

---

## 5. 函数调用的执行：为什么需要 evaluate(callee)

### 5.1 解释器中的调用执行

```java
@Override
public Object visitCallExpr(Expr.Call expr) {
    // 第一步：求值 callee，得到实际的函数对象
    Object callee = evaluate(expr.callee);

    // 第二步：求值所有参数
    List<Object> arguments = new ArrayList<>();
    for (Expr argument : expr.arguments) {
        arguments.add(evaluate(argument));
    }

    // 第三步：执行函数调用
    LoxCallable function = (LoxCallable) callee;
    return function.call(this, arguments);
}
```

### 5.2 为什么需要 evaluate？

`expr.callee` 是 **AST 节点**（语法结构），`evaluate()` 将其求值为 **运行时对象**（实际的函数）。

| 调用语法 | `expr.callee` 的类型 | `evaluate()` 后得到 |
|----------|---------------------|---------------------|
| `foo()` | `Identifier("foo")` | 变量 foo 指向的函数对象 |
| `obj.method()` | `Member(obj, "method")` | obj 的 method 属性（函数） |
| `getFunc()()` | `Call(getFunc, [])` | getFunc() 返回的函数 |
| `arr[0]()` | `Member(arr, 0)` | arr[0] 存储的函数 |

### 5.3 执行流程图

```
  expr.callee          evaluate()           调用
 (AST 节点)    ───────────────────►  (运行时对象)  ───►  function.call()
     │                                    │
Identifier("greet")              实际的函数对象
```

### 5.4 具体例子

```javascript
var greet = function() { return "hello"; };
greet();  // 调用
```

执行 `greet()` 时：

1. **`expr.callee`** = `Identifier("greet")` — 只是一个名字（AST 节点）
2. **`evaluate(expr.callee)`** = 在环境中查找 `greet` 变量 → 得到**函数对象**
3. **`function.call()`** = 执行这个函数，返回 `"hello"`

---

## 6. 总结

| 概念 | 关键点 |
|------|--------|
| 左因子提取 | 处理共同前缀，先解析 MemberExpression，再根据 `(` 判断是否为 CallExpression |
| CallExpression | 支持链式调用 `()`, `.`, `[]` 的混合 |
| PostfixExpression | 可选的 `++`/`--`，需验证操作数是有效左值 |
| evaluate(callee) | 将 AST 节点求值为运行时可调用的函数对象 |

---

## 参考资料

- [ECMAScript 5.1 规范](https://262.ecma-international.org/5.1/)
- [Crafting Interpreters](https://craftinginterpreters.com/)
