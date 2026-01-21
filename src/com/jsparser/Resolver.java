package com.jsparser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.jsparser.Stmt.Var;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION
    }

    private void endScope() {
        scopes.pop();
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    /**
     * 块语句，创建新作用域并执行语句列表
     * 例: { var x = 1; console.log(x); }
     */
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitEmptyStmt(Stmt.Empty stmt) {
        return null;
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    // 全局作用域可以声明多个同名变量，局部作用域不允许这么做(本身 JS 允许这么做就很奇怪，本人实现的解释器中不允许)
    private void declare(Token name) {
        if (scopes.isEmpty())
            return;
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name,
                    "Already variable with this name in this scope.");
        }
        // false 表示变量"尚未就绪"
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, true);
    }

    @Override
    public Void visitVarStmt(Var stmt) {

        List<VarDeclarator> declarations = stmt.declarations;
        for (VarDeclarator declaration : declarations) {
            declare(declaration.name);
            if (declaration.init != null) {
                resolve(declaration.init);
            }
            define(declaration.name);
        }
        return null;
    }

    private void resolveFunction(Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void resolveFunction(Expr.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.consequent);
        if (stmt.alternate != null)
            resolve(stmt.alternate);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitDoWhileStmt(Stmt.DoWhile stmt) {
        return null;
    }

    @Override
    public Void visitForStmt(Stmt.For stmt) {
        return null;
    }

    @Override
    public Void visitForInStmt(Stmt.ForIn stmt) {
        return null;
    }

    @Override
    public Void visitSwitchStmt(Stmt.Switch stmt) {
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        return null;
    }

    @Override
    public Void visitContinueStmt(Stmt.Continue stmt) {
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (stmt.argument != null) {
            resolve(stmt.argument);
        }

        return null;
    }

    @Override
    public Void visitThrowStmt(Stmt.Throw stmt) {
        return null;
    }

    @Override
    public Void visitTryStmt(Stmt.Try stmt) {
        return null;
    }

    @Override
    public Void visitWithStmt(Stmt.With stmt) {
        return null;
    }

    @Override
    public Void visitLabeledStmt(Stmt.Labeled stmt) {
        return null;
    }

    @Override
    public Void visitDebuggerStmt(Stmt.Debugger stmt) {
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitArrayLiteralExpr(Expr.ArrayLiteral expr) {
        return null;
    }

    @Override
    public Void visitObjectLiteralExpr(Expr.ObjectLiteral expr) {
        beginScope();
        for (Property properties : expr.properties) {
            resolve(properties.value);
        }
        endScope();
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    /**
     * 解析赋值目标（Expr 版本）
     * 支持 Identifier 和 Member 表达式
     */
    private void resolveLocal(Expr expr, Expr target) {
        if (target instanceof Expr.Identifier) {
            // 变量赋值：a = 1
            resolveLocal(expr, ((Expr.Identifier) target).name);
        } else if (target instanceof Expr.Member) {
            // 属性赋值：obj.x = 1，解析对象部分
            resolve(((Expr.Member) target).object);
        }
    }

    @Override
    public Void visitIdentifierExpr(Expr.Identifier expr) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            Lox.error(expr.name,
                    "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;

    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.operand);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitConditionalExpr(Expr.Conditional expr) {
        return null;
    }

    @Override
    public Void visitSequenceExpr(Expr.Sequence expr) {
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.target);
        return null;
    }

    @Override
    public Void visitMemberExpr(Expr.Member expr) {
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitNewExpr(Expr.New expr) {
        return null;
    }

    @Override
    public Void visitFunctionExpr(Expr.Function expr) {
        // 函数表达式没有name;
        if (expr.name != null) {
            declare(expr.name);
            define(expr.name);
        }
        resolveFunction(expr, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitTypeofExpr(Expr.Typeof expr) {
        return null;
    }

    @Override
    public Void visitVoidExpr(Expr.Void expr) {
        return null;
    }

    @Override
    public Void visitDeleteExpr(Expr.Delete expr) {
        return null;
    }

    @Override
    public Void visitInstanceofExpr(Expr.Instanceof expr) {
        return null;
    }

    @Override
    public Void visitInExpr(Expr.In expr) {
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }
}