package com.jsparser;

import java.util.List;

/**
 * JavaScript Stmt AST 节点基类
 * 
 * 使用访问者模式支持对 AST 的各种操作（解释执行、代码生成、静态分析等）
 */
abstract class Stmt {
    /**
     * 访问者接口
     * 
     * @param <R> 访问方法的返回类型
     */
    interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);

        R visitBlockStmt(Block stmt);

        R visitEmptyStmt(Empty stmt);

        R visitVarStmt(Var stmt);

        R visitFunctionStmt(Function stmt);

        R visitIfStmt(If stmt);

        R visitWhileStmt(While stmt);

        R visitDoWhileStmt(DoWhile stmt);

        R visitForStmt(For stmt);

        R visitForInStmt(ForIn stmt);

        R visitSwitchStmt(Switch stmt);

        R visitBreakStmt(Break stmt);

        R visitContinueStmt(Continue stmt);

        R visitReturnStmt(Return stmt);

        R visitThrowStmt(Throw stmt);

        R visitTryStmt(Try stmt);

        R visitWithStmt(With stmt);

        R visitLabeledStmt(Labeled stmt);

        R visitDebuggerStmt(Debugger stmt);
    }

    /**
     * Expression stmt
     */
    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }

    /**
     * Block stmt
     */
    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        public List<Stmt> getStatements() {
            return statements;
        }

        final List<Stmt> statements;
    }

    /**
     * Empty stmt
     */
    static class Empty extends Stmt {
        Empty(Token semicolon) {
            this.semicolon = semicolon;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitEmptyStmt(this);
        }

        final Token semicolon;
    }

    /**
     * Var stmt
     */
    static class Var extends Stmt {
        Var(List<VarDeclarator> declarations) {
            this.declarations = declarations;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        final List<VarDeclarator> declarations;
    }

    /**
     * Function stmt
     */
    static class Function extends Stmt {
        Function(Token name, List<Token> params, Stmt.Block body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }

        final Token name;
        final List<Token> params;
        final Stmt.Block body;
    }

    /**
     * If stmt
     */
    static class If extends Stmt {
        If(Expr condition, Stmt consequent, Stmt alternate) {
            this.condition = condition;
            this.consequent = consequent;
            this.alternate = alternate;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }

        final Expr condition;
        final Stmt consequent;
        final Stmt alternate;
    }

    /**
     * While stmt
     */
    static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }

        final Expr condition;
        final Stmt body;
    }

    /**
     * DoWhile stmt
     */
    static class DoWhile extends Stmt {
        DoWhile(Stmt body, Expr condition) {
            this.body = body;
            this.condition = condition;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitDoWhileStmt(this);
        }

        final Stmt body;
        final Expr condition;
    }

    /**
     * For stmt
     */
    static class For extends Stmt {
        For(Stmt init, Expr condition, Expr update, Stmt body) {
            this.init = init;
            this.condition = condition;
            this.update = update;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForStmt(this);
        }

        final Stmt init;
        final Expr condition;
        final Expr update;
        final Stmt body;
    }

    /**
     * ForIn stmt
     */
    static class ForIn extends Stmt {
        ForIn(Stmt left, Expr right, Stmt body) {
            this.left = left;
            this.right = right;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitForInStmt(this);
        }

        final Stmt left;
        final Expr right;
        final Stmt body;
    }

    /**
     * Switch stmt
     */
    static class Switch extends Stmt {
        Switch(Expr discriminant, List<SwitchCase> cases) {
            this.discriminant = discriminant;
            this.cases = cases;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSwitchStmt(this);
        }

        final Expr discriminant;
        final List<SwitchCase> cases;
    }

    /**
     * Break stmt
     */
    static class Break extends Stmt {
        Break(Token keyword, Token label) {
            this.keyword = keyword;
            this.label = label;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStmt(this);
        }

        final Token keyword;
        final Token label;
    }

    /**
     * Continue stmt
     */
    static class Continue extends Stmt {
        Continue(Token keyword, Token label) {
            this.keyword = keyword;
            this.label = label;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitContinueStmt(this);
        }

        final Token keyword;
        final Token label;
    }

    /**
     * Return stmt
     */
    static class Return extends Stmt {
        Return(Token keyword, Expr argument) {
            this.keyword = keyword;
            this.argument = argument;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }

        final Token keyword;
        final Expr argument;
    }

    /**
     * Throw stmt
     */
    static class Throw extends Stmt {
        Throw(Token keyword, Expr argument) {
            this.keyword = keyword;
            this.argument = argument;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThrowStmt(this);
        }

        final Token keyword;
        final Expr argument;
    }

    /**
     * Try stmt
     */
    static class Try extends Stmt {
        Try(Stmt block, CatchClause handler, Stmt finalizer) {
            this.block = block;
            this.handler = handler;
            this.finalizer = finalizer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTryStmt(this);
        }

        final Stmt block;
        final CatchClause handler;
        final Stmt finalizer;
    }

    /**
     * With stmt
     */
    static class With extends Stmt {
        With(Expr object, Stmt body) {
            this.object = object;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWithStmt(this);
        }

        final Expr object;
        final Stmt body;
    }

    /**
     * Labeled stmt
     */
    static class Labeled extends Stmt {
        Labeled(Token label, Stmt body) {
            this.label = label;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLabeledStmt(this);
        }

        final Token label;
        final Stmt body;
    }

    /**
     * Debugger stmt
     */
    static class Debugger extends Stmt {
        Debugger(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitDebuggerStmt(this);
        }

        final Token keyword;
    }

    /**
     * 接受访问者
     * 
     * @param visitor 访问者对象
     * @param <R>     返回类型
     * @return 访问结果
     */
    abstract <R> R accept(Visitor<R> visitor);
}
