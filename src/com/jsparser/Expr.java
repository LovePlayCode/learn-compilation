package com.jsparser;

import java.util.List;

/**
 * JavaScript Expr AST 节点基类
 * 
 * 使用访问者模式支持对 AST 的各种操作（解释执行、代码生成、静态分析等）
 */
abstract class Expr {
    /**
     * 访问者接口
     * 
     * @param <R> 访问方法的返回类型
     */
    interface Visitor<R> {
        R visitLiteralExpr(Literal expr);

        R visitArrayLiteralExpr(ArrayLiteral expr);

        R visitObjectLiteralExpr(ObjectLiteral expr);

        R visitIdentifierExpr(Identifier expr);

        R visitThisExpr(This expr);

        R visitUnaryExpr(Unary expr);

        R visitBinaryExpr(Binary expr);

        R visitLogicalExpr(Logical expr);

        R visitConditionalExpr(Conditional expr);

        R visitSequenceExpr(Sequence expr);

        R visitAssignExpr(Assign expr);

        R visitMemberExpr(Member expr);

        R visitCallExpr(Call expr);

        R visitNewExpr(New expr);

        R visitFunctionExpr(Function expr);

        R visitTypeofExpr(Typeof expr);

        R visitVoidExpr(Void expr);

        R visitDeleteExpr(Delete expr);

        R visitInstanceofExpr(Instanceof expr);

        R visitInExpr(In expr);

        R visitGroupingExpr(Grouping expr);
    }

    /**
     * Literal expr
     */
    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }

    /**
     * ArrayLiteral expr
     */
    static class ArrayLiteral extends Expr {
        ArrayLiteral(List<Expr> elements) {
            this.elements = elements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitArrayLiteralExpr(this);
        }

        final List<Expr> elements;
    }

    /**
     * ObjectLiteral expr
     */
    static class ObjectLiteral extends Expr {
        ObjectLiteral(List<Property> properties) {
            this.properties = properties;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitObjectLiteralExpr(this);
        }

        final List<Property> properties;
    }

    /**
     * Identifier expr
     * 变量引用表达式，如: x, y, z
     */
    static class Identifier extends Expr {
        Identifier(Token name) {
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIdentifierExpr(this);
        }

        final Token name;
    }

    /**
     * This expr
     */
    static class This extends Expr {
        This(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }

        final Token keyword;
    }

    /**
     * Unary expr
     */
    static class Unary extends Expr {
        Unary(Token operator, Expr operand, boolean prefix) {
            this.operator = operator;
            this.operand = operand;
            this.prefix = prefix;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr operand;
        final boolean prefix;
    }

    /**
     * Binary expr
     */
    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    /**
     * Logical expr
     */
    static class Logical extends Expr {
        Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    /**
     * Conditional expr
     */
    static class Conditional extends Expr {
        Conditional(Expr condition, Expr consequent, Expr alternate) {
            this.condition = condition;
            this.consequent = consequent;
            this.alternate = alternate;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitConditionalExpr(this);
        }

        final Expr condition;
        final Expr consequent;
        final Expr alternate;
    }

    /**
     * Sequence expr
     */
    static class Sequence extends Expr {
        Sequence(List<Expr> expressions) {
            this.expressions = expressions;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSequenceExpr(this);
        }

        final List<Expr> expressions;
    }

    /**
     * Assign expr
     */
    static class Assign extends Expr {
        Assign(Expr target, Token operator, Expr value) {
            this.target = target;
            this.operator = operator;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpr(this);
        }

        final Expr target;
        final Token operator;
        final Expr value;
    }

    /**
     * Member expr
     */
    static class Member extends Expr {
        Member(Expr object, Token property, boolean computed) {
            this.object = object;
            this.property = property;
            this.computed = computed;
            this.computedProperty = null;
        }

        Member(Expr object, Expr computedProperty, boolean computed) {
            this.object = object;
            this.property = null;
            this.computed = computed;
            this.computedProperty = computedProperty;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitMemberExpr(this);
        }

        final Expr object;
        final Token property;
        final boolean computed;
        final Expr computedProperty;
    }

    /**
     * Call expr
     */
    static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }

        final Expr callee;
        final Token paren;
        final List<Expr> arguments;
    }

    /**
     * New expr
     */
    static class New extends Expr {
        New(Token keyword, Expr callee, List<Expr> arguments) {
            this.keyword = keyword;
            this.callee = callee;
            this.arguments = arguments;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitNewExpr(this);
        }

        final Token keyword;
        final Expr callee;
        final List<Expr> arguments;
    }

    /**
     * Function expr
     */
    static class Function extends Expr {
        Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionExpr(this);
        }

        final Token name;
        final List<Token> params;
        final List<Stmt> body;
    }

    /**
     * Typeof expr
     */
    static class Typeof extends Expr {
        Typeof(Token operator, Expr operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTypeofExpr(this);
        }

        final Token operator;
        final Expr operand;
    }

    /**
     * Void expr
     */
    static class Void extends Expr {
        Void(Token operator, Expr operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVoidExpr(this);
        }

        final Token operator;
        final Expr operand;
    }

    /**
     * Delete expr
     */
    static class Delete extends Expr {
        Delete(Token operator, Expr operand) {
            this.operator = operator;
            this.operand = operand;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitDeleteExpr(this);
        }

        final Token operator;
        final Expr operand;
    }

    /**
     * Instanceof expr
     */
    static class Instanceof extends Expr {
        Instanceof(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitInstanceofExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    /**
     * In expr
     */
    static class In extends Expr {
        In(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitInExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    /**
     * Grouping expr
     */
    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
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
