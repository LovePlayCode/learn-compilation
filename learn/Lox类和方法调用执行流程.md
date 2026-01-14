# Lox 类和方法调用执行流程分析

本文档分析 Lox 解释器如何执行以下代码：

```lox
class Bacon {
  eat() {
    print "Crunch crunch crunch!";
  }
}

Bacon().eat(); // Prints "Crunch crunch crunch!".
```

---

## 1. 整体架构

```
源代码 → Scanner → Parser → AST → Interpreter → 执行结果
                              ↓
                    ┌─────────────────┐
                    │   核心组件       │
                    ├─────────────────┤
                    │ LoxClass        │ ← 类的运行时表示
                    │ LoxInstance     │ ← 实例的运行时表示
                    │ LoxFunction     │ ← 函数/方法的运行时表示
                    │ LoxCallable     │ ← 可调用对象接口
                    └─────────────────┘
```

---

## 2. 关键组件

### 2.1 LoxCallable 接口

```java
interface LoxCallable {
    int arity();                                    // 参数数量
    Object call(Interpreter interpreter, List<Object> arguments);  // 执行调用
}
```

**作用**：统一函数和类的调用方式。`LoxFunction` 和 `LoxClass` 都实现此接口。

### 2.2 LoxClass - 类的运行时表示

```java
class LoxClass implements LoxCallable {
    final String name;
    private final Map<String, LoxFunction> methods;  // 方法表

    // 查找方法
    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return null;
    }

    @Override
    public int arity() { return 0; }

    // 调用类 = 创建实例
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        return instance;
    }
}
```

**关键点**：
- 类实现 `LoxCallable`，所以 `Bacon()` 可以像函数一样调用
- `call()` 方法创建并返回新的 `LoxInstance`

### 2.3 LoxInstance - 实例的运行时表示

```java
class LoxInstance {
    private LoxClass klass;                          // 所属类
    private final Map<String, Object> fields = new HashMap<>();  // 字段

    // 获取属性（字段或方法）
    Object get(Token name) {
        // 优先查找字段
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }
        // 其次查找方法
        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method;
        
        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    // 设置字段
    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
}
```

**关键点**：
- 字段存储在实例级别
- `get()` 先查字段，再查方法（字段优先）

### 2.4 LoxFunction - 函数/方法的运行时表示

```java
class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;  // AST 节点
    private final Environment closure;        // 闭包环境

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // 基于闭包创建新环境
        Environment environment = new Environment(closure);
        
        // 绑定参数
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }
        
        // 执行函数体
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
}
```

---

## 3. 执行流程详解

### 第一阶段：类声明 `class Bacon { ... }`

```
visitClassStmt(Bacon)
    │
    ├─→ 1. environment.define("Bacon", null)  // 先占位
    │
    ├─→ 2. 遍历方法，创建 LoxFunction
    │      methods = { "eat": LoxFunction(eat, closure) }
    │
    ├─→ 3. 创建 LoxClass
    │      klass = LoxClass("Bacon", methods)
    │
    └─→ 4. environment.assign("Bacon", klass)  // 绑定类对象
```

**执行后环境**：
```
globals: { "Bacon": LoxClass("Bacon", {"eat": LoxFunction}) }
```

### 第二阶段：调用 `Bacon().eat()`

这是一个链式调用，分解为：

```
Bacon().eat()
└─┬─┘ └──┬──┘
  │      │
  │      └─→ 方法调用：对 Bacon() 的结果调用 eat()
  │
  └─→ 类调用：创建 Bacon 实例
```

#### 步骤 1：解析 `Bacon()`

```
visitCallExpr(Call(Identifier("Bacon"), []))
    │
    ├─→ 1. callee = evaluate(Identifier("Bacon"))
    │      → 从环境中查找 "Bacon"
    │      → 返回 LoxClass("Bacon", {...})
    │
    ├─→ 2. arguments = []  // 无参数
    │
    ├─→ 3. 检查 callee instanceof LoxCallable → true
    │
    └─→ 4. callee.call(interpreter, [])
           → LoxClass.call()
           → return new LoxInstance(this)
           → 返回 LoxInstance(Bacon)
```

#### 步骤 2：解析 `.eat`

```
visitGetExpr(Get(LoxInstance, "eat"))
    │
    └─→ instance.get("eat")
        │
        ├─→ 1. fields.containsKey("eat") → false
        │
        └─→ 2. klass.findMethod("eat")
               → 返回 LoxFunction(eat, closure)
```

#### 步骤 3：解析 `()`

```
visitCallExpr(Call(LoxFunction(eat), []))
    │
    ├─→ 1. callee = LoxFunction(eat)
    │
    ├─→ 2. arguments = []
    │
    └─→ 3. callee.call(interpreter, [])
           │
           ├─→ 创建新环境 Environment(closure)
           │
           └─→ executeBlock(eat.body, environment)
               │
               └─→ visitPrintStmt(...)
                   → 输出 "Crunch crunch crunch!"
```

---

## 4. 完整调用栈

```
Bacon().eat()

1. visitCallExpr(Bacon())
   └─→ evaluate(Bacon) → LoxClass
   └─→ LoxClass.call() → LoxInstance
   
2. visitGetExpr(_.eat)
   └─→ LoxInstance.get("eat") → LoxFunction
   
3. visitCallExpr(_())
   └─→ LoxFunction.call()
       └─→ executeBlock(eat.body)
           └─→ visitPrintStmt("Crunch...")
               └─→ 输出 "Crunch crunch crunch!"
```

---

## 5. AST 结构

`Bacon().eat()` 的 AST：

```
            Call
           /    \
        Get      []  (无参数)
       /   \
    Call   "eat"
   /    \
Identifier  []  (无参数)
  "Bacon"
```

对应的解析过程：

```java
// Parser 中的 call() 方法
private Expr call() {
    Expr expr = primary();  // Identifier("Bacon")
    
    while (true) {
        if (match(LEFT_PAREN)) {
            expr = finishCall(expr);  // Call(Bacon, [])
        } else if (match(DOT)) {
            Token name = consume(IDENTIFIER, "...");
            expr = new Expr.Get(expr, name);  // Get(Call(Bacon,[]), "eat")
        } else {
            break;
        }
    }
    // 最后一个 () 在外层处理
    return expr;
}
```

---

## 6. 组件关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                        Interpreter                               │
├─────────────────────────────────────────────────────────────────┤
│  visitClassStmt()                                                │
│    └─→ 创建 LoxClass (包含 methods Map)                          │
│                                                                  │
│  visitCallExpr()                                                 │
│    └─→ 调用 LoxCallable.call()                                   │
│         ├─→ LoxFunction.call() → 执行函数                        │
│         └─→ LoxClass.call() → 创建 LoxInstance                   │
│                                                                  │
│  visitGetExpr()                                                  │
│    └─→ LoxInstance.get() → 返回字段或方法                        │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐     implements     ┌──────────────┐
│ LoxCallable  │◄──────────────────│  LoxClass    │
│  (interface) │                    │              │
│  - arity()   │                    │  - name      │
│  - call()    │                    │  - methods   │
└──────────────┘                    │  - call() ──────→ 创建 LoxInstance
       ▲                            └──────────────┘
       │ implements                        │
       │                                   │ findMethod()
┌──────────────┐                           ▼
│ LoxFunction  │                    ┌──────────────┐
│              │                    │ LoxInstance  │
│  - closure   │◄───────────────────│              │
│  - call()    │   get() 返回方法   │  - klass     │
└──────────────┘                    │  - fields    │
                                    │  - get()     │
                                    └──────────────┘
```

---

## 7. 关键设计思想

### 7.1 类即可调用对象

```java
class LoxClass implements LoxCallable { ... }
```

让类实现 `LoxCallable`，使得 `Bacon()` 和 `someFunction()` 的调用方式完全一致。

### 7.2 方法存储在类，字段存储在实例

```
LoxClass                    LoxInstance
├─ methods: {eat: ...}      ├─ klass → LoxClass
                            ├─ fields: {}
```

- **方法**：所有实例共享，存储在类中
- **字段**：每个实例独立，存储在实例中

### 7.3 属性查找优先级

```java
Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
        return fields.get(name.lexeme);  // 1. 先查字段
    }
    LoxFunction method = klass.findMethod(name.lexeme);
    if (method != null) return method;   // 2. 再查方法
    throw new RuntimeError(...);
}
```

字段可以覆盖方法（shadowing）。

---

## 8. 总结

| 代码 | 触发的方法 | 返回值 |
|------|-----------|--------|
| `class Bacon {...}` | `visitClassStmt` | 无（定义类） |
| `Bacon()` | `visitCallExpr` → `LoxClass.call()` | `LoxInstance` |
| `.eat` | `visitGetExpr` → `LoxInstance.get()` | `LoxFunction` |
| `()` | `visitCallExpr` → `LoxFunction.call()` | 无（执行方法） |
