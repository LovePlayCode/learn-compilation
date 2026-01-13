# JavaScript ES5 语法规范

本文档描述 JavaScript ES5 的语法规范，使用 BNF（巴科斯-诺尔范式）表示。

## 目录

- [程序结构](#程序结构)
- [语句](#语句statements)
- [函数](#函数)
- [表达式](#表达式expressions)
- [运算符优先级](#运算符优先级表)

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

### 语句总览

```bnf
Statement
    : Block
    | VariableStatement
    | EmptyStatement
    | ExpressionStatement
    | IfStatement
    | IterationStatement
    | ContinueStatement
    | BreakStatement
    | ReturnStatement
    | WithStatement
    | SwitchStatement
    | ThrowStatement
    | TryStatement
    | DebuggerStatement
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
    : IDENTIFIER Initializer?
    ;

Initializer
    : '=' AssignmentExpression
    ;
```

**示例：**
```javascript
var x;
var a = 1;
var x = 1, y = 2, z;
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
    : Expression ';'    // 注意：不能以 '{' 或 'function' 开头
    ;
```

**示例：**
```javascript
x = 5;
foo();
a + b;
```

### If 语句

```bnf
IfStatement
    : 'if' '(' Expression ')' Statement ('else' Statement)?
    ;
```

**示例：**
```javascript
if (x > 0) {
    console.log("positive");
} else {
    console.log("non-positive");
}
```

### 循环语句

```bnf
IterationStatement
    : 'do' Statement 'while' '(' Expression ')' ';'
    | 'while' '(' Expression ')' Statement
    | 'for' '(' ForInit? ';' Expression? ';' Expression? ')' Statement
    | 'for' '(' 'var' VariableDeclarationList ';' Expression? ';' Expression? ')' Statement
    | 'for' '(' LeftHandSideExpression 'in' Expression ')' Statement
    | 'for' '(' 'var' VariableDeclaration 'in' Expression ')' Statement
    ;

ForInit
    : Expression    // 不含 'in' 运算符
    ;
```

**示例：**
```javascript
// do-while
do {
    x++;
} while (x < 10);

// while
while (x > 0) {
    x--;
}

// for
for (var i = 0; i < 10; i++) {
    console.log(i);
}

// for-in
for (var key in obj) {
    console.log(key);
}
```

### 跳转语句

```bnf
ContinueStatement
    : 'continue' IDENTIFIER? ';'
    ;

BreakStatement
    : 'break' IDENTIFIER? ';'
    ;

ReturnStatement
    : 'return' Expression? ';'
    ;
```

**示例：**
```javascript
continue;
continue outerLoop;
break;
break label;
return;
return x + y;
```

### With 语句

```bnf
WithStatement
    : 'with' '(' Expression ')' Statement
    ;
```

**注意：** 严格模式下禁止使用 `with`。

### Switch 语句

```bnf
SwitchStatement
    : 'switch' '(' Expression ')' CaseBlock
    ;

CaseBlock
    : '{' CaseClauses? DefaultClause? CaseClauses? '}'
    ;

CaseClauses
    : CaseClause+
    ;

CaseClause
    : 'case' Expression ':' StatementList?
    ;

DefaultClause
    : 'default' ':' StatementList?
    ;
```

**示例：**
```javascript
switch (x) {
    case 1:
        console.log("one");
        break;
    case 2:
        console.log("two");
        break;
    default:
        console.log("other");
}
```

### 异常处理

```bnf
ThrowStatement
    : 'throw' Expression ';'    // 'throw' 和 Expression 之间不能有换行
    ;

TryStatement
    : 'try' Block Catch
    | 'try' Block Finally
    | 'try' Block Catch Finally
    ;

Catch
    : 'catch' '(' IDENTIFIER ')' Block
    ;

Finally
    : 'finally' Block
    ;
```

**示例：**
```javascript
try {
    riskyOperation();
} catch (e) {
    console.log("Error: " + e.message);
} finally {
    cleanup();
}

throw new Error("Something went wrong");
```

### Debugger 语句

```bnf
DebuggerStatement
    : 'debugger' ';'
    ;
```

---

## 函数

### 函数声明

```bnf
FunctionDeclaration
    : 'function' IDENTIFIER '(' FormalParameterList? ')' '{' FunctionBody '}'
    ;
```

**示例：**
```javascript
function add(a, b) {
    return a + b;
}
```

### 函数表达式

```bnf
FunctionExpression
    : 'function' IDENTIFIER? '(' FormalParameterList? ')' '{' FunctionBody '}'
    ;
```

**示例：**
```javascript
// 匿名函数
var add = function(a, b) {
    return a + b;
};

// 命名函数表达式
var factorial = function fact(n) {
    return n <= 1 ? 1 : n * fact(n - 1);
};
```

### 参数和函数体

```bnf
FormalParameterList
    : IDENTIFIER (',' IDENTIFIER)*
    ;

FunctionBody
    : SourceElements?
    ;
```

---

## 表达式（Expressions）

### 表达式总览（按优先级从低到高）

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
    : '=' | '*=' | '/=' | '%=' | '+=' | '-='
    | '<<=' | '>>=' | '>>>=' | '&=' | '^=' | '|='
    ;
```

**示例：**
```javascript
x = 5;
x += 10;
x <<= 2;
```

### 条件（三元）表达式

```bnf
ConditionalExpression
    : LogicalORExpression ('?' AssignmentExpression ':' AssignmentExpression)?
    ;
```

**示例：**
```javascript
var result = x > 0 ? "positive" : "non-positive";
```

### 逻辑表达式

```bnf
LogicalORExpression
    : LogicalANDExpression ('||' LogicalANDExpression)*
    ;

LogicalANDExpression
    : BitwiseORExpression ('&&' BitwiseORExpression)*
    ;
```

**示例：**
```javascript
a && b
x || y
a && b || c && d
```

### 位运算表达式

```bnf
BitwiseORExpression
    : BitwiseXORExpression ('|' BitwiseXORExpression)*
    ;

BitwiseXORExpression
    : BitwiseANDExpression ('^' BitwiseANDExpression)*
    ;

BitwiseANDExpression
    : EqualityExpression ('&' EqualityExpression)*
    ;
```

**示例：**
```javascript
a | b
a ^ b
a & b
```

### 相等性表达式

```bnf
EqualityExpression
    : RelationalExpression (('==' | '!=' | '===' | '!==') RelationalExpression)*
    ;
```

**示例：**
```javascript
a == b      // 宽松相等
a === b     // 严格相等
a != b      // 宽松不等
a !== b     // 严格不等
```

### 关系表达式

```bnf
RelationalExpression
    : ShiftExpression (('<' | '>' | '<=' | '>=' | 'instanceof' | 'in') ShiftExpression)*
    ;
```

**示例：**
```javascript
a < b
a >= b
obj instanceof Array
"name" in obj
```

### 移位表达式

```bnf
ShiftExpression
    : AdditiveExpression (('<<' | '>>' | '>>>') AdditiveExpression)*
    ;
```

**示例：**
```javascript
x << 2      // 左移
x >> 2      // 有符号右移
x >>> 2     // 无符号右移
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
    | 'delete' UnaryExpression
    | 'void' UnaryExpression
    | 'typeof' UnaryExpression
    | '++' UnaryExpression
    | '--' UnaryExpression
    | '+' UnaryExpression
    | '-' UnaryExpression
    | '~' UnaryExpression
    | '!' UnaryExpression
    ;
```

**示例：**
```javascript
delete obj.prop
void 0
typeof x
++i
--i
+x      // 转为数字
-x      // 取负
~x      // 按位取反
!flag   // 逻辑非
```

### 后缀表达式

```bnf
PostfixExpression
    : LeftHandSideExpression ('++' | '--')?    // 运算符前不能有换行
    ;
```

**示例：**
```javascript
i++
j--
```

### 左值表达式

```bnf
LeftHandSideExpression
    : CallExpression
    | NewExpression
    ;

CallExpression
    : MemberExpression Arguments (Arguments | '[' Expression ']' | '.' IDENTIFIER)*
    ;

NewExpression
    : MemberExpression
    | 'new' NewExpression
    ;

MemberExpression
    : PrimaryExpression ('[' Expression ']' | '.' IDENTIFIER)*
    | FunctionExpression ('[' Expression ']' | '.' IDENTIFIER)*
    | 'new' MemberExpression Arguments ('[' Expression ']' | '.' IDENTIFIER)*
    ;
```

**示例：**
```javascript
foo()
obj.method()
arr[0]
obj.prop
new Date()
new Array(10)
```

### 参数列表

```bnf
Arguments
    : '(' ArgumentList? ')'
    ;

ArgumentList
    : AssignmentExpression (',' AssignmentExpression)*
    ;
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
    : NULL
    | TRUE
    | FALSE
    | NUMBER
    | STRING
    ;
```

**示例：**
```javascript
null
true
false
42
3.14
"hello"
'world'
```

### 数组字面量

```bnf
ArrayLiteral
    : '[' ElementList? ']'
    ;

ElementList
    : AssignmentExpression? (',' AssignmentExpression?)*
    ;
```

**示例：**
```javascript
[]
[1, 2, 3]
[1, , 3]        // 稀疏数组
[1, 2, 3, ]     // 尾随逗号
```

### 对象字面量

```bnf
ObjectLiteral
    : '{' PropertyNameAndValueList? '}'
    ;

PropertyNameAndValueList
    : PropertyAssignment (',' PropertyAssignment)* ','?
    ;

PropertyAssignment
    : PropertyName ':' AssignmentExpression
    | 'get' PropertyName '(' ')' '{' FunctionBody '}'
    | 'set' PropertyName '(' IDENTIFIER ')' '{' FunctionBody '}'
    ;

PropertyName
    : IDENTIFIER
    | STRING
    | NUMBER
    ;
```

**示例：**
```javascript
{}
{ name: "John", age: 30 }
{ "special-key": value }
{ 0: "first", 1: "second" }
{
    get fullName() { return this.first + " " + this.last; },
    set fullName(v) { /* ... */ }
}
```

---

## 运算符优先级表

从低到高排列：

| 优先级 | 运算符 | 描述 | 结合性 |
|--------|--------|------|--------|
| 1 | `,` | 逗号 | 左到右 |
| 2 | `= += -= *= /= %= <<= >>= >>>= &= ^= \|=` | 赋值 | 右到左 |
| 3 | `?:` | 条件（三元） | 右到左 |
| 4 | `\|\|` | 逻辑或 | 左到右 |
| 5 | `&&` | 逻辑与 | 左到右 |
| 6 | `\|` | 按位或 | 左到右 |
| 7 | `^` | 按位异或 | 左到右 |
| 8 | `&` | 按位与 | 左到右 |
| 9 | `== != === !==` | 相等性 | 左到右 |
| 10 | `< > <= >= instanceof in` | 关系 | 左到右 |
| 11 | `<< >> >>>` | 移位 | 左到右 |
| 12 | `+ -` | 加减 | 左到右 |
| 13 | `* / %` | 乘除取模 | 左到右 |
| 14 | `! ~ + - typeof void delete ++ --` | 一元（前缀） | 右到左 |
| 15 | `++ --` | 后缀 | 无 |
| 16 | `new` (带参数) | 带参数的 new | 右到左 |
| 17 | `. [] ()` | 成员访问、调用 | 左到右 |

---

## 自动分号插入（ASI）

JavaScript 在某些情况下会自动插入分号：

1. 当遇到换行符，且下一个 token 无法与前面的语句连接时
2. 当遇到 `}` 时
3. 当遇到文件末尾时

**特殊规则：**
- `return`、`throw`、`break`、`continue` 后如果有换行，会自动插入分号
- `++` 和 `--` 作为后缀时，不能与操作数之间有换行

**示例：**
```javascript
// 这会被解析为 return; undefined;
return
undefined

// 应该写成
return undefined;
```

---

## 保留字

### ES5 关键字

```
break      case       catch      continue   debugger
default    delete     do         else       finally
for        function   if         in         instanceof
new        return     switch     this       throw
try        typeof     var        void       while
with
```

### 未来保留字（严格模式）

```
class      const      enum       export     extends
import     super
implements interface  let        package    private
protected  public     static     yield
```

---

## 参考资料

- [ECMAScript 5.1 规范](https://262.ecma-international.org/5.1/)
- [MDN JavaScript 参考](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference)
