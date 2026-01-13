package com.craftinginterpreters.lox;

class RpnPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return expr.left.accept(this) + " " +
                expr.right.accept(this) + " " +
                expr.operator.lexeme;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        String operand = expr.right.accept(this);
        // 对于一元负号，可以用特殊符号表示，或者用 0 减去操作数
        if (expr.operator.type == TokenType.MINUS) {
            return operand + " neg";
        }
        return operand + " " + expr.operator.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return expr.value.accept(this) + " " + expr.name.lexeme + " =";
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        StringBuilder builder = new StringBuilder();
        for (Expr arg : expr.arguments) {
            builder.append(arg.accept(this)).append(" ");
        }
        builder.append(expr.callee.accept(this));
        return builder.toString().trim();
    }

    @Override
    public String visitFunctionExpr(Expr.Function expr) {
        return "<lambda>";
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return expr.object.accept(this) + " " + expr.name.lexeme + " .";
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return expr.left.accept(this) + " " +
                expr.right.accept(this) + " " +
                expr.operator.lexeme;
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return expr.object.accept(this) + " " +
                expr.name.lexeme + " " +
                expr.value.accept(this) + " .=";
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return "super " + expr.method.lexeme + " .";
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    public static void main(String[] args) {
        // (1 + 2) * (4 - 3) => 1 2 + 4 3 - *
        Expr expression = new Expr.Binary(
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(1),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Expr.Literal(2))),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(4),
                                new Token(TokenType.MINUS, "-", null, 1),
                                new Expr.Literal(3))));

        System.out.println(new RpnPrinter().print(expression));
        // 输出: 1 2 + 4 3 - *
    }
}
