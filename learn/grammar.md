# Lox 语言文法规范

## 完整的 BNF 文法

基于项目中 `Parser.java` 的实现，Lox 语言的完整文法如下：

### 表达式文法（按优先级从低到高）

```bnf
expression     → comma ;
comma          → equality ( "," equality )* ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
               | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
               | "(" expression ")" ;
```

## 运算符优先级表

| 优先级 | 运算符 | 结合性 | 描述 |
|--------|--------|--------|------|
| 1（最低） | `,` | 左结合 | 逗号运算符 |
| 2 | `==` `!=` | 左结合 | 相等性比较 |
| 3 | `>` `>=` `<` `<=` | 左结合 | 关系比较 |
| 4 | `+` `-` | 左结合 | 加法和减法 |
| 5 | `*` `/` | 左结合 | 乘法和除法 |
| 6（最高） | `!` `-` | 右结合 | 一元运算符 |

## 文法规则详解

### 1. 逗号运算符（Comma Operator）

```bnf
comma → equality ( "," equality )* ;
```

**语义**：
- 从左到右计算操作数
- 丢弃左操作数的结果，返回右操作数的值
- 优先级最低，左结合

**示例**：
```javascript
1 + 2, 3 * 4    // 结果: 12 (先算 1+2=3 丢弃，再算 3*4=12 返回)
a, b, c         // 结果: c 的值
```

### 2. 相等性运算符（Equality）

```bnf
equality → comparison ( ( "!=" | "==" ) comparison )* ;
```

**运算符**：
- `==`：相等比较
- `!=`：不等比较

**示例**：
```javascript
1 == 1          // true
"hello" != "world"  // true
```

### 3. 关系运算符（Comparison）

```bnf
comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
```

**运算符**：
- `>`：大于
- `>=`：大于等于
- `<`：小于
- `<=`：小于等于

### 4. 算术运算符（Term & Factor）

```bnf
term   → factor ( ( "-" | "+" ) factor )* ;
factor → unary ( ( "/" | "*" ) unary )* ;
```

**加减法**（term）：
- `+`：加法
- `-`：减法

**乘除法**（factor）：
- `*`：乘法
- `/`：除法

### 5. 一元运算符（Unary）

```bnf
unary → ( "!" | "-" ) unary | primary ;
```

**运算符**：
- `!`：逻辑非
- `-`：数值取负

**特点**：右结合，可以嵌套（如 `!!true`、`--5`）

### 6. 基本表达式（Primary）

```bnf
primary → NUMBER | STRING | "true" | "false" | "nil"
        | "(" expression ")" ;
```

**字面量**：
- `NUMBER`：数字（如 `123`、`3.14`）
- `STRING`：字符串（如 `"hello"`）
- `true`、`false`：布尔值
- `nil`：空值

**分组**：
- `( expression )`：用括号改变优先级

## 递归下降解析

每个文法规则对应 Parser 中的一个方法：

```java
// 文法规则 → 对应方法
expression() → comma()
comma()      → equality()
equality()   → comparison()
comparison() → term()
term()       → factor()
factor()     → unary()
unary()      → primary()
primary()    → 终结符处理
```

## 示例解析

### 示例 1：`1 + 2 * 3`

**解析过程**：
1. `expression()` → `comma()` → `equality()` → `comparison()` → `term()`
2. `term()` 识别到 `1`，然后看到 `+`
3. 调用 `factor()` 解析 `2 * 3`
4. `factor()` 识别到 `2`，然后看到 `*`，调用 `unary()` 解析 `3`
5. 构建 AST：`(+ 1 (* 2 3))`

### 示例 2：`(1 + 2) * 3`

**解析过程**：
1. `primary()` 识别到 `(`，调用 `expression()` 解析内部
2. 内部解析 `1 + 2`，返回 `(+ 1 2)`
3. 外层 `factor()` 看到 `*`，继续解析 `3`
4. 构建 AST：`(* (group (+ 1 2)) 3)`

### 示例 3：`a, b = 1, 2`

**解析过程**：
1. `comma()` 解析 `a`
2. 看到 `,`，继续解析右侧 `b = 1, 2`
3. 右侧又是一个逗号表达式
4. 构建 AST：`(, a (, (= b 1) 2))`

## 扩展性

当前文法可以轻松扩展：

1. **添加新运算符**：在适当优先级层插入新规则
2. **添加新语句**：扩展 primary 规则
3. **添加函数调用**：在 primary 中添加 `IDENTIFIER "(" args ")"`

这种分层的文法设计使得 Lox 语言具有清晰的语法结构和良好的扩展性。