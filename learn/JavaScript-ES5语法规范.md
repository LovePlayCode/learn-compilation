# JavaScript ES5 精简语法规范（编译原理学习版）

本文档是 ES5 语法的精简版本，移除了位运算、移位运算等复杂特性，专注于编译原理核心概念。

---

## 程序结构

```bnf
Program
    : SourceElements? EOF
    ;

SourceElements
    : SourceElement+
    ;

SourceElement
    : Statement
    | FunctionDeclaration
    ;
```

---

## 语句（Statements）

```bnf
Statement
    : Block
    | VariableStatement
    | EmptyStatement
    | ExpressionStatement
    | IfStatement
    | WhileStatement
    | ForStatement
    | ReturnStatement
    ;
```

### 块语句

```bnf
Block
    : '{' StatementList? '}'
    ;

StatementList
    : Statement+
    ;
```

### 变量声明

```bnf
VariableStatement
    : 'var' VariableDeclarationList ';'
    ;

VariableDeclarationList
    : VariableDeclaration (',' VariableDeclaration)*
    ;

VariableDeclaration
    : IDENTIFIER ('=' AssignmentExpression)?
    ;
```

**示例：**
```javascript
var x;
var a = 1;
var x = 1, y = 2;
```

### 空语句

```bnf
EmptyStatement
    : ';'
    ;
```

### 表达式语句

```bnf
ExpressionStatement
    : Expression ';'
    ;
```

### If 语句

```bnf
IfStatement
    : 'if' '(' Expression ')' Statement ('else' Statement)?
    ;
```

### 循环语句

```bnf
WhileStatement
    : 'while' '(' Expression ')' Statement
    ;

ForStatement
    : 'for' '(' ForInit? ';' Expression? ';' Expression? ')' Statement
    ;

ForInit
    : 'var' VariableDeclarationList
    | Expression
    ;
```

### Return 语句

```bnf
ReturnStatement
    : 'return' Expression? ';'
    ;
```

---

## 函数

```bnf
FunctionDeclaration
    : 'function' IDENTIFIER '(' FormalParameterList? ')' Block
    ;

FunctionExpression
    : 'function' IDENTIFIER? '(' FormalParameterList? ')' Block
    ;

FormalParameterList
    : IDENTIFIER (',' IDENTIFIER)*
    ;
```

**示例：**
```javascript
function add(a, b) {
    return a + b;
}

var mul = function(a, b) {
    return a * b;
};
```

---

## 表达式（Expressions）

### 表达式优先级链（从低到高）

```
Expression          →  逗号表达式
    ↓
AssignmentExpr      →  赋值表达式（右结合）
    ↓
ConditionalExpr     →  三元表达式
    ↓
LogicalORExpr       →  ||
    ↓
LogicalANDExpr      →  &&
    ↓
EqualityExpr        →  == != === !==
    ↓
RelationalExpr      →  < > <= >=
    ↓
AdditiveExpr        →  + -
    ↓
MultiplicativeExpr  →  * / %
    ↓
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

### 逗号表达式

```bnf
Expression
    : AssignmentExpression (',' AssignmentExpression)*
    ;
```

### 赋值表达式

```bnf
AssignmentExpression
    : ConditionalExpression
    | LeftHandSideExpression AssignmentOperator AssignmentExpression
    ;

AssignmentOperator
    : '=' | '+=' | '-=' | '*=' | '/='
    ;
```

**实现要点：** 右结合，先解析左侧，判断是否为赋值运算符后递归。

### 条件（三元）表达式

```bnf
ConditionalExpression
    : LogicalORExpression ('?' AssignmentExpression ':' AssignmentExpression)?
    ;
```

### 逻辑表达式

```bnf
LogicalORExpression
    : LogicalANDExpression ('||' LogicalANDExpression)*
    ;

LogicalANDExpression
    : EqualityExpression ('&&' EqualityExpression)*
    ;
```

### 相等性表达式

```bnf
EqualityExpression
    : RelationalExpression (('==' | '!=' | '===' | '!==') RelationalExpression)*
    ;
```

### 关系表达式

```bnf
RelationalExpression
    : AdditiveExpression (('<' | '>' | '<=' | '>=') AdditiveExpression)*
    ;
```

### 加法表达式

```bnf
AdditiveExpression
    : MultiplicativeExpression (('+' | '-') MultiplicativeExpression)*
    ;
```

### 乘法表达式

```bnf
MultiplicativeExpression
    : UnaryExpression (('*' | '/' | '%') UnaryExpression)*
    ;
```

### 一元表达式

```bnf
UnaryExpression
    : PostfixExpression
    | ('delete' | 'void' | 'typeof' | '++' | '--' | '+' | '-' | '~' | '!') UnaryExpression
    ;
```


### 后缀表达式

```bnf
PostfixExpression
    : LeftHandSideExpression ('++' | '--')?
    ;
```

### 左值表达式（核心难点）

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

Arguments
    : '(' (AssignmentExpression (',' AssignmentExpression)*)? ')'
    ;
```

**示例：**
```javascript
foo()           // 函数调用
obj.method()    // 方法调用
arr[0]          // 索引访问
obj.prop        // 属性访问
new Date()      // 构造函数
```

### 基本表达式

```bnf
PrimaryExpression
    : 'this'
    | IDENTIFIER
    | Literal
    | ArrayLiteral
    | ObjectLiteral
    | '(' Expression ')'
    ;
```

### 字面量

```bnf
Literal
    : NULL | TRUE | FALSE | NUMBER | STRING
    ;

ArrayLiteral
    : '[' (AssignmentExpression (',' AssignmentExpression)*)? ']'
    ;

ObjectLiteral
    : '{' (PropertyAssignment (',' PropertyAssignment)*)? '}'
    ;

PropertyAssignment
    : (IDENTIFIER | STRING | NUMBER) ':' AssignmentExpression
    ;
```

---

## 运算符优先级表（精简版）

| 优先级 | 运算符 | 描述 | 结合性 |
|--------|--------|------|--------|
| 1 | `,` | 逗号 | 左 |
| 2 | `= += -= *= /=` | 赋值 | **右** |
| 3 | `?:` | 条件 | **右** |
| 4 | `\|\|` | 逻辑或 | 左 |
| 5 | `&&` | 逻辑与 | 左 |
| 6 | `== != === !==` | 相等性 | 左 |
| 7 | `< > <= >=` | 关系 | 左 |
| 8 | `+ -` | 加减 | 左 |
| 9 | `* / %` | 乘除 | 左 |
| 10 | `! - + typeof` | 一元前缀 | **右** |
| 11 | `++ --` | 后缀 | - |
| 12 | `()` | 函数调用 | 左 |
| 13 | `. []` | 成员访问 | 左 |

---

## 解析器实现提示

### 1. 二元表达式（左结合）

```java
// 通用模式：AdditiveExpression, MultiplicativeExpression 等
private Expr binaryExpr(Supplier<Expr> operand, TokenType... operators) {
    Expr left = operand.get();
    while (match(operators)) {
        Token op = previous();
        Expr right = operand.get();
        left = new Expr.Binary(left, op, right);
    }
    return left;
}
```

### 2. 赋值表达式（右结合）

```java
private Expr AssignmentExpression() {
    Expr expr = ConditionalExpression();
    
    if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL)) {
        Token op = previous();
        Expr value = AssignmentExpression();  // 递归实现右结合
        
        if (expr instanceof Expr.Identifier) {
            return new Expr.Assign(expr, op, value);
        }
        throw error(op, "Invalid assignment target.");
    }
    return expr;
}
```

### 3. 左值表达式解析策略

```
解析流程：
1. 先解析 PrimaryExpression
2. 循环处理后缀：. [] ()
3. 根据是否有 () 区分 CallExpression 和 MemberExpression
```

---

## 移除的特性（可后续扩展）

- 位运算：`& | ^ ~ << >> >>>`
- for-in 循环
- switch/case
- try/catch/throw
- break/continue
- with 语句
- getter/setter
- instanceof/in 运算符
- delete/void 运算符

---

## 参考资料

- [ECMAScript 5.1 规范](https://262.ecma-international.org/5.1/)
- [Crafting Interpreters](https://craftinginterpreters.com/)
