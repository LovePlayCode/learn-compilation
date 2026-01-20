package com.jsparser;

import java.util.ArrayList;
import java.util.List;

import com.jsparser.Expr.ArrayLiteral;
import com.jsparser.Expr.Assign;
import com.jsparser.Expr.Binary;
import com.jsparser.Expr.Call;
import com.jsparser.Expr.Conditional;
import com.jsparser.Expr.Delete;
import com.jsparser.Expr.Function;
import com.jsparser.Expr.Grouping;
import com.jsparser.Expr.Identifier;
import com.jsparser.Expr.In;
import com.jsparser.Expr.Instanceof;
import com.jsparser.Expr.Literal;
import com.jsparser.Expr.Logical;
import com.jsparser.Expr.Member;
import com.jsparser.Expr.New;
import com.jsparser.Expr.ObjectLiteral;
import com.jsparser.Expr.Sequence;
import com.jsparser.Expr.This;
import com.jsparser.Expr.Typeof;
import com.jsparser.Expr.Unary;
import com.jsparser.Expr.Void;
import com.jsparser.Stmt.Block;
import com.jsparser.Stmt.Break;
import com.jsparser.Stmt.Continue;
import com.jsparser.Stmt.Debugger;
import com.jsparser.Stmt.DoWhile;
import com.jsparser.Stmt.Empty;
import com.jsparser.Stmt.Expression;
import com.jsparser.Stmt.For;
import com.jsparser.Stmt.ForIn;
import com.jsparser.Stmt.If;
import com.jsparser.Stmt.Labeled;
import com.jsparser.Stmt.Return;
import com.jsparser.Stmt.Switch;
import com.jsparser.Stmt.Throw;
import com.jsparser.Stmt.Try;
import com.jsparser.Stmt.Var;
import com.jsparser.Stmt.While;
import com.jsparser.Stmt.With;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    // 全局作用域的概念
    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        globals.define("console", new Console());
    }

    /**
     * 数组字面量表达式
     * 例: [1, 2, 3] 或 [a, b + 1, "hello"]
     */
    @Override
    public Object visitArrayLiteralExpr(ArrayLiteral expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 赋值表达式
     * 例: x = 1, x += 2, obj.prop = value
     */
    @Override
    public Object visitAssignExpr(Assign expr) {
        // TODO Auto-generated method stub
        return null;
    }

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

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);

                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);

                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);

                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);

                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);

                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);

                return (double) left * (double) right;
            // +号可以用来拼接字符串或计算
            case PLUS:

                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                // 替换部分开始
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        // Unreachable.
        return null;

    }

    /**
     * 函数调用表达式
     * 例: foo(), obj.method(1, 2), console.log("hello")
     */
    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }
        LoxCallable function = (LoxCallable) callee;
        // 检查传入的参数是否满足函数本身要求的参数
        // arity < 0 表示可变参数函数，跳过检查
        if (function.arity() >= 0 && arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    /**
     * 条件（三元）表达式
     * 例: a > b ? a : b, isValid ? "yes" : "no"
     */
    @Override
    public Object visitConditionalExpr(Conditional expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * delete 运算符表达式
     * 例: delete obj.prop, delete arr[0]
     */
    @Override
    public Object visitDeleteExpr(Delete expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 函数表达式（匿名函数/命名函数表达式）
     * 例: function() { return 1; }, function add(a, b) { return a + b; }
     */
    @Override
    public Object visitFunctionExpr(Function expr) {
        // TODO Auto-generated method stub
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * 分组（括号）表达式，递归子表达式求值即可
     * 例: (a + b), (1 + 2) * 3
     */
    @Override
    public Object visitGroupingExpr(Grouping expr) {

        return evaluate(expr.expression);
    }

    /**
     * 标识符表达式，从环境中查找变量的值
     * 例: x, myVar, console
     */
    @Override
    public Object visitIdentifierExpr(Identifier expr) {
        return environment.get(expr.name);
    }

    /**
     * in 运算符表达式，判断属性是否存在于对象中
     * 例: "name" in obj, 0 in arr
     */
    @Override
    public Object visitInExpr(In expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * instanceof 运算符表达式，判断对象是否是某个类的实例
     * 例: obj instanceof Array, err instanceof Error
     */
    @Override
    public Object visitInstanceofExpr(Instanceof expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 字面量表达式，直接返回值
     * 例: 42, "hello", true, false, null
     */
    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    /**
     * 逻辑运算符表达式（短路求值）
     * 例: a && b, a || b, !flag && isReady
     */
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    /**
     * 成员访问表达式
     * 例: obj.prop（点号访问）, arr[0]（计算属性访问）
     */
    @Override
    public Object visitMemberExpr(Member expr) {
        return null;
    }

    /**
     * new 表达式，创建对象实例
     * 例: new Date(), new Array(10), new Person("John")
     */
    @Override
    public Object visitNewExpr(New expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 对象字面量表达式
     * 例: {}, { name: "John", age: 30 }, { get x() { return 1; } }
     */
    @Override
    public Object visitObjectLiteralExpr(ObjectLiteral expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 序列（逗号）表达式，依次求值并返回最后一个值
     * 例: (a = 1, b = 2, a + b) 返回 3
     */
    @Override
    public Object visitSequenceExpr(Sequence expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * this 表达式，返回当前执行上下文的 this 值
     * 例: this, this.name, this.method()
     */
    @Override
    public Object visitThisExpr(This expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * typeof 运算符表达式，返回操作数的类型字符串
     * 例: typeof x 返回 "undefined"/"number"/"string"/"boolean"/"object"/"function"
     */
    @Override
    public Object visitTypeofExpr(Typeof expr) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    /**
     * 一元运算符表达式
     * 前缀: !a, -a, +a, ++a, --a, typeof a
     * 后缀: a++, a--
     */
    @Override
    public Object visitUnaryExpr(Unary expr) {
        // TODO: 有前缀和后缀的区别，需要进行完善
        Object right = evaluate(expr.operand);
        if (expr.prefix) {
            switch (expr.operator.type) {
                case BANG:
                    return !isTruthy(right);
                case MINUS:
                    checkNumberOperand(expr.operator, right);

                    return -(double) right;
            }
        }

        // Unreachable.
        return null;

    }

    /**
     * void 运算符表达式，求值后返回 undefined
     * 例: void 0, void(expression)
     */
    @Override
    public Object visitVoidExpr(Void expr) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 表达式语句，执行表达式但不使用返回值
     * 例: foo();, x = 1;, console.log("hi");
     */
    @Override
    public Void visitExpressionStmt(Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    void executeBlock(List<Stmt> statements,
            Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    /**
     * 块语句，创建新作用域并执行语句列表
     * 例: { var x = 1; console.log(x); }
     */
    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    /**
     * 空语句，什么都不做
     * 例: ; (单独的分号)
     */
    @Override
    public Void visitEmptyStmt(Empty stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitEmptyStmt'");
    }

    /**
     * 变量声明语句，在当前环境中定义变量
     * 例: var x;, var x = 1;, var a = 1, b = 2;
     */
    @Override
    public Void visitVarStmt(Var stmt) {
        if (stmt.declarations.size() > 0) {
            for (VarDeclarator declarations : stmt.declarations) {
                Object value = declarations.init;
                environment.define(declarations.name.lexeme, value);
            }
        }
        return null;
    }

    /**
     * 函数声明语句
     * 例: function add(a, b) { return a + b; }
     */
    @Override
    public Void visitFunctionStmt(com.jsparser.Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    /**
     * if 条件语句
     * 例: if (x > 0) { ... }, if (x) { ... } else { ... }
     */
    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.consequent);
        } else if (stmt.alternate != null) {
            execute(stmt.alternate);
        }

        return null;
    }

    /**
     * while 循环语句
     * 例: while
     * (i < 10) { i++; }
     */
    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    /**
     * do-while 循环语句，至少执行一次循环体
     * 例: do { i++; } while (i < 10);
     */
    @Override
    public Void visitDoWhileStmt(DoWhile stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDoWhileStmt'");
    }

    /**
     * for 循环语句
     * 例: for (var i = 0; i < 10; i++) { ... }
     */
    @Override
    public Void visitForStmt(For stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitForStmt'");
    }

    /**
     * for-in 循环语句，遍历对象的可枚举属性
     * 例: for (var key in obj) { console.log(key); }
     */
    @Override
    public Void visitForInStmt(ForIn stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitForInStmt'");
    }

    /**
     * switch 语句
     * 例: switch (x) { case 1: ... break; default: ... }
     */
    @Override
    public Void visitSwitchStmt(Switch stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitSwitchStmt'");
    }

    /**
     * break 语句，跳出当前循环或 switch
     * 例: break;, break label;
     */
    @Override
    public Void visitBreakStmt(Break stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBreakStmt'");
    }

    /**
     * continue 语句，跳过当前循环迭代
     * 例: continue;, continue label;
     */
    @Override
    public Void visitContinueStmt(Continue stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitContinueStmt'");
    }

    /**
     * return 语句，从函数返回值
     * 例: return;, return x + y;
     */
    @Override
    public Void visitReturnStmt(Return stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStmt'");
    }

    /**
     * throw 语句，抛出异常
     * 例: throw new Error("message");, throw "error";
     */
    @Override
    public Void visitThrowStmt(Throw stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitThrowStmt'");
    }

    /**
     * try-catch-finally 语句，异常处理
     * 例: try { ... } catch(e) { ... } finally { ... }
     */
    @Override
    public Void visitTryStmt(Try stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitTryStmt'");
    }

    /**
     * with 语句，扩展作用域链（不推荐使用）
     * 例: with (obj) { x = 1; } // 等同于 obj.x = 1
     */
    @Override
    public Void visitWithStmt(With stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWithStmt'");
    }

    /**
     * 标签语句，为语句添加标签用于 break/continue
     * 例: outer: for (...) { inner: for (...) { break outer; } }
     */
    @Override
    public Void visitLabeledStmt(Labeled stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLabeledStmt'");
    }

    /**
     * debugger 语句，触发调试器断点
     * 例: debugger;
     */
    @Override
    public Void visitDebuggerStmt(Debugger stmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDebuggerStmt'");
    }

}
